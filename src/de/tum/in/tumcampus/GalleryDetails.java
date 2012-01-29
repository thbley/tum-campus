package de.tum.in.tumcampus;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import de.tum.in.tumcampus.models.GalleryManager;

/**
 * Activity to show event details (name, location, image, description, etc.)
 */
public class GalleryDetails extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_details);

		// get gallery item details from db
		GalleryManager gm = new GalleryManager(this, Const.db);
		Cursor c = gm.getDetailsFromDb(getIntent().getStringExtra("id"));

		if (c.moveToNext()) {
			String name = c.getString(c.getColumnIndex("name"));
			String image = c.getString(c.getColumnIndex("image"));

			TextView tv = (TextView) findViewById(R.id.infos);
			tv.setText(name);

			ImageView iv = (ImageView) findViewById(R.id.image);
			iv.setImageBitmap(BitmapFactory.decodeFile(image));
		}
		gm.close();
	}
}