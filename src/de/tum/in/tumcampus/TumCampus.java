package de.tum.in.tumcampus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
import de.tum.in.tumcampus.models.GalleryManager;
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
public class TumCampus extends Activity implements OnItemClickListener, View.OnClickListener {

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
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);

		// adjust logo width to screen width
		ImageView iv = (ImageView) findViewById(R.id.logo);
		iv.getLayoutParams().width = getWindowManager().getDefaultDisplay().getWidth();

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

		// open import if required
		String s = getIntent().getAction();
		if (s != null && s.equals("import")) {
			SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
			sd.animateOpen();
		}

		// register receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.broadcast);
		intentFilter.addAction(DownloadService.broadcast);
		registerReceiver(receiver, intentFilter);

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
		SimpleAdapter adapter = new SimpleAdapter(this, buildMenu(), R.layout.main_listview, new String[] { "icon",
				"name", "icon2" }, new int[] { R.id.icon, R.id.name, R.id.icon2 });

		ListView lv = (ListView) findViewById(R.id.menu);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		String conn = getConnection();
		Button b = (Button) findViewById(R.id.refresh);

		/**
		 * <pre>
		 * disable download button if offline
		 * show cancel button if currently syncing
		 * else show download button
		 * </pre>
		 */
		if (conn.length() > 0) {
			if (!syncing) {
				b.setText("Aktualisieren (" + conn + ")");
				b.setEnabled(true);
			} else {
				b.setText("Abbrechen");

				// hide initial download button when syncing
				b = (Button) findViewById(R.id.initial);
				b.setVisibility(View.GONE);
			}
		} else {
			b.setText("offline.");
			b.setEnabled(false);
		}

		// initialize import buttons
		setImportButtons(true);

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
		if (Utils.getSettingBool(this, "lectures")) {
			addItem(list, R.drawable.vorlesung, "Vorlesungen", LectureItemManager.lastInserted > 0, new Intent(this,
					Lectures.class));
		}
		if (Utils.getSettingBool(this, "cafeterias")) {
			addItem(list, R.drawable.essen, "Speisepläne", CafeteriaMenuManager.lastInserted > 0, new Intent(this,
					Cafeterias.class));
		}
		if (Utils.getSettingBool(this, "transports")) {
			addItem(list, R.drawable.zug, "MVV", false, new Intent(this, Transports.class));
		}
		if (Utils.getSettingBool(this, "feeds")) {
			int count = FeedItemManager.lastInserted + FeedManager.lastInserted;
			addItem(list, R.drawable.rss, "RSS-Feeds", count > 0, new Intent(this, Feeds.class));
		}
		if (Utils.getSettingBool(this, "events")) {
			addItem(list, R.drawable.party, "Veranstaltungen", EventManager.lastInserted > 0, new Intent(this,
					Events.class));
		}
		if (Utils.getSettingBool(this, "gallery")) {
			addItem(list, R.drawable.gallery, "Kurz notiert", false, new Intent(this, Gallery.class));
		}
		if (Utils.getSettingBool(this, "news")) {
			addItem(list, R.drawable.globus, "Nachrichten", NewsManager.lastInserted > 0, new Intent(this, News.class));
		}
		if (Utils.getSettingBool(this, "plans")) {
			addItem(list, R.drawable.kompass, "Umgebungspläne", false, new Intent(this, Plans.class));
		}
		if (Utils.getSettingBool(this, "hours")) {
			addItem(list, R.drawable.hours, "Öffnungszeiten", false, new Intent(this, Hours.class));
		}
		if (Utils.getSettingBool(this, "links")) {
			addItem(list, R.drawable.www, "Links", LinkManager.lastInserted > 0, new Intent(this, Links.class));
		}
		if (Utils.getSettingBool(this, "facebook")) {
			String url = "https://m.facebook.com/TUMCampus";
			addItem(list, R.drawable.fb, "Facebook", false, new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}

		if (Utils.getSettingBool(this, Const.Settings.debug)) {
			addItem(list, R.drawable.icon, "Debug", false, new Intent(this, Debug.class));
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
	public void addItem(List<Map<String, Object>> list, int icon, String name, boolean changed, Intent intent) {
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
		Map<String, Object> map = (Map<String, Object>) av.getAdapter().getItem(position);

		Intent intent = (Intent) map.get("intent");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem m = menu.add(0, Menu.FIRST, 0, "App-Info");
		m.setIcon(android.R.drawable.ic_menu_info_details);

		m = menu.add(0, Menu.FIRST + 1, 0, "Einstellungen");
		m.setIcon(android.R.drawable.ic_menu_preferences);

		m = menu.add(0, Menu.FIRST + 2, 0, "Handbuch");
		m.setIcon(android.R.drawable.ic_menu_agenda);

		m = menu.add(0, Menu.FIRST + 3, 0, "Cache leeren");
		m.setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// open settings activity, clear cache (database tables, sd-card)
		switch (item.getItemId()) {
		case Menu.FIRST:
			startActivity(new Intent(this, AppInfo.class));
			return true;

		case Menu.FIRST + 1:
			startActivity(new Intent(this, Settings.class));
			return true;

		case Menu.FIRST + 2:
			try {
				// copy pdf manual from assets to sd-card
				String target = Utils.getCacheDir("cache") + "TUM Campus Handbuch.pdf";

				InputStream in = getAssets().open("manual.pdf");
				OutputStream out = new FileOutputStream(target);

				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				in.close();
				out.close();

				// open pdf manual
				Uri uri = Uri.fromFile(new File(target));
				Intent intent2 = new Intent(Intent.ACTION_VIEW);
				intent2.setDataAndType(uri, "application/pdf");
				intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent2);
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			return true;

		case Menu.FIRST + 3:
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

		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
		cmm.removeCache();

		FeedItemManager fim = new FeedItemManager(this, Const.db);
		fim.removeCache();

		EventManager em = new EventManager(this, Const.db);
		em.removeCache();

		GalleryManager gm = new GalleryManager(this, Const.db);
		gm.removeCache();

		LinkManager lm = new LinkManager(this, Const.db);
		lm.removeCache();

		NewsManager nm = new NewsManager(this, Const.db);
		nm.removeCache();

		// table of all download events
		SyncManager sm = new SyncManager(this, Const.db);
		sm.deleteFromDb();
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

		View n = findViewById(R.id.noteLectures1);
		View n2 = findViewById(R.id.noteLectures2);
		View v = findViewById(R.id.importLectures);
		v.setOnClickListener(this);
		v.setEnabled(enabled);
		if (!Utils.getSettingBool(this, "lectures")) {
			v.setVisibility(View.GONE);
			n.setVisibility(View.GONE);
			n2.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
			n.setVisibility(View.VISIBLE);
			n2.setVisibility(View.VISIBLE);
		}

		n = findViewById(R.id.noteFeeds);
		v = findViewById(R.id.importFeeds);
		v.setOnClickListener(this);
		v.setEnabled(enabled);
		if (!Utils.getSettingBool(this, "feeds")) {
			v.setVisibility(View.GONE);
			n.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
			n.setVisibility(View.VISIBLE);
		}

		n = findViewById(R.id.noteLinks);
		v = findViewById(R.id.importLinks);
		v.setOnClickListener(this);
		v.setEnabled(enabled);
		if (!Utils.getSettingBool(this, "links")) {
			v.setVisibility(View.GONE);
			n.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
			n.setVisibility(View.VISIBLE);
		}

		n = findViewById(R.id.noteModules);
		if (!Utils.getSettingBool(this, "lectures") && !Utils.getSettingBool(this, "feeds")
				&& !Utils.getSettingBool(this, "links")) {
			n.setVisibility(View.VISIBLE);
		} else {
			n.setVisibility(View.GONE);
		}
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
					TextView tv = (TextView) findViewById(R.id.status);
					tv.setVisibility(View.VISIBLE);
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