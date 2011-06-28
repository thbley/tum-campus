package de.tum.in.tumcampus;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.Utils;

public class Cafeterias extends Activity implements OnItemClickListener {

	String date;
	String dateStr;
	String mensaId;
	String mensaName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO check again, next working day?
		date = Utils.getDateString(new Date());
		dateStr = Utils.getDateStringDe(new Date());

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.cafeterias_horizontal);
		} else {
			setContentView(R.layout.cafeterias);
		}

		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, "database.db");
		Cursor c = cmm.getDatesFromDb();

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		cmm.close();

		CafeteriaManager cm = new CafeteriaManager(this, "database.db");
		Cursor c2 = cm.getAllFromDb();

		adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 });

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		lv2.setOnItemClickListener(this);
		cm.close();
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		if (sd.isOpened()) {
			sd.animateClose();
		}

		if (av.getId() == R.id.listView) {
			ListView lv = (ListView) findViewById(R.id.listView);
			Cursor c = (Cursor) lv.getAdapter().getItem(position);
			date = c.getString(c.getColumnIndex("_id"));
			dateStr = c.getString(c.getColumnIndex("date_de"));
		}

		if (av.getId() == R.id.listView2) {
			ListView lv2 = (ListView) findViewById(R.id.listView2);
			Cursor c = (Cursor) lv2.getAdapter().getItem(position);

			mensaId = c.getString(c.getColumnIndex("_id"));
			mensaName = c.getString(c.getColumnIndex("name"));
		}

		if (mensaId != null && date != null) {
			TextView tv = (TextView) findViewById(R.id.cafeteriaText);
			tv.setText(mensaName + ": " + dateStr);

			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
					"database.db");
			Cursor c = cmm.getTypeNameFromDb(mensaId, date);

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
}
