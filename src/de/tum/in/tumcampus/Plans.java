package de.tum.in.tumcampus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SlidingDrawer;

/**
 * Activity to show plans
 */
public class Plans extends Activity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plans);

		String[] plans = new String[] { "Campus Garching",
				"MVV-Schnellbahnnetz", "MVV-Nachtlinien" };

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, plans));
		lv.setOnItemClickListener(this);

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
		sd.open();
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int position,
			long id) {
		WebView browser = (WebView) findViewById(R.id.webView);
		// activate zoom controls
		browser.getSettings().setBuiltInZoomControls(true);
		// activate double tab to zoom
		browser.getSettings().setUseWideViewPort(true);

		// draw image from assets directory in webview
		String file = "";
		if (position == 0) {
			file = "plans/CampusGarching.jpg";
			setTitle("Plan: Campus Garching");
			browser.setInitialScale(100 * view.getWidth() / 1024);

		} else if (position == 1) {
			file = "plans/mvv.jpg";
			setTitle("Plan: MVV-Schnellbahnnetz");
			browser.setInitialScale(100 * view.getWidth() / 1100);

		} else {
			file = "plans/mvv_night.jpg";
			setTitle("Plan: MVV-Nachtlinien");
			browser.setInitialScale(100 * view.getWidth() / 1485);
		}

		String data = "<body style='margin:0px;'><img src='" + file + "'/>"
				+ "</body>";
		browser.loadDataWithBaseURL("file:///android_asset/", data,
				"text/html", "UTF-8", null);
		browser.forceLayout();

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
		sd.animateClose();
	}
}