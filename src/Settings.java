package edu.ucla.linux.tutorial;

import android.os.Bundle;
import android.preference.PreferenceActivity;

// The PreferenceActivity does almost everything for us.
//
// All we need to do is is pass the XML settings description
// using addPreferencesFromResource.
//
// Note that in recent version of Android PreferenceActivity
// has been deprecated in favor of fragments.
public class Settings extends PreferenceActivity
{
	@Override
        @SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
