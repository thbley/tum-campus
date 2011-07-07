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

public class Lectures extends Activity implements OnItemClickListener,
		OnItemLongClickListener, ViewBinder {

	String lectureId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.lectures_horizontal);
		} else {
			setContentView(R.layout.lectures);
		}

		// TODO add robotium tests

		LectureItemManager lim = new LectureItemManager(this, "database.db");
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

		LectureManager lm = new LectureManager(this, "database.db");
		Cursor c2 = lm.getAllFromDb();

		SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1 });
		adapter2.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor c, int index) {
				String name = c.getString(index);
				TextView tv = (TextView) view;
				tv.setText(Utils.trunc(name, 20));
				return true;
			}
		});

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter2);
		lv2.setOnItemClickListener(this);
		lm.close();

		// TODO change series esp. time ??

		// TODO add holidays, vacation
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int index) {
		String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");

		// TODO optimize
		if (view.getId() == android.R.id.text1) {
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
			String location = c.getString(c.getColumnIndex("location"));
			if (location.indexOf(",") != -1) {
				location = location.substring(0, location.indexOf(","));
			}

			String datetime = weekDays[c.getInt(c.getColumnIndex("weekday"))]
					+ ", " + c.getString(c.getColumnIndex("start_de")) + " - "
					+ c.getString(c.getColumnIndex("end_de"));

			TextView tv = (TextView) view;
			tv.setText(Html.fromHtml(datetime + ", " + location));
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		ListView lv = (ListView) findViewById(R.id.listView);
		ListView lv2 = (ListView) findViewById(R.id.listView2);

		if (av.getId() == R.id.listView2) {
			Cursor c2 = (Cursor) lv2.getAdapter().getItem(position);
			lectureId = c2.getString(c2.getColumnIndex("_id"));
			String name = c2.getString(c2.getColumnIndex("name"));
			String module = c2.getString(c2.getColumnIndex("module"));

			LectureItemManager lim = new LectureItemManager(this, "database.db");

			SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
			adapter.changeCursor(lim.getAllFromDb(lectureId));
			lim.close();

			TextView tv = (TextView) findViewById(R.id.lectureText);
			tv.setText(Utils.trunc(name + ":", 35));

			String url = "http://drehscheibe.in.tum.de/myintum/kurs_verwaltung/cm.html.de?id="
					+ module;

			TextView tv2 = (TextView) findViewById(R.id.moduleText);
			tv2.setText(Html.fromHtml("<a href='" + url + "'>" + module
					+ "</a>"));
			tv2.setMovementMethod(LinkMovementMethod.getInstance());
			return;
		}

		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex("url"));

		if (url.length() == 0) {
			url = "https://campus.tum.de/tumonline/wbSuche.LVSucheSimple?"
					+ "pLVNrFlag=J&pSjNr=1573&pSemester=A&pSuchbegriff="
					+ c.getString(c.getColumnIndex("lectureId"));
		}

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> av, View v,
			final int position, long id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Wirklch löschen?");
		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				Cursor c = (Cursor) av.getAdapter().getItem(position);
				String itemId = c.getString(c.getColumnIndex("_id"));

				LectureItemManager lim = new LectureItemManager(
						av.getContext(), "database.db");
				lim.deleteItemFromDb(itemId);

				SimpleCursorAdapter adapter = (SimpleCursorAdapter) av
						.getAdapter();
				if (lectureId == null) {
					adapter.changeCursor(lim.getRecentFromDb());
				} else {
					adapter.changeCursor(lim.getAllFromDb(lectureId));
				}
				lim.close();

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