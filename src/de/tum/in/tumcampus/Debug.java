package de.tum.in.tumcampus;

import de.tum.in.tumcampus.models.DatabaseManager;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity to show raw table contents of the database
 */
public class Debug extends Activity implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug);

		// initialize buttons
		Button b = (Button) findViewById(R.id.debugCafeterias);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugCafeteriasMenus);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugFeeds);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugFeedsItems);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugLectures);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugLecturesItems);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugLinks);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugEvents);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugGallery);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugNews);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugSyncs);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugTime);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugMaster);
		b.setOnClickListener(this);

		b = (Button) findViewById(R.id.debugLocations);
		b.setOnClickListener(this);
	}

	/**
	 * clear debug content text view
	 */
	public void debugReset() {
		TextView tv = (TextView) findViewById(R.id.debug);
		tv.setText("");
	}

	/**
	 * Add a message to the debug content text view
	 * 
	 * @param s
	 *            Debug message
	 */
	public void debugStr(String s) {
		TextView tv = (TextView) findViewById(R.id.debug);
		tv.setMovementMethod(new ScrollingMovementMethod());
		tv.append(s + "\n");
	}

	/**
	 * Execute a database query and present the results in the GUI
	 * 
	 * @param query
	 *            SQL query to execute
	 */
	public void debugSQL(String query) {
		debugReset();
		SQLiteDatabase db = DatabaseManager.getDb(this);

		// output raw data row-by-row
		Cursor c = db.rawQuery(query, null);
		while (c.moveToNext()) {
			StringBuilder content = new StringBuilder();
			for (int i = 0; i < c.getColumnCount(); i++) {
				content.append(c.getColumnName(i) + ": " + c.getString(i)
						+ "\n");
			}
			debugStr(content.toString());
		}
		c.close();
	}

	@Override
	public void onClick(View v) {

		// execute queries on click and present results in GUI
		if (v.getId() == R.id.debugSyncs) {
			debugSQL("SELECT * FROM syncs ORDER BY id");
		}

		if (v.getId() == R.id.debugCafeterias) {
			debugSQL("SELECT * FROM cafeterias ORDER BY id");
		}

		if (v.getId() == R.id.debugCafeteriasMenus) {
			debugSQL("SELECT * FROM cafeterias_menus ORDER BY id");
		}

		if (v.getId() == R.id.debugLocations) {
			debugSQL("SELECT * FROM locations ORDER BY id");
		}

		if (v.getId() == R.id.debugFeeds) {
			debugSQL("SELECT * FROM feeds ORDER BY id");
		}

		if (v.getId() == R.id.debugFeedsItems) {
			debugSQL("SELECT * FROM feeds_items ORDER BY feedId, date DESC");
		}

		if (v.getId() == R.id.debugLectures) {
			debugSQL("SELECT * FROM lectures ORDER BY name");
		}

		if (v.getId() == R.id.debugLecturesItems) {
			debugSQL("SELECT * FROM lectures_items ORDER BY lectureId, id");
		}

		if (v.getId() == R.id.debugLinks) {
			debugSQL("SELECT * FROM links ORDER BY id");
		}

		if (v.getId() == R.id.debugEvents) {
			debugSQL("SELECT * FROM events ORDER BY start DESC");
		}

		if (v.getId() == R.id.debugGallery) {
			debugSQL("SELECT * FROM gallery ORDER BY position ASC");
		}

		if (v.getId() == R.id.debugNews) {
			debugSQL("SELECT * FROM news ORDER BY date DESC");
		}

		if (v.getId() == R.id.debugTime) {
			debugSQL("SELECT datetime('now', 'localtime')");
		}

		if (v.getId() == R.id.debugMaster) {
			debugSQL("SELECT * FROM sqlite_master");
		}
	}
}