package de.tum.in.tumcampus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;

public class TumCampus extends Activity implements OnItemClickListener,
		View.OnClickListener {

	private final int CLEAR_CACHE = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		addItem(list, R.drawable.icon, "Vorlesungen", new Intent(this,
				Lectures.class));

		addItem(list, R.drawable.icon, "Speisepläne", new Intent(this,
				Cafeterias.class));

		addItem(list, R.drawable.icon, "MVV",
				new Intent(this, Transports.class));

		addItem(list, R.drawable.icon, "Nachrichten", new Intent(this,
				News.class));

		addItem(list, R.drawable.icon, "Veranstaltungen", new Intent(this,
				Events.class));

		addItem(list, R.drawable.icon, "Links", new Intent(this, Links.class));

		SimpleAdapter notes = new SimpleAdapter(this, list,
				R.layout.main_listview, new String[] { "icon", "name",
						"content" }, new int[] { R.id.icon, R.id.name });

		ListView lv = (ListView) findViewById(R.id.listViewMain);
		lv.setAdapter(notes);
		lv.setOnItemClickListener(this);

		Button b = (Button) findViewById(R.id.refresh);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugCafeterias);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugCafeteriasMenus);
		b.setOnClickListener(this);

		IntentFilter intentFilter = new IntentFilter(
				"de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD");
		getApplicationContext().registerReceiver(receiver, intentFilter);
	}

	public void DebugSQL(String query) {
		DebugReset();
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				this.getDatabasePath("database.db").toString(), null,
				SQLiteDatabase.OPEN_READONLY);

		Cursor c = db.rawQuery(query, null);
		while (c.moveToNext()) {
			for (int i = 0; i < c.getColumnCount(); i++) {
				Debug(c.getColumnName(i) + ": " + c.getString(i));
			}
			Debug("");
		}
		c.close();
		db.close();
	}

	public void Debug(Exception e) {
		Debug(e.getMessage());

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		Debug(sw.toString());
	}

	public void DebugReset() {
		TextView tv = (TextView) findViewById(R.id.debug);
		tv.setText("");
	}

	public void Debug(String s) {
		TextView tv = (TextView) findViewById(R.id.debug);
		tv.setMovementMethod(new ScrollingMovementMethod());
		tv.append(s + "\n");

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		if (!sd.isOpened()) {
			sd.animateOpen();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ListView lv = (ListView) findViewById(R.id.listViewMain);
		ListAdapter adapter = lv.getAdapter();

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) adapter
				.getItem(position);

		Intent itemIntent = (Intent) map.get("intent");
		startActivity(itemIntent);
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
		menu.add(0, CLEAR_CACHE, 0, "Cache leeren");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAR_CACHE:

			CafeteriaManager cm = new CafeteriaManager(this, "database.db");
			cm.deleteAllFromDb();
			cm.close();

			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
					"database.db");
			cmm.deleteAllFromDb();
			cmm.close();

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
				b.setText("Aktualisieren");
			} else {
				startService(service);
				b.setText("Abbrechen");
			}
		}

		if (v.getId() == R.id.debugCafeterias) {
			DebugSQL("SELECT * FROM cafeterias ORDER BY id");
		}

		if (v.getId() == R.id.debugCafeteriasMenus) {
			DebugSQL("SELECT * FROM cafeterias_menus ORDER BY id");
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
					b.setText("Aktualisieren");
				}
				if (message.length() > 0) {
					TextView tv = (TextView) findViewById(R.id.hello);
					tv.setText(message);
				}
			}
		}
	};

}