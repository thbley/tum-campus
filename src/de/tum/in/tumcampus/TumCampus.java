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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.TextView;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.EventManager;
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.models.SyncManager;
import de.tum.in.tumcampus.services.DownloadService;
import de.tum.in.tumcampus.services.ImportService;

public class TumCampus extends Activity implements OnItemClickListener,
		View.OnClickListener {

	private boolean debug = true;

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
	protected void onResume() {
		super.onResume();

		String conn = getConnection();

		Button b = (Button) findViewById(R.id.refresh);
		TextView tv = (TextView) findViewById(R.id.hello);

		if (conn.length() > 0) {
			b.setVisibility(android.view.View.VISIBLE);
			b.setText("Aktualisieren (" + conn + ")");
			tv.setText(getString(R.string.hello));

		} else {
			b.setVisibility(android.view.View.GONE);
			tv.setText(getString(R.string.hello) + " Offline.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// TODO optimize
		Intent service = new Intent(this, ImportService.class);
		startService(service);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		addItem(list, R.drawable.vorlesung, "Vorlesungen", new Intent(this,
				Lectures.class));

		addItem(list, R.drawable.essen, "Speisepläne", new Intent(this,
				Cafeterias.class));

		addItem(list, R.drawable.zug, "MVV", new Intent(this, Transports.class));

		addItem(list, R.drawable.rss, "RSS-Feeds",
				new Intent(this, Feeds.class));

		addItem(list, R.drawable.party, "Veranstaltungen", new Intent(this,
				Events.class));

		addItem(list, R.drawable.globus, "Nachrichten", new Intent(this,
				News.class));

		addItem(list, R.drawable.www, "Links", new Intent(this, Links.class));

		addItem(list, R.drawable.info, "App-Info", new Intent(this,
				AppInfo.class));

		if (debug) {
			addItem(list, R.drawable.icon, "Debug", new Intent(this,
					Debug.class));
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.main_listview, new String[] { "icon", "name",
						"content" }, new int[] { R.id.icon, R.id.name });

		ListView lv = (ListView) findViewById(R.id.listViewMain);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		Button b = (Button) findViewById(R.id.refresh);
		b.setOnClickListener(this);

		IntentFilter intentFilter = new IntentFilter(
				"de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD");
		getApplicationContext().registerReceiver(receiver, intentFilter);

		// TODO initial sync
		// TODO partial sync
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ListView lv = (ListView) findViewById(R.id.listViewMain);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) lv.getAdapter()
				.getItem(position);

		Intent intent = (Intent) map.get("intent");
		startActivity(intent);
	}

	private void addItem(List<Map<String, Object>> data, int icon, String name,
			Intent intent) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("icon", icon);
		map.put("name", name);
		map.put("intent", intent);
		data.add(map);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, "Cache leeren");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:

			SharedPreferences settings = getSharedPreferences("prefs",
					Context.MODE_PRIVATE);
			Editor e = settings.edit();
			e.clear();
			e.commit();

			// TODO check sd card readable
			// Utils.getCacheDir("");

			CafeteriaManager cm = new CafeteriaManager(this, "database.db");
			cm.removeCache();
			cm.close();

			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
					"database.db");
			cmm.removeCache();
			cmm.close();

			FeedItemManager fim = new FeedItemManager(this, "database.db");
			fim.removeCache();
			fim.close();

			EventManager em = new EventManager(this, "database.db");
			em.removeCache();
			em.close();

			LinkManager lm = new LinkManager(this, "database.db");
			lm.removeCache();
			lm.close();

			NewsManager nm = new NewsManager(this, "database.db");
			nm.removeCache();
			nm.close();

			SyncManager sm = new SyncManager(this, "database.db");
			sm.deleteFromDb();
			sm.close();

			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.refresh) {
			Intent service = new Intent(this, DownloadService.class);
			Button b = (Button) findViewById(R.id.refresh);

			if (b.getText().equals("Abbrechen")) {
				stopService(service);
				b.setText("Aktualisieren (" + getConnection() + ")");
			} else {
				startService(service);
				b.setText("Abbrechen");
			}
		}
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(
					"de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD")) {
				return;
			}
			Bundle extra = intent.getExtras();

			if (extra != null) {
				String message = extra.getString("message");
				String action = extra.getString("action");

				if (action.equals("completed")) {
					Button b = (Button) findViewById(R.id.refresh);
					b.setText("Aktualisieren (" + getConnection() + ")");
				}
				if (message.length() > 0) {
					TextView tv = (TextView) findViewById(R.id.hello);
					tv.setText(message);
				}
			}
		}
	};
}