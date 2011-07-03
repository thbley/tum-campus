package de.tum.in.tumcampus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampus.models.TransportManager;

public class Transports extends Activity implements OnItemClickListener,
		OnItemLongClickListener, OnEditorActionListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.transports_horizontal);
		} else {
			setContentView(R.layout.transports);
		}

		TransportManager tm = new TransportManager(this, "database.db");
		Cursor c = tm.getAllFromDb();

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
				new int[] { android.R.id.text1 });

		final ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		tm.close();

		final EditText et = (EditText) findViewById(R.id.search);
		et.setOnEditorActionListener(this);

		et.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence input, int arg1, int arg2,
					int arg3) {
				if (input.length() == 3) {
					et.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String location = c.getString(c.getColumnIndex("name"));

		TextView tv = (TextView) findViewById(R.id.transportText);
		tv.setText("Abfahrt: " + location);

		tv = (TextView) findViewById(R.id.transportText2);
		tv.setText("Gespeicherte Stationen:");

		TransportManager tm = new TransportManager(this, "database.db");

		Cursor c2 = null;
		try {
			tm.replaceIntoDb(location);

			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();

			if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
				throw new Exception("<Keine Internetverbindung>");
			}
			c2 = tm.getDeparturesFromExternal(location);

		} catch (Exception e) {
			MatrixCursor c3 = new MatrixCursor(new String[]{"name", "_id"});
			c3.addRow(new String[] { e.getMessage(), "" });
			c2 = c3;
		}

		ListAdapter adapter2 = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 }) {

			public boolean isEnabled(int position) {
				return false;
			}
		};

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter2);

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
		adapter.changeCursor(tm.getAllFromDb());
		tm.close();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position,
			long id) {
		final ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		final String location = c.getString(c.getColumnIndex("name"));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Wirklch löschen?");
		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				TransportManager tm = new TransportManager(lv.getContext(),
						"database.db");
				tm.deleteFromDb(location);

				SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv
						.getAdapter();
				adapter.changeCursor(tm.getAllFromDb());
				tm.close();

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

	@Override
	public boolean onEditorAction(TextView input, int code, KeyEvent key) {

		ListView lv = (ListView) findViewById(R.id.listView);
		TransportManager tm = new TransportManager(lv.getContext(),
				"database.db");

		Cursor c = null;
		try {
			c = tm.getStationsFromExternal(input.getText().toString());
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
		TextView tv = (TextView) findViewById(R.id.transportText2);
		tv.setText("Suchergebnis:");

		SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
		adapter.changeCursor(c);
		tm.close();
		return false;
	}
}