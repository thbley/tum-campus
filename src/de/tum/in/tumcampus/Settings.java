package de.tum.in.tumcampus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Activity to show the settings dialog
 */
public class Settings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen ps, Preference pref) {
		String key = pref.getKey();

		// open application details
		if (key.equals("app_details")) {

			if (android.os.Build.VERSION.SDK_INT >= 9) {
				// 2.3 and newer
				Uri uri = Uri.parse("package:" + getPackageName());
				Intent intent = new Intent(
						"android.settings.APPLICATION_DETAILS_SETTINGS", uri);
				startActivity(intent);
			} else {
				// older Androids
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName("com.android.settings",
						"com.android.settings.InstalledAppDetails");
				intent.putExtra("com.android.settings.ApplicationPkgName",
						getPackageName());
				startActivity(intent);
			}
		}
		if (key.equals("market")) {
			String url = "http://market.android.com/details?id=de.tum.in.tumcampus";
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		return true;
	}
}