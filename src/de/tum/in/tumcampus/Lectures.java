package de.tum.in.tumcampus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.LectureManager;
import de.tum.in.tumcampus.models.Utils;

/**
 * Activity to show lectures and lecture units
 */
public class Lectures extends Activity implements OnItemClickListener,
		OnItemLongClickListener, ViewBinder {

	/**
	 * Current lecture selected
	 */
	String lectureId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.lectures_horizontal);
		} else {
			setContentView(R.layout.lectures);
		}

		// get all upcoming lecture units
		LectureItemManager lim = new LectureItemManager(this, Const.db);
		Cursor c = lim.getRecentFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c, c.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		lim.close();

		// get all lectures
		LectureManager lm = new LectureManager(this, Const.db);
		Cursor c2 = lm.getAllFromDb();

		SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1 });
		adapter2.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor c, int index) {
				// truncate lecture names to 20 characters
				String name = c.getString(index);
				TextView tv = (TextView) view;
				tv.setText(Utils.trunc(name, 20));
				return true;
			}
		});

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter2);
		lv2.setOnItemClickListener(this);
		lv2.setOnItemLongClickListener(this);
		lm.close();

		// reset new items counter
		LectureItemManager.lastInserted = 0;
	}

	@Override
	/**
	 * change presentation of lecture units in the list
	 */
	public boolean setViewValue(View view, Cursor c, int index) {
		String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");

		if (view.getId() == android.R.id.text1) {
			// truncate lecture name to 20 characters,
			// append lecture unit note
			String name = c.getString(c.getColumnIndex("name"));
			String note = c.getString(c.getColumnIndex("note"));
			if (note.length() > 0) {
				note = " - " + note;
			}
			TextView tv = (TextView) view;
			tv.setText(Utils.trunc(name, 20) + note);
			return true;
		}
		if (view.getId() == android.R.id.text2) {
			/**
			 * <pre>
			 * show info as:
			 * Lecture: Week-day, Start DateTime - End Time, Room-Nr-Intern
			 * Holiday: Week-day, Start Date
			 * vacation info: Start Date - End Date
			 * 
			 * Location format: Room-Nr-Intern, Room-name (Room-Nr-Extern)
			 * </pre>
			 */
			String info = "";
			String lectureId = c.getString(c.getColumnIndex("lectureId"));

			if (lectureId.equals("vacation")) {
				info = c.getString(c.getColumnIndex("start_dt")) + " - "
						+ c.getString(c.getColumnIndex("end_dt"));

			} else if (lectureId.equals("holiday")) {
				info = weekDays[c.getInt(c.getColumnIndex("weekday"))] + ", "
						+ c.getString(c.getColumnIndex("start_dt"));

			} else {
				info = weekDays[c.getInt(c.getColumnIndex("weekday"))] + ", "
						+ c.getString(c.getColumnIndex("start_de")) + " - "
						+ c.getString(c.getColumnIndex("end_de"));

				String location = c.getString(c.getColumnIndex("location"));
				if (location.indexOf(",") != -1) {
					location = location.substring(0, location.indexOf(","));
				}
				if (location.length() != 0) {
					info += ", " + location;
				}
			}
			TextView tv = (TextView) view;
			tv.setText(info);
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		ListView lv = (ListView) findViewById(R.id.listView);
		ListView lv2 = (ListView) findViewById(R.id.listView2);

		// Click on lecture list
		if (av.getId() == R.id.listView2) {
			Cursor c2 = (Cursor) lv2.getAdapter().getItem(position);
			lectureId = c2.getString(c2.getColumnIndex("_id"));
			String name = c2.getString(c2.getColumnIndex("name"));
			String module = c2.getString(c2.getColumnIndex("module"));

			// get all lecture units from a lecture
			LectureItemManager lim = new LectureItemManager(this, Const.db);

			SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
			adapter.changeCursor(lim.getAllFromDb(lectureId));
			lim.close();

			TextView tv = (TextView) findViewById(R.id.lectureText);
			tv.setText(Utils.trunc(name + ":", 35));

			// Link to lecture module homepage (e.g. contains ECTS)
			String moduleUrl = "https://drehscheibe.in.tum.de/myintum/kurs_verwaltung/cm.html.de?id="
					+ module;

			TextView tv2 = (TextView) findViewById(R.id.moduleText);
			tv2.setText(Html.fromHtml("<a href='" + moduleUrl + "'>" + module
					+ "</a>"));
			tv2.setMovementMethod(LinkMovementMethod.getInstance());
			return;
		}

		// click on lecture unit list
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex("url"));

		// empty link => no action
		if (url.equals("about:blank")) {
			return;
		}

		// tumonline search page => more lecture details
		if (url.length() == 0) {
			url = "https://campus.tum.de/tumonline/wbSuche.LVSucheSimple?"
					+ "pLVNrFlag=J&pSjNr=1573&pSemester=A&pSuchbegriff="
					+ c.getString(c.getColumnIndex("lectureId"));
		}

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}

	/**
	 * Deletes a lecture unit and refreshes the lecture unit list
	 * 
	 * <pre>
	 * @param itemId Lecture unit id
	 * </pre>
	 */
	public void deleteLectureItem(String itemId) {
		// delete lecture item
		LectureItemManager lim = new LectureItemManager(this, Const.db);
		lim.deleteItemFromDb(itemId);

		ListView lv = (ListView) findViewById(R.id.listView);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
		if (lectureId == null) {
			adapter.changeCursor(lim.getRecentFromDb());
		} else {
			adapter.changeCursor(lim.getAllFromDb(lectureId));
		}
		lim.close();
	}

	/**
	 * Deletes a lecture and refreshes both list views
	 * 
	 * <pre>
	 * @param itemId Lecture id
	 * </pre>
	 */
	public void deleteLecture(String itemId) {
		// delete lecture
		LectureManager lm = new LectureManager(this, Const.db);
		lm.deleteItemFromDb(itemId);

		// refresh lecture list
		ListView lv2 = (ListView) findViewById(R.id.listView2);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv2.getAdapter();
		adapter.changeCursor(lm.getAllFromDb());
		lm.close();

		// delete lecture items
		LectureItemManager lim = new LectureItemManager(this, Const.db);
		lim.deleteLectureFromDb(itemId);

		// refresh lecture unit list if viewing deleted lecture or
		// recent lectures (could contain a unit from deleted lecture)
		ListView lv = (ListView) findViewById(R.id.listView);
		adapter = (SimpleCursorAdapter) lv.getAdapter();

		if (lectureId == null || lectureId.equals(itemId)) {
			adapter.changeCursor(lim.getRecentFromDb());

			TextView tv = (TextView) findViewById(R.id.lectureText);
			tv.setText("Nächste Vorlesungen:");

			TextView tv2 = (TextView) findViewById(R.id.moduleText);
			tv2.setText("");

			// unselect current lecture (no longer exists)
			lectureId = null;
		}
		lim.close();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> av, View v,
			final int position, long id) {

		// confirm deleting lectures or lecture units
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				Cursor c = (Cursor) av.getAdapter().getItem(position);
				String itemId = c.getString(c.getColumnIndex("_id"));

				if (av.getId() == R.id.listView) {
					deleteLectureItem(itemId);
				} else {
					deleteLecture(itemId);
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Wirklch löschen?");
		builder.setPositiveButton("Ja", listener);
		builder.setNegativeButton("Nein", null);
		builder.show();
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem m = menu.add(0, Menu.FIRST, 0, "Roomfinder");
		m.setIcon(android.R.drawable.ic_menu_mylocation);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// open url in browser
		String url = "http://portal.mytum.de/campus/roomfinder/";
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		return true;
	}
}