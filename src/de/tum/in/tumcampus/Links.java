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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.tum.in.tumcampus.models.LinkManager;

public class Links extends Activity implements OnItemClickListener,
		OnItemLongClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.links);

		LinkManager lm = new LinkManager(this, "database.db");
		Cursor c = lm.getAllFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.links_listview, c, c.getColumnNames(), new int[] {
						R.id.icon, R.id.name });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		lm.close();
	}

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int position,
			long id) {
		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex("url"));

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
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

				LinkManager lm = new LinkManager(lv.getContext(), "database.db");
				lm.deleteFromDb(_id);

				SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv
						.getAdapter();
				adapter.changeCursor(lm.getAllFromDb());
				lm.close();

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