package de.tum.in.tumcampus;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity to show information about authors, copyright and support
 */
public class AppInfo extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appinfo);
	}
}