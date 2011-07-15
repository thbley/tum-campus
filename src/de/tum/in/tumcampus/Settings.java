package de.tum.in.tumcampus;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Activity to show the settings dialog
 */
public class Settings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}