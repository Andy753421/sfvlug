package org.sfvlug.tutorial;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import java.util.LinkedList;
import java.util.List;

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
    /* URL Constants - only used if the preferences file is invalid */
    private final String COFFEE_JSON = "https://linux.ucla.edu/api/coffee.json";
    private final String COFFEE_LOG  = "https://linux.ucla.edu/api/coffee.log";

    /* Settings - this provides access to the actual settings data,
     *            the settings activity is in the Settings.java class
     *            and provides the user interface for editing the settings. */
    private SharedPreferences settings;

    /* Activity Widgets */
    private TextView  statusView;  // Text view for dumping ASCII status
                                   // This also shows error messages

    private ImageView potIn;       // Star icon for pos status
    private TextView  potActivity; // Pot activity timestamp field
    private ImageView lidOpen;     // Star icon for lid status
    private TextView  lidActivity; // Lid activity timestamp field

    /* Convert URL data into a JSON Object
     *   Currently we don't actually need separate functions for reading lines
     *   of text and reading JSON objects However, if we want to support the
     *   COFFEE_LOG URL in the future having a function to get the lines of
     *   text will be useful. */
    private JSONObject readUrlObject(String location)
    {
        List<String> lines = readUrlLines(location);
        String       text  = TextUtils.join("", lines);
        try {
            return new JSONObject(text);
        } catch (JSONException e) {
            Log.d("SfvLug", "Invalid JSON Object: " + text);
            return null;
        }
    }

    /* Read URL data as lines of text
     *   Android provides several HTTP/URL libraries, like the JSON library, we
     *   arbitrarily picked HttpURLConnection.
     *
     *   Personally, I like to use libraries that are not Android specific
     *   because it makes my code more reusable in other java applications.
     *
     *   We also keep the URL library internal to this function so that we can
     *   easily change it if we ever want to. */
    private List<String> readUrlLines(String location)
    {
        LinkedList<String> lines = new LinkedList<String>();

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

        return lines;
    }

    /* Display the coffee status to the user */
    private void setStatus(CoffeeStatus status)
    {
        DateFormat fmt = DateFormat.getDateTimeInstance();
        int        yes = android.R.drawable.star_on;
        int        no  = android.R.drawable.star_off;

        // Update graphical display 
        //   this provides a nice, readable representation of the coffee
        //   status, but it does not support showing error messages so we only
        //   update it if there have been no errors.
        if (status.error == null) {
            this.potIn.setImageResource(status.potIn ? yes : no);
            this.potActivity.setText(fmt.format(status.potActivity));
            this.lidOpen.setImageResource(status.lidOpen ? yes : no);
            this.lidActivity.setText(fmt.format(status.lidActivity));
        }

        // Set text view contents
        //   normally this shows the same information as the graphical display,
        //   but if there are errors, we will display them here.
        statusView.setTypeface(Typeface.MONOSPACE);
        statusView.setText(status.toString());
    }

    /* Trigger a refresh of the coffee pot status */
    private void loadStatus()
    {
        // Lookup URL in the user preferences, COFFEE_JSON is the default URL
        String location = this.settings.getString("coffee_json", this.COFFEE_JSON);

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
        //   1. String       - argument for doInBackground, from .execute()
        //   2. Void         - not used here, normally used for progress bars
        //   3. CoffeeStatus - the return type from doInBackground which is
        //                     passed to onPostExecute function.
        new AsyncTask<String, Void, CoffeeStatus>() {

            // Called from a background thread, so we don't block the user
            // interface. Using AsyncTask synchronization is handled for us.
            protected CoffeeStatus doInBackground(String... args) {
                // Java passes this as a variable argument array, but we only
                // use the first entry.
                String       location = args[0];
                JSONObject   json     = SfvLug.this.readUrlObject(location);
                CoffeeStatus status   = new CoffeeStatus(json);
                return status;
            }

            // Called once in the main thread once doInBackground finishes.
            // This is executed in the Main thread once again so that we can
            // update the user interface.
            protected void onPostExecute(CoffeeStatus status) {
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

            // reload the coffee pot status
            case R.id.refresh:
                this.loadStatus();
                return true;

            // open the user settings list
            //   In Android, activities are started using Intents rather than
            //   open directly. Here we use an "explicit" Intent by specifying
            //   the specific  class (Settings.class) that we want Android to
            //   start for us.
            //
            //   We can also use an "implicit" Intents if we don't know the
            //   class name, such as if we wanted to open a PDF file. In that
            //   case Android would search for an App that can handle PDF files
            //   and deliver the Intent to that App.
            case R.id.settings:
                Intent intent = new Intent(this, Settings.class);
                this.startActivity(intent);
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

        // Set the default preferences values our XML settings file
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // Grab the settings (containing the server URLs)
        this.settings    = PreferenceManager.getDefaultSharedPreferences(this);

        // Lookup all our user interface widgets
        this.statusView  =  (TextView)findViewById(R.id.status);
        this.potIn       = (ImageView)findViewById(R.id.pot_in);
        this.potActivity =  (TextView)findViewById(R.id.pot_activity);
        this.lidOpen     = (ImageView)findViewById(R.id.lid_open);
        this.lidActivity =  (TextView)findViewById(R.id.lid_activity);

        // Trigger the initial status update
        this.loadStatus();
    }
}
