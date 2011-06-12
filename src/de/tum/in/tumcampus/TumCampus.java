package de.tum.in.tumcampus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class TumCampus extends Activity implements OnItemClickListener {

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

		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setAdapter(notes);

		lv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ListView lv = (ListView) findViewById(R.id.listView1);
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
}