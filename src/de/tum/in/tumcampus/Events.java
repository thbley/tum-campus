package de.tum.in.tumcampus;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import de.tum.in.tumcampus.models.EventManager;

public class Events extends Activity implements OnItemClickListener, ViewBinder {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);

		EventManager em = new EventManager(this, "database.db");
		Cursor c = em.getAllFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.events_listview, c, c.getColumnNames(), new int[] {
						R.id.icon, R.id.title, R.id.infos });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		em.close();
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		
		// TODO move to activity
		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String description = c.getString(c.getColumnIndex("description"));
		String image = c.getString(c.getColumnIndex("image"));

		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		if (!sd.isOpened()) {
			sd.animateOpen();
		}

		TextView tv = (TextView) findViewById(R.id.description);
		tv.setText(description);

		ImageView iv = (ImageView) findViewById(R.id.image);
		iv.setImageURI(Uri.parse(image));

		// TODO optimize
		double ratio = (double) iv.getDrawable().getIntrinsicWidth()
				/ (double) iv.getDrawable().getIntrinsicHeight();
		iv.getLayoutParams().width = 300;
		iv.getLayoutParams().height = (int) Math.floor(300 / ratio);
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int index) {

		String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");

		if (view.getId() == R.id.infos) {
			TextView infos = (TextView) view;
			infos.setText(weekDays[c.getInt(c.getColumnIndex("weekday"))]
					+ ", " + c.getString(c.getColumnIndex("start")) + " - "
					+ c.getString(c.getColumnIndex("end")) + "\n"
					+ c.getString(c.getColumnIndex("location")));
			return true;
		}
		return false;
	}
}