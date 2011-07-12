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

		LinkManager.lastInserted = 0;
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
	public boolean onItemLongClick(final AdapterView<?> av, View v,
			final int position, long id) {

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Cursor c = (Cursor) av.getAdapter().getItem(position);
				String _id = c.getString(c.getColumnIndex("_id"));

				LinkManager lm = new LinkManager(av.getContext(), "database.db");
				lm.deleteFromDb(_id);

				SimpleCursorAdapter adapter = (SimpleCursorAdapter) av
						.getAdapter();
				adapter.changeCursor(lm.getAllFromDb());
				lm.close();
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Wirklch löschen?");
		builder.setPositiveButton("Ja", listener);
		builder.setNegativeButton("Nein", null);
		builder.show();
		return false;
	}
}