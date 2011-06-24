package de.tum.in.tumcampus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.SlidingDrawer;

public class News extends Activity implements OnItemClickListener {

	SQLiteDatabase db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		sd.open();

		db = SQLiteDatabase.openDatabase(this.getDatabasePath("database.db")
				.toString(), null, SQLiteDatabase.OPEN_READONLY);

		Cursor c = db.rawQuery("SELECT DISTINCT name, feedUrl, id as _id "
				+ "FROM news ORDER BY name", null);

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		// TODO destroy db, cursor, move to manager
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		if (sd.isOpened()) {
			sd.animateClose();
		}

		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String feedId = c.getString(c.getColumnIndex("_id"));
		String name = c.getString(c.getColumnIndex("name"));

		setTitle("Nachrichten: "+ name);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		// TODO move to manager

		Cursor c2 = db.rawQuery("SELECT image, title, id as _id "
				+ "FROM news_items " + "WHERE feedId = ? "
				+ "ORDER BY date DESC", new String[] { feedId });

		while (c2.moveToNext()) {
			Map<String, Object> map = new HashMap<String, Object>();

			String image = c2.getString(0);

			if (image.length() == 0) {
				image = String.valueOf(R.drawable.icon);
			}

			// TODO check file exists, is readable?
			map.put("image", image);
			map.put("title", c2.getString(1));
			map.put("id", c2.getString(2));
			list.add(map);
		}
		c2.close();

		boolean showImages = true;
		if (list.size() > 0) {
			showImages = !list.get(0).get("image")
					.equals(String.valueOf(R.drawable.icon));
		}

		SimpleAdapter adapter;
		if (showImages) {
			adapter = new SimpleAdapter(this, list, R.layout.news_listview,
					new String[] { "image", "title", "id" }, new int[] {
							R.id.icon, R.id.name });
		} else {
			adapter = new SimpleAdapter(this, list,
					R.layout.news_listview_text,
					new String[] { "title", "id" }, new int[] { R.id.name });
		}

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);

		// TODO implement
		// lv2.setOnItemClickListener(this);
	}

}
