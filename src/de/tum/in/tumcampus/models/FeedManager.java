package de.tum.in.tumcampus.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public FeedManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public void importFromInternal() throws Exception {

		File[] files = new File(Utils.getCacheDir("rss")).listFiles();

		db.beginTransaction();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".URL")) {
				String name = files[i].getName().replace(".URL", "");
				String url = Utils.getLinkFromUrlFile(files[i]);

				insertUpdateIntoDb(new Feed(name, url));
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, feedUrl, id as _id "
				+ "FROM feeds ORDER BY name", null);
	}

	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM feeds LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	public List<Integer> getAllIdsFromDb() {
		List<Integer> list = new ArrayList<Integer>();

		Cursor c = db.rawQuery("SELECT id FROM feeds ORDER BY id", null);

		while (c.moveToNext()) {
			list.add(c.getInt(0));
		}
		c.close();
		return list;
	}

	public void insertUpdateIntoDb(Feed n) throws Exception {
		Log.d("TumCampus feeds replaceIntoDb", n.toString());

		if (n.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (n.feedUrl.length() == 0) {
			throw new Exception("Invalid feedUrl.");
		}

		Cursor c = db.rawQuery("SELECT id FROM feeds WHERE name = ?",
				new String[] { n.name });

		if (c.moveToNext()) {
			db.execSQL("UPDATE feeds SET name=?, feedUrl=? WHERE id=?",
					new String[] { n.name, n.feedUrl, c.getString(0) });

		} else {
			db.execSQL("INSERT INTO feeds (name, feedUrl) VALUES (?, ?)",
					new String[] { n.name, n.feedUrl });
		}
	}

	public void deleteFromDb(String id) {
		db.execSQL("DELETE FROM feeds WHERE id = ?", new String[] { id });
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS feeds ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, feedUrl VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}