package edu.ucla.linux.tutorial;

import android.app.Activity;
import android.os.Bundle;

/*
 * Our Main Activity (the user interface). This is launched when the user
 * starts our App. Activity extends Context, so we can access all out
 * application data from within this class using functions such as
 * findViewById.
 */
public class LugAtUcla extends Activity
{
    /* Initialize our app
     *   The "lifecycle" of an Android App is much more complex than this, but
     *   luckily we can do everything in onCreate because we don't have to save
     *   state when the user starts and stops the App. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the user interfaces from the XML UI description
        setContentView(R.layout.main);
    }
}
