package org.sfvlug.tutorial;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Our Main Activity (the user interface). This is launched when the user
 * starts our App. Activity extends Context, so we can access all out
 * application data from within this class using functions such as
 * findViewById.
 */
public class SfvLug extends Activity
{
    /* URL Constants - signed Meetup meetings API */
    private final String MEETUP_API =
        "http://api.meetup.com/2/events?"
            + "group_id=2575122&"
            + "status=upcoming&"
            + "order=time&"
            + "limited_events=False&"
            + "desc=false&"
            + "offset=0&"
            + "photo-host=public&"
            + "format=json&"
            + "page=500&"
            + "fields=&"
            + "sig_id=28045742&"
            + "sig=42d2f7ec48b697ba087db2d8f2c65a2f144de8b1";

    /* Activity Widgets */
    private TextView  statusView;  // Text view for dumping ASCII status

    /* Read URL data as a JSON Object
     *   Android provides several HTTP/URL libraries, like the JSON library, we
     *   arbitrarily picked HttpURLConnection.
     *
     *   Personally, I like to use libraries that are not Android specific
     *   because it makes my code more reusable in other java applications.
     *
     *   We also keep the URL library internal to this function so that we can
     *   easily change it if we ever want to. */
    private JSONObject loadUrl(String location)
    {
        LinkedList<String> lines = new LinkedList<String>();

        // Download URL to array of lines
        try {
            // Open URL
            URL url = new URL(location);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            // Open buffered reader
            InputStream       is  = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader    buf = new BufferedReader(isr);

            // Read data into lines array
            String line;
            while ((line = buf.readLine()) != null)
                lines.add(line);

            // Close keep-alive connections
            conn.disconnect();
        }
        catch (MalformedURLException e) {
            Log.d("SfvLug", "Invalid URL: " + location);
        }
        catch (IOException e) {
            Log.d("SfvLug", "Download failed for: " + location);
        }

        // Load text into JSON Object
        String text = TextUtils.join("", lines);
        try {
            return new JSONObject(text);
        } catch (JSONException e) {
            Log.d("SfvLug", "Invalid JSON Object: " + text);
            return null;
        }
    }

    /* Display the meeting status to the user */
    private void setStatus(JSONObject status)
    {
        String text = null;

        // Extract info from JSON object and format output messages
        try {
            DateFormat fmt = new SimpleDateFormat("yyyy-MM-DD'T'hh:mm:ss");

            JSONObject event = status.getJSONArray("results").getJSONObject(0);
            JSONObject venue = event.getJSONObject("venue");

            String name  = event.getString("name");
            String where = venue.getString("name") + " " + venue.getString("city");
            long   stamp = event.getLong("time") + event.getLong("utc_offset");

            String when  = fmt.format(new Date(stamp));

            text = "What:  " + name  + "\n" +
                   "When:  " + when  + "\n" +
                   "Where: " + where + "\n";
        }
        catch (NullPointerException e) {
            text = "Error downloading meeting status";
        }
        catch (JSONException e) {
            text = "Error parsing meeting status: " + e.toString();
        }
        Log.d("LugAtUcla", "Next meeting: " + this.toString());

        // Set text view contents
        statusView.setTypeface(Typeface.MONOSPACE);
        statusView.setText(text);
    }

    /* Trigger a refresh of the meeting status */
    private void loadStatus()
    {
        // Set URI location
        String location = this.MEETUP_API;

        // Notify the user that we're loading
        statusView.setText("Loading..");

        // Update status in a background thread
        //
        // In Android, we normally cannot access the network from the main
        // thread; doing so would cause the user interface to freeze during
        // data transfer.
        //
        // Again, android provides several ways around this, here we use an
        // AsyncTask which lets us run some code in a background thread and
        // then update the user interface once the background code has
        // finished.
        //
        // The first Java Generic parameter are:
        //   1. String     - argument for doInBackground, from .execute()
        //   2. Void       - not used here, normally used for progress bars
        //   3. JSONObject - the return type from doInBackground which is
        //                   passed to onPostExecute function.
        new AsyncTask<String, Void, JSONObject>() {

            // Called from a background thread, so we don't block the user
            // interface. Using AsyncTask synchronization is handled for us.
            protected JSONObject doInBackground(String... args) {
                // Java passes this as a variable argument array,
                // but we only use the first entry.
                return SfvLug.this.loadUrl(args[0]);
            }

            // Called once in the main thread once doInBackground finishes.
            // This is executed in the Main thread once again so that we can
            // update the user interface.
            protected void onPostExecute(JSONObject status) {
                SfvLug.this.setStatus(status);
                Toast.makeText(SfvLug.this, "Load complete", Toast.LENGTH_SHORT).show();
            }

        }.execute(location);
    }

    /* Callbacks - there are multiple ways to specify callback but one of the
     *             easiest ones is to add the onClick attribute to
     *             res/layout/main.xml with a function to call when the user
     *             presses the button.
     *
     *             Callback methods assigned in this way must be public and
     *             have the correct argument types. */
    public void onRefresh(View btn)
    {
        this.loadStatus();
    }

    /* Initialize the App menu
     *   we define the menu in res/menu/menu.xml so all we have to do is load
     *   it using MenuInflater. */
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /* Handle menu item selections
     *   since we named all our menu entries in the XML file we can just switch
     *   on the menu item ID in order to determine which entry was selected by
     *   the user */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {

            // reload the meeting status
            case R.id.refresh:
                this.loadStatus();
                return true;

            default:
                return false;
        }
    }

    /* Initialize our app
     *   The "lifecycle" of an Android App is much more complex than this, but
     *   luckily we can do everything in onCreate because we don't have to save
     *   state when the user starts and stops the App. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the user interfaces from the XML UI description
        setContentView(R.layout.main);

        // Lookup all our user interface widgets
        this.statusView  =  (TextView)findViewById(R.id.status);

        // Trigger the initial status update
        this.loadStatus();
    }
}

// vim: ts=4 sw=4 sts=4 et
