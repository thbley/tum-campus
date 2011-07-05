package de.tum.in.tumcampus;

import de.tum.in.tumcampus.models.NewsManager;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class News extends Activity implements OnItemClickListener, ViewBinder{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        
        NewsManager nm = new NewsManager(this, "database.db");
		Cursor c = nm.getAllFromDb();

		ListAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.news_listview, c, c.getColumnNames(),
				new int[] { R.id.image, R.id.message });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		nm.close();
  
    }

	@Override
	public void onItemClick(AdapterView<?> aview, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		ListView lv = (ListView) findViewById(R.id.listView);
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex("link"));

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
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
}