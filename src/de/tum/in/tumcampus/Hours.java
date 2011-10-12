package de.tum.in.tumcampus;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import de.tum.in.tumcampus.models.LocationManager;

/**
 * Activity to show opening hours
 */
public class Hours extends Activity implements OnItemClickListener, ViewBinder {

	private String[] names = new String[] { "Bibliotheken", "Mensen",
			"Information" };

	private String[] categories = new String[] { "library", "cafeteria", "info" };

	private static int position = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hours);

		// show all categories
		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, names));
		lv.setOnItemClickListener(this);

		if (position == -1) {
			SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
			sd.open();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// refresh current selected plan on resume (rotate)
		if (position != -1) {
			onItemClick(null, null, position, 0);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		position = pos;

		// click on feed item in list, open URL in browser
		// if (av.getId() == R.id.listView2) {
		// TODO implement URL?
		/*
		 * Cursor c = (Cursor) av.getAdapter().getItem(position); String link =
		 * c.getString(c.getColumnIndex("link"));
		 * 
		 * Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		 * startActivity(intent); return;
		 */
		// }

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
		if (sd.isOpened()) {
			sd.animateClose();
		}

		setTitle("Öffnungszeiten: " + names[position]);

		// click on category in list
		LocationManager lm = new LocationManager(this, Const.db);
		Cursor c = lm.getAllHoursFromDb(categories[position]);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c, c.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 });
		adapter.setViewBinder(this);

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		// lv2.setOnItemClickListener(this);
		lm.close();

		// TODO cleanup

		// TODO documentation

		// TODO add hours to cafeteria
	}

	@Override
	/**
	 * change presentation of lecture units in the list
	 */
	public boolean setViewValue(View view, Cursor c, int index) {
		if (view.getId() == android.R.id.text2) {
			String transport = c.getString(c.getColumnIndex("transport"));
			String address = c.getString(c.getColumnIndex("address"));

			String hours = c.getString(c.getColumnIndex("hours"));
			String remark = c.getString(c.getColumnIndex("remark"));

			// TODO use stringbuilder
			String content = hours + "\n" + address;
			if (transport.length() > 0) {
				content += " (" + transport + ")";
			}
			if (remark.length() > 0) {
				content += "\n" + remark.replaceAll("\\\\n", "\n");
			}
			TextView tv = (TextView) view;
			tv.setText(content);
			return true;
		}
		return false;
	}
}