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

public class TumCampus extends Activity implements OnItemClickListener,
		View.OnClickListener {

	static boolean syncing = false;

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

		Button b = (Button) findViewById(R.id.refresh);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.initial);
		b.setOnClickListener(this);

		FeedItemManager fim = new FeedItemManager(this, Const.db);
		if (fim.empty()) {
			b.setVisibility(View.VISIBLE);
		} else {
			b.setVisibility(View.GONE);
		}
		fim.close();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.broadcast);
		intentFilter.addAction(DownloadService.broadcast);
		registerReceiver(receiver, intentFilter);
		setImportButtons(true);

		Intent service = new Intent(this, ImportService.class);
		service.putExtra("action", "defaults");
		startService(service);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	};

	@Override
	protected void onResume() {
		super.onResume();

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

		if (conn.length() > 0) {
			b.setVisibility(android.view.View.VISIBLE);
			if (!syncing) {
				b.setText("Aktualisieren (" + conn + ")");
				tv.setText(getString(R.string.hello));
			} else {
				b.setText("Abbrechen");
			}
		} else {
			b.setVisibility(android.view.View.GONE);
			tv.setText(getString(R.string.hello) + " Offline.");
		}

		Intent service = new Intent(this, SilenceService.class);
		startService(service);
	}

	public List<Map<String, Object>> buildMenu() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

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

		if (Utils.getSettingBool(this, Const.settings.debug)) {
			addItem(list, R.drawable.icon, "Debug", false, new Intent(this,
					Debug.class));
		}
		return list;
	}

	private void addItem(List<Map<String, Object>> data, int icon, String name,
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
		data.add(map);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View view, int position, long id) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) av.getAdapter()
				.getItem(position);

		Intent intent = (Intent) map.get("intent");
		startActivity(intent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, "Einstellungen");
		menu.add(0, Menu.FIRST + 1, 0, "Cache leeren");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
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

		SyncManager sm = new SyncManager(this, Const.db);
		sm.deleteFromDb();
		sm.close();
	}

	@Override
	public void onClick(View v) {

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
		if (v.getId() == R.id.importLectures) {
			Intent service = new Intent(this, ImportService.class);
			service.putExtra("action", "lectures");
			startService(service);
			setImportButtons(false);
		}
		if (v.getId() == R.id.importLinks) {
			Intent service = new Intent(this, ImportService.class);
			service.putExtra("action", "links");
			startService(service);
			setImportButtons(false);
		}
		if (v.getId() == R.id.importFeeds) {
			Intent service = new Intent(this, ImportService.class);
			service.putExtra("action", "feeds");
			startService(service);
			setImportButtons(false);
		}
	}

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

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

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