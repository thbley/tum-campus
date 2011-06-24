package de.tum.in.tumcampus.models;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;

	public SQLiteDatabase db;

	public NewsManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
	}

	public void downloadFromExternal() throws Exception {
		// TODO implement
	}

	public List<News> getAllFromDb() {
		List<News> list = new ArrayList<News>();

		Cursor c = db.rawQuery("SELECT * FROM news ORDER BY name", null);

		while (c.moveToNext()) {
			list.add(new News(c.getInt(c.getColumnIndex("id")), c.getString(c
					.getColumnIndex("name")), c.getString(c
					.getColumnIndex("feedUrl"))));
		}
		c.close();
		return list;
	}

	public List<Integer> getAllIdsFromDb() {
		List<Integer> list = new ArrayList<Integer>();

		Cursor c = db.rawQuery("SELECT id FROM news ORDER BY id", null);

		while (c.moveToNext()) {
			list.add(c.getInt(0));
		}
		c.close();
		return list;
	}

	public void replaceIntoDb(News n) throws Exception {
		Log.d("TumCampus news replaceIntoDb", n.toString());

		if (n.id <= 0) {
			throw new Exception("Invalid id.");
		}
		if (n.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (n.feedUrl.length() == 0) {
			throw new Exception("Invalid feedUrl.");
		}

		db.execSQL(
				"REPLACE INTO news (id, name, feedUrl) VALUES (?, ?, ?)",
				new String[] { String.valueOf(n.id), n.name, n.feedUrl });
	}

	public void deleteAllFromDb() {
		Log.d("TumCampus news deleteAllFromDb", "");
		db.execSQL("DELETE FROM news");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS news ("
				+ "id INTEGER PRIMARY KEY, name VARCHAR, feedUrl VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}