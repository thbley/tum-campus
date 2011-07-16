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
import de.tum.in.tumcampus.models.Utils;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Activity to show cafeterias and meals selected by date
 */
public class Cafeterias extends Activity implements OnItemClickListener {

	private String date;
	private String dateStr;
	private String cafeteriaId;
	private String cafeteriaName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// default date: today or next monday if today is weekend
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

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.cafeterias_horizontal);
		} else {
			setContentView(R.layout.cafeterias);
		}

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
		String filter = Utils.getSetting(this, Const.settings.cafeteriaFilter);

		CafeteriaManager cm = new CafeteriaManager(this, Const.db);
		Cursor c2 = cm.getAllFromDb("%" + filter + "%");

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 });

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		lv2.setOnItemClickListener(this);
		cm.close();

		// get all (distinct) dates having menus available
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
		Cursor c = cmm.getDatesFromDb();

		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		cmm.close();

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

			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
			Cursor c = cmm.getTypeNameFromDb(cafeteriaId, date);

			// no onclick for items, no separator line
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
					android.R.layout.two_line_list_item, c, c.getColumnNames(),
					new int[] { android.R.id.text1, android.R.id.text2 }) {

				public boolean areAllItemsEnabled() {
					return false;
				}

				public boolean isEnabled(int position) {
					return false;
				}
			};

			ListView lv3 = (ListView) findViewById(R.id.listView3);
			lv3.setAdapter(adapter);
			cmm.close();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, "Aktualisieren");
		menu.add(0, Menu.FIRST + 1, 0, "Einstellungen");
		menu.add(0, Menu.FIRST + 2, 0, "Preise");
		menu.add(0, Menu.FIRST + 3, 0, "Öffnungszeiten Garching");
		menu.add(0, Menu.FIRST + 4, 0, "Öffnungszeiten München");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		// option menu for refresh, settings and external links
		switch (item.getItemId()) {
		case Menu.FIRST:
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