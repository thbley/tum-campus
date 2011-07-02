package de.tum.in.tumcampus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.tum.in.tumcampus.models.TransportManager;

public class Transports extends Activity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.transports_horizontal);
		} else {
			setContentView(R.layout.transports);
		}

		TransportManager tm = new TransportManager(this, "database.db");
		Cursor c = tm.getAllFromDb();

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		tm.close();
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String location = c.getString(c.getColumnIndex("_id"));

		TextView tv = (TextView) findViewById(R.id.transportText);
		tv.setText("Abfahrtszeiten: " + location);

		TransportManager tm = new TransportManager(this, "database.db");

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();

			if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
				throw new Exception("<Keine Internetverbindung>");
			}
			list = tm.getFromExternal(location);

		} catch (Exception e) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("desc", e.getMessage());
			list.add(map);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				android.R.layout.two_line_list_item, new String[] { "desc",
						"name" }, new int[] { android.R.id.text1,
						android.R.id.text2 }) {
			public boolean isEnabled(int position) {
				return false;
			}
		};

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		tm.close();
	}
}