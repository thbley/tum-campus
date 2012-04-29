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
import android.widget.SlidingDrawer;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import de.tum.in.tumcampus.models.EventManager;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Activity to show events (name, location, image, etc.)
 */
public class Events extends Activity implements OnItemClickListener, ViewBinder {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);

		// get toast feedback and resume activity
		registerReceiver(DownloadService.receiver, new IntentFilter(
				DownloadService.broadcast));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(DownloadService.receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get current and upcoming events from database
		EventManager em = new EventManager(this, Const.db);
		Cursor c = em.getNextFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.events_listview, c, c.getColumnNames(), new int[] {
						R.id.icon, R.id.name, R.id.infos });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
		sd.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			@Override
			public void onDrawerOpened() {
				// get past events from database
				EventManager em = new EventManager(Events.this, Const.db);
				Cursor c = em.getPastFromDb();
				SimpleCursorAdapter adapter = new SimpleCursorAdapter(
						Events.this, R.layout.events_listview, c, c
								.getColumnNames(), new int[] { R.id.icon,
								R.id.name, R.id.infos });
				adapter.setViewBinder(Events.this);

				ListView lv2 = (ListView) findViewById(R.id.listView2);
				lv2.setAdapter(adapter);
				lv2.setOnItemClickListener(Events.this);
			}
		});

		// reset new items counter
		EventManager.lastInserted = 0;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor c = (Cursor) av.getAdapter().getItem(position);

		// open event details when clicking an event in the list
		Intent intent = new Intent(this, EventsDetails.class);
		intent.putExtra("id", c.getString(c.getColumnIndex("_id")));
		startActivity(intent);
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int index) {

		/**
		 * <pre>
		 * Show event info text as:
		 * Week-Day, Start DateTime - End Time
		 * location
		 * </pre>
		 */
		if (view.getId() == R.id.infos) {
			String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");

			TextView infos = (TextView) view;
			infos.setText(weekDays[c.getInt(c.getColumnIndex("weekday"))]
					+ ", " + c.getString(c.getColumnIndex("start_de")) + " - "
					+ c.getString(c.getColumnIndex("end_de")) + "\n"
					+ c.getString(c.getColumnIndex("location")));
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem m = menu.add(0, Menu.FIRST, 0, "Aktualisieren");
		m.setIcon(R.drawable.ic_menu_refresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// download latest events
		Intent service = new Intent(this, DownloadService.class);
		service.putExtra("action", "events");
		startService(service);
		return true;
	}
}