package de.tum.in.tumcampus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SlidingDrawer;
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.FeedManager;

public class Feeds extends Activity implements OnItemClickListener, ViewBinder, OnItemLongClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeds);

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		sd.open();

		FeedManager fm = new FeedManager(this, "database.db");
		Cursor c = fm.getAllFromDb();

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		fm.close();
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		if (av.getId() == R.id.listView2) {
			ListView lv = (ListView) findViewById(R.id.listView2);
			Cursor c = (Cursor) lv.getAdapter().getItem(position);
			String link = c.getString(c.getColumnIndex("link"));

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			startActivity(intent);
			return;
		}

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		if (sd.isOpened()) {
			sd.animateClose();
		}

		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String feedId = c.getString(c.getColumnIndex("_id"));
		String name = c.getString(c.getColumnIndex("name"));

		setTitle("Nachrichten: " + name);

		FeedItemManager fim = new FeedItemManager(this, "database.db");
		Cursor c2 = fim.getAllFromDb(feedId);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.feeds_listview, c2, c2.getColumnNames(), new int[] {
						R.id.icon, R.id.title, R.id.description });

		adapter.setViewBinder(this);
		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter);
		lv2.setOnItemClickListener(this);
		fim.close();
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
	public boolean onItemLongClick(AdapterView<?> av, View v, int position,
			long id) {

		final ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		final String _id = c.getString(c.getColumnIndex("_id"));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Wirklch löschen?");
		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				FeedManager fm = new FeedManager(lv.getContext(), "database.db");
				fm.deleteFromDb(_id);

				SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv
						.getAdapter();
				adapter.changeCursor(fm.getAllFromDb());
				fm.close();

				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Nein",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.show();

		return false;
	}
}