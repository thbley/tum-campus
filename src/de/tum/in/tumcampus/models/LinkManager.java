package de.tum.in.tumcampus.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.R;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LinkManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	public SQLiteDatabase db;

	public LinkManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
	}

	public void downloadFromExternal() throws Exception {

		deleteAllFromDb();
		File[] files = new File(Utils.getCacheDir("")).listFiles();

		// TODO implement
		String icon = String.valueOf(R.drawable.icon);

		for (int i = 0; i < files.length; i++) {

			if (files[i].getName().endsWith(".URL")) {
				String name = files[i].getName().replace(".URL", "");
				String url = Utils.getLinkFromUrlFile(files[i]);

				insertIntoDb(new Link(0, name, url, icon));
			}
		}
	}

	public List<Link> getAllFromDb() {
		List<Link> list = new ArrayList<Link>();

		Cursor c = db.rawQuery("SELECT * FROM links ORDER BY name", null);

		while (c.moveToNext()) {
			list.add(new Link(c.getInt(c.getColumnIndex("id")), c.getString(c
					.getColumnIndex("name")), c.getString(c
					.getColumnIndex("url")), c.getString(c
					.getColumnIndex("icon"))));
		}
		c.close();
		return list;
	}

	public void insertIntoDb(Link l) throws Exception {
		Log.d("TumCampus links replaceIntoDb", l.toString());

		if (l.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (l.url.length() == 0) {
			throw new Exception("Invalid url.");
		}

		db.execSQL("INSERT INTO links (name, url, icon) VALUES (?, ?, ?)",
				new String[] { l.name, l.url, l.icon });
	}

	public void deleteAllFromDb() {
		Log.d("TumCampus links deleteAllFromDb", "");
		db.execSQL("DELETE FROM links");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS links ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, "
				+ "url VARCHAR, icon VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}