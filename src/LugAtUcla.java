package edu.ucla.linux.tutorial;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;

/*
 * Our Main Activity (the user interface). This is launched when the user
 * starts our App. Activity extends Context, so we can access all out
 * application data from within this class using functions such as
 * findViewById.
 */
public class LugAtUcla extends Activity
{
    /* Activity Widgets */
    private TextView  statusView;  // Text view for dumping ASCII status
                                   // This also shows error messages

    private ImageView potIn;       // Star icon for pos status
    private TextView  potActivity; // Pot activity timestamp field
    private ImageView lidOpen;     // Star icon for lid status
    private TextView  lidActivity; // Lid activity timestamp field

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
        // Notify the user that we're loading
        statusView.setText("Loading..");

        // Update status with dummy data
        CoffeeStatus status = new CoffeeStatus();
        status.potIn   = Math.random() > 0.5;
        status.lidOpen = Math.random() > 0.5;
        this.setStatus(status);
        Toast.makeText(LugAtUcla.this, "Load complete", Toast.LENGTH_SHORT).show();
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
        this.potIn       = (ImageView)findViewById(R.id.pot_in);
        this.potActivity =  (TextView)findViewById(R.id.pot_activity);
        this.lidOpen     = (ImageView)findViewById(R.id.lid_open);
        this.lidActivity =  (TextView)findViewById(R.id.lid_activity);

        // Trigger the initial status update
        this.loadStatus();
    }
}
