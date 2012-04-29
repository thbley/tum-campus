package de.tum.in.tumcampus;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.LocationManager;
import de.tum.in.tumcampus.models.Utils;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Activity to show cafeterias and meals selected by date
 */
public class Cafeterias extends Activity implements OnItemClickListener {

	/**
	 * Current Date selected (ISO format)
	 */
	private static String date;

	/**
	 * Current Date selected (German format)
	 */
	private static String dateStr;

	/**
	 * Current Cafeteria selected
	 */
	private String cafeteriaId;

	/**
	 * Current Cafeteria name selected
	 */
	private String cafeteriaName;

	/**
	 * Footer with opening hours
	 */
	View footer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// default date: today or next monday if today is weekend
		if (date == null) {
			Calendar calendar = Calendar.getInstance();
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SATURDAY) {
				calendar.add(Calendar.DATE, 2);
			}
			if (dayOfWeek == Calendar.SUNDAY) {
				calendar.add(Calendar.DATE, 1);
			}
			date = Utils.getDateString(calendar.getTime());
			dateStr = Utils.getDateStringDe(calendar.getTime());
		}

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.cafeterias_horizontal);
		} else {
			setContentView(R.layout.cafeterias);
		}

		// initialize listview footer for opening hours
		footer = getLayoutInflater().inflate(
				android.R.layout.two_line_list_item, null, false);

		ListView lv3 = (ListView) findViewById(R.id.listView3);
		lv3.addFooterView(footer);

		// get toast feedback and resume activity
		registerReceiver(DownloadService.receiver, new IntentFilter(
				DownloadService.broadcast));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(DownloadService.receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get cafeteria list, filtered by user-defined substring
		String filter = Utils.getSetting(this, Const.Settings.cafeteriaFilter);

		CafeteriaManager cm = new CafeteriaManager(this, Const.db);
		Cursor c2 = cm.getAllFromDb("%" + filter + "%");

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 });

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		lv2.setOnItemClickListener(this);

		// get all (distinct) dates having menus available
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
		Cursor c = cmm.getDatesFromDb();

		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		// reset new items counter
		CafeteriaMenuManager.lastInserted = 0;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
		if (sd.isOpened()) {
			sd.animateClose();
		}

		// click on date
		if (av.getId() == R.id.listView) {
			ListView lv = (ListView) findViewById(R.id.listView);
			Cursor c = (Cursor) lv.getAdapter().getItem(position);
			date = c.getString(c.getColumnIndex("_id"));
			dateStr = c.getString(c.getColumnIndex("date_de"));
		}

		// click on cafeteria
		if (av.getId() == R.id.listView2) {
			ListView lv2 = (ListView) findViewById(R.id.listView2);
			Cursor c = (Cursor) lv2.getAdapter().getItem(position);

			cafeteriaId = c.getString(c.getColumnIndex("_id"));
			cafeteriaName = c.getString(c.getColumnIndex("name"));
		}

		// get menus filtered by cafeteria and date
		if (cafeteriaId != null && date != null) {
			TextView tv = (TextView) findViewById(R.id.cafeteriaText);
			tv.setText(cafeteriaName + ": " + dateStr);

			// opening hours
			LocationManager lm = new LocationManager(this, Const.db);
			tv = (TextView) footer.findViewById(android.R.id.text2);
			tv.setText(lm.getHoursById(cafeteriaId));

			// menus
			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
			Cursor c = cmm.getTypeNameFromDb(cafeteriaId, date);

			TextView tv3 = (TextView) footer.findViewById(android.R.id.text1);
			if (c.getCount() == 0) {
				tv3.setText("Öffnungszeiten");
			} else {
				tv3.setText("Ausgabezeiten");
			}

			// no onclick for items, no separator line
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
					android.R.layout.two_line_list_item, c, c.getColumnNames(),
					new int[] { android.R.id.text1, android.R.id.text2 }) {

				@Override
				public boolean areAllItemsEnabled() {
					return false;
				}

				@Override
				public boolean isEnabled(int position) {
					return false;
				}
			};

			ListView lv3 = (ListView) findViewById(R.id.listView3);
			lv3.setAdapter(adapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem m = menu.add(0, Menu.FIRST, 0, "Aktualisieren");
		m.setIcon(R.drawable.ic_menu_refresh);

		m = menu.add(0, Menu.FIRST + 1, 0, "Einstellungen");
		m.setIcon(android.R.drawable.ic_menu_preferences);

		m = menu.add(0, Menu.FIRST + 2, 0, "Preise");
		m.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// option menu for refresh, settings and external links
		switch (item.getItemId()) {
		case Menu.FIRST:
			// download latest cafeterias and menus
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra("action", "cafeterias");
			startService(service);
			return true;

		case Menu.FIRST + 1:
			startActivity(new Intent(this, Settings.class));
			return true;

		case Menu.FIRST + 2:
			String url3 = "http://www.studentenwerk-muenchen.de/mensa/unsere-preise/";
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url3)));
			return true;

		case Menu.FIRST + 3:
			String url = "http://www.studentenwerk-muenchen.de/mensa/unsere-mensen-und-cafeterien/garching/";
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			return true;

		case Menu.FIRST + 4:
			String url2 = "http://www.studentenwerk-muenchen.de/mensa/unsere-mensen-und-cafeterien/muenchen/";
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url2)));
			return true;
		}
		return false;
	}
}