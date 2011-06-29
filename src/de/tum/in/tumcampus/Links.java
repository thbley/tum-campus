package de.tum.in.tumcampus;

import de.tum.in.tumcampus.models.LinkManager;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class Links extends Activity implements OnItemClickListener, ViewBinder {

	SQLiteDatabase db;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.links);
        
        LinkManager lm = new LinkManager(this, "database.db");
		Cursor c = lm.getAllFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.links_listview, c, c.getColumnNames(), new int[] {
						R.id.icon, R.id.name });
		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lm.close();
		
    }

	@Override
	public boolean setViewValue(View arg0, Cursor arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}