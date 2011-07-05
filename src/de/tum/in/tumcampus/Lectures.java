package de.tum.in.tumcampus;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampus.models.LectureItemManager;

public class Lectures extends Activity implements OnItemClickListener,
		ViewBinder {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lectures);

		LectureItemManager lim = new LectureItemManager(this, "database.db");
		Cursor c = lim.getAllFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, c, c.getColumnNames(),
				new int[] { android.R.id.text1, android.R.id.text2 });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lim.close();
		
		// TODO context menu:
		// TODO change series esp. time
		// TODO delete single entry, e.g. cscw2 13.7. 
		// TODO add single entry to lecture, e.g. 14.7.

		// TODO add second listview with all lectures
		// TODO delete complete lecture + confirm
		
		// TODO add horizontal view
		
		// TODO add holidays, vacation
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int index) {
		String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");

		// TODO optimize
		if (view.getId() == android.R.id.text1) {
			String name = c.getString(c.getColumnIndex("name"));
			if (name.indexOf("(") != -1) {
				name = name.substring(0, name.indexOf("(")).trim();
			}
			if (name.length() > 20) {
				name = name.substring(0, 20) + " ...";
			}

			String note = c.getString(c.getColumnIndex("note"));
			if (note.length() > 0) {
				name += " - " + note;
			}
			TextView tv = (TextView) view;
			tv.setText(name);
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
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex("url"));

		if (url.length() == 0) {
			url = "https://campus.tum.de/tumonline/wbSuche.LVSucheSimple?"
					+ "pLVNrFlag=J&pSjNr=1573&pSemester=S&pSuchbegriff="
					+ c.getString(c.getColumnIndex("lectureId"));
		}

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}
}