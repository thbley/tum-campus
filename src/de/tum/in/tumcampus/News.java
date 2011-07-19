package de.tum.in.tumcampus;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Activity to show News (message, image, date)
 */

public class News extends Activity implements OnItemClickListener, ViewBinder {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);

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

		NewsManager nm = new NewsManager(this, Const.db);
		Cursor c = nm.getAllFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.news_listview, c, c.getColumnNames(), new int[] {
						R.id.image, R.id.message, R.id.date });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		nm.close();

		NewsManager.lastInserted = 0;
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int position,
			long id) {
		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex("link"));

		if (url.length() == 0) {
			Toast.makeText(this, "Kein Link vorhanden.", Toast.LENGTH_LONG)
					.show();
			return;
		}

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int index) {
		// hide empty view elements
		if (cursor.getString(index).length() == 0) {
			view.setVisibility(View.GONE);

			// no binding needed
			return true;
		} else {
			view.setVisibility(View.VISIBLE);
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, "Aktualisieren");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent service = new Intent(this, DownloadService.class);
		service.putExtra("action", "news");
		startService(service);
		return true;
	}
}