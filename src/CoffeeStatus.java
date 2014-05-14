package edu.ucla.linux.tutorial;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * CoffeeStatus represents the information retrieved from the Coffee API.
 */
public class CoffeeStatus
{
    // If we get a parser error we set it in this string so that the user knows
    // about the error.
    public String   error       = null;

    public boolean  potIn       = false;      // Is the coffee pot currently in the Coffee maker?
    public Date     potActivity = new Date(); // Last time the coffee pot was moved
    public boolean  lidOpen     = false;      // Is the coffee pot lid currently open?
    public Date     lidActivity = new Date(); // Last time the lid was opened or closed

    // Convenience function to format coffee pot status as a string.
    public String toString() {
        // If there was an error just output error status,
        // the rest of the object is probably invalid.
        if (this.error != null)
            return this.error;

        // Get the users preferred time format.
        DateFormat fmt = DateFormat.getDateTimeInstance();

        String potStat = this.potIn   ? "In"   : "Out";
        String lidStat = this.lidOpen ? "Open" : "Closed";
        String potTime = fmt.format(this.potActivity);
        String lidTime = fmt.format(this.lidActivity);

        return
            String.format("Pot %-6s at %s\n", potStat, potTime) +
            String.format("Lid %-6s at %s\n", lidStat, lidTime);
    }
}
