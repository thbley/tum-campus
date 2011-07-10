package de.tum.in.tumcampus;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampus.models.EventManager;
import de.tum.in.tumcampus.services.DownloadService;

public class Events extends Activity implements OnItemClickListener, ViewBinder {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);
	}

	@Override
	protected void onResume() {
		super.onResume();

		EventManager em = new EventManager(this, "database.db");
		Cursor c = em.getNextFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.events_listview, c, c.getColumnNames(), new int[] {
						R.id.icon, R.id.name, R.id.infos });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		c = em.getPastFromDb();

		adapter = new SimpleCursorAdapter(this, R.layout.events_listview, c,
				c.getColumnNames(), new int[] { R.id.icon, R.id.name,
						R.id.infos });
		adapter.setViewBinder(this);

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		lv2.setOnItemClickListener(this);
		em.close();
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor c = (Cursor) av.getAdapter().getItem(position);

		Intent intent = new Intent(this, EventsDetails.class);
		intent.putExtra("id", c.getString(c.getColumnIndex("_id")));
		startActivity(intent);
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int index) {

		String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");

		if (view.getId() == R.id.infos) {
			TextView infos = (TextView) view;
			infos.setText(weekDays[c.getInt(c.getColumnIndex("weekday"))]
					+ ", " + c.getString(c.getColumnIndex("start_de")) + " - "
					+ c.getString(c.getColumnIndex("end_de")) + "\n"
					+ c.getString(c.getColumnIndex("location")));
			return true;
		}
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, "Aktualisieren");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent service = new Intent(this, DownloadService.class);
		service.putExtra("action", "events");
		startService(service);

		registerReceiver(DownloadService.receiver, new IntentFilter(
				DownloadService.broadcast));
		return true;
	}
}