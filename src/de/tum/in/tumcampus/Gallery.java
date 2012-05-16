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
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import de.tum.in.tumcampus.models.GalleryManager;
import de.tum.in.tumcampus.services.DownloadService;

/**
 * Activity to show gallery items (name, image, etc.)
 */
public class Gallery extends Activity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		// get toast feedback and resume activity
		registerReceiver(DownloadService.receiver, new IntentFilter(DownloadService.broadcast));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(DownloadService.receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get images from database
		GalleryManager gm = new GalleryManager(this, Const.db);
		Cursor c = gm.getFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.gallery_image, c, c.getColumnNames(),
				new int[] { R.id.image });

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(this);

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
		sd.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				GalleryManager gm = new GalleryManager(Gallery.this, Const.db);
				Cursor c = gm.getFromDbArchive();

				SimpleCursorAdapter adapter = new SimpleCursorAdapter(Gallery.this, R.layout.gallery_image, c, c
						.getColumnNames(), new int[] { R.id.image });

				GridView gridview2 = (GridView) findViewById(R.id.gridview2);
				gridview2.setAdapter(adapter);
				gridview2.setOnItemClickListener(Gallery.this);
			}
		});

		// reset new items counter
		GalleryManager.lastInserted = 0;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Cursor c = (Cursor) av.getAdapter().getItem(position);

		// open gallery details when clicking an item in the list
		Intent intent = new Intent(this, GalleryDetails.class);
		intent.putExtra("id", c.getString(c.getColumnIndex("id")));
		startActivity(intent);
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
		// download latest gallery items
		Intent service = new Intent(this, DownloadService.class);
		service.putExtra("action", "gallery");
		startService(service);
		return true;
	}
}