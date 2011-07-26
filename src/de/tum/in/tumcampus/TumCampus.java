package de.tum.in.tumcampus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.EventManager;
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.FeedManager;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.models.SyncManager;
import de.tum.in.tumcampus.models.Utils;
import de.tum.in.tumcampus.services.DownloadService;
import de.tum.in.tumcampus.services.ImportService;
import de.tum.in.tumcampus.services.SilenceService;

/**
 * Main activity to show main menu, logo and refresh button
 */
public class TumCampus extends Activity implements OnItemClickListener,
		View.OnClickListener {

	static boolean syncing = false;

	/**
	 * Returns network connection type if available or can be available soon
	 * 
	 * @return empty String if not available or connection type if available
	 */
	public String getConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			String connection = "";
			if (netInfo.getSubtypeName().length() > 0) {
				connection += netInfo.getSubtypeName();
			} else {
				connection += netInfo.getTypeName();
			}
			if (netInfo.isRoaming()) {
				connection += " roaming";
			}
			return connection;
		}
		return "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// bind download buttons
		Button b = (Button) findViewById(R.id.refresh);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.initial);
		b.setOnClickListener(this);

		// show initial download button if feed items are empty
		FeedItemManager fim = new FeedItemManager(this, Const.db);
		if (fim.empty()) {
			b.setVisibility(View.VISIBLE);
		} else {
			b.setVisibility(View.GONE);
		}
		fim.close();

		// register receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.broadcast);
		intentFilter.addAction(DownloadService.broadcast);
		registerReceiver(receiver, intentFilter);

		// initialize import buttons
		setImportButtons(true);

		// import default values into database
		Intent service = new Intent(this, ImportService.class);
		service.putExtra("action", "defaults");
		startService(service);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// build main menu
		SimpleAdapter adapter = new SimpleAdapter(this, buildMenu(),
				R.layout.main_listview,
				new String[] { "icon", "name", "icon2" }, new int[] {
						R.id.icon, R.id.name, R.id.icon2 });

		ListView lv = (ListView) findViewById(R.id.menu);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		String conn = getConnection();
		Button b = (Button) findViewById(R.id.refresh);
		TextView tv = (TextView) findViewById(R.id.hello);

		/**
		 * <pre>
		 * hide download button if offline
		 * show cancel button if currently syncing
		 * else show download button
		 * </pre>
		 */
		if (conn.length() > 0) {
			b.setVisibility(android.view.View.VISIBLE);
			if (!syncing) {
				b.setText("Aktualisieren (" + conn + ")");

				// reset text if offline message is still there
				if (tv.getTag() != null) {
					tv.setText(getString(R.string.hello));
					tv.setTag(null);
				}
			} else {
				b.setText("Abbrechen");

				// hide initial download button when syncing
				b = (Button) findViewById(R.id.initial);
				b.setVisibility(View.GONE);
			}
		} else {
			b.setVisibility(android.view.View.GONE);
			tv.setText(getString(R.string.hello) + " Offline.");
			tv.setTag("offline");
		}

		// start silence service
		Intent service = new Intent(this, SilenceService.class);
		startService(service);
	}

	/**
	 * Return main menu item list
	 * 
	 * @return item list of Map[] (icon, name, icon2, intent)
	 */
	public List<Map<String, Object>> buildMenu() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		// build list, intent = start activity on click
		addItem(list, R.drawable.vorlesung, "Vorlesungen",
				LectureItemManager.lastInserted > 0, new Intent(this,
						Lectures.class));

		addItem(list, R.drawable.essen, "Speisepläne",
				CafeteriaMenuManager.lastInserted > 0, new Intent(this,
						Cafeterias.class));

		addItem(list, R.drawable.zug, "MVV", false, new Intent(this,
				Transports.class));

		addItem(list, R.drawable.rss, "RSS-Feeds", FeedItemManager.lastInserted
				+ FeedManager.lastInserted > 0, new Intent(this, Feeds.class));

		addItem(list, R.drawable.party, "Veranstaltungen",
				EventManager.lastInserted > 0, new Intent(this, Events.class));

		addItem(list, R.drawable.globus, "Nachrichten",
				NewsManager.lastInserted > 0, new Intent(this, News.class));

		addItem(list, R.drawable.www, "Links", LinkManager.lastInserted > 0,
				new Intent(this, Links.class));

		addItem(list, R.drawable.info, "App-Info", false, new Intent(this,
				AppInfo.class));

		if (Utils.getSettingBool(this, Const.Settings.debug)) {
			addItem(list, R.drawable.icon, "Debug", false, new Intent(this,
					Debug.class));
		}
		return list;
	}

	/**
	 * Add menu item to list
	 * 
	 * <pre>
	 * @param list List to append new item to
	 * @param icon Icon ID
	 * @param name Menu item name
	 * @param changed Menu item was changed recently
	 * @param intent Activity to start on click
	 * </pre>
	 */
	public void addItem(List<Map<String, Object>> list, int icon, String name,
			boolean changed, Intent intent) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("icon", icon);
		map.put("name", name);
		int icon2 = android.R.color.transparent;
		if (changed) {
			icon2 = android.R.drawable.star_off;
		}
		map.put("icon2", icon2);
		map.put("intent", intent);
		list.add(map);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View view, int position, long id) {

		// start activity on main menu item click
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) av.getAdapter()
				.getItem(position);

		Intent intent = (Intent) map.get("intent");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem m = menu.add(0, Menu.FIRST, 0, "Einstellungen");
		m.setIcon(android.R.drawable.ic_menu_preferences);

		m = menu.add(0, Menu.FIRST + 1, 0, "Cache leeren");
		m.setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// open settings activity, clear cache (database tables, sd-card)
		switch (item.getItemId()) {
		case Menu.FIRST:
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			return true;

		case Menu.FIRST + 1:
			clearCache();
			return true;
		}
		return false;
	}

	/**
	 * Clears the cache (database tables, sd-card)
	 */
	public void clearCache() {
		try {
			Utils.getCacheDir("");
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}

		CafeteriaManager cm = new CafeteriaManager(this, Const.db);
		cm.removeCache();
		cm.close();

		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
		cmm.removeCache();
		cmm.close();

		FeedItemManager fim = new FeedItemManager(this, Const.db);
		fim.removeCache();
		fim.close();

		EventManager em = new EventManager(this, Const.db);
		em.removeCache();
		em.close();

		LinkManager lm = new LinkManager(this, Const.db);
		lm.removeCache();
		lm.close();

		NewsManager nm = new NewsManager(this, Const.db);
		nm.removeCache();
		nm.close();

		// table of all download events
		SyncManager sm = new SyncManager(this, Const.db);
		sm.deleteFromDb();
		sm.close();
	}

	@Override
	public void onClick(View v) {

		// Click on download/cancel button, start/stop download service
		if (v.getId() == R.id.refresh || v.getId() == R.id.initial) {
			Intent service = new Intent(this, DownloadService.class);
			if (syncing) {
				stopService(service);
				syncing = false;
			} else {
				startService(service);
				syncing = true;
			}
			onResume();
		}

		// Click on import lectures, start import service
		if (v.getId() == R.id.importLectures) {
			Intent service = new Intent(this, ImportService.class);
			service.putExtra("action", "lectures");
			startService(service);
			setImportButtons(false);
		}

		// Click on import links, start import service
		if (v.getId() == R.id.importLinks) {
			Intent service = new Intent(this, ImportService.class);
			service.putExtra("action", "links");
			startService(service);
			setImportButtons(false);
		}

		// Click on import links, start import service
		if (v.getId() == R.id.importFeeds) {
			Intent service = new Intent(this, ImportService.class);
			service.putExtra("action", "feeds");
			startService(service);
			setImportButtons(false);
		}
	}

	/**
	 * Initialize import buttons
	 * 
	 * <pre>
	 * @param enabled True to enable buttons, False to disable buttons
	 * </pre>
	 */
	public void setImportButtons(boolean enabled) {
		Button b = (Button) findViewById(R.id.importLectures);
		b.setOnClickListener(this);
		b.setEnabled(enabled);

		b = (Button) findViewById(R.id.importFeeds);
		b.setOnClickListener(this);
		b.setEnabled(enabled);

		b = (Button) findViewById(R.id.importLinks);
		b.setOnClickListener(this);
		b.setEnabled(enabled);
	}

	/**
	 * Receiver for Download and Import services
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// show message from download service, refresh main menu
			if (intent.getAction().equals(DownloadService.broadcast)) {
				String message = intent.getStringExtra("message");
				String action = intent.getStringExtra("action");

				if (action.equals("completed")) {
					syncing = false;
				}
				if (message.length() > 0) {
					TextView tv = (TextView) findViewById(R.id.hello);
					tv.setText(message);
				}
				onResume();
			}

			// show message from import service, refresh main menu
			if (intent.getAction().equals(ImportService.broadcast)) {
				String message = intent.getStringExtra("message");
				String action = intent.getStringExtra("action");

				if (action.length() != 0) {
					Toast.makeText(context, message, Toast.LENGTH_LONG).show();
					setImportButtons(true);

					SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
					sd.animateClose();

					onResume();
				}
			}
		}
	};
}