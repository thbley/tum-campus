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

	public SQLiteDatabase db;

	public FeedManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public void downloadFromExternal() throws Exception {
		
		// TODO change
		deleteAllFromDb();
		// TODO transaction
		File[] files = new File(Utils.getCacheDir("rss")).listFiles();

		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i]);

			if (files[i].getName().endsWith(".URL")) {
				String name = files[i].getName().replace(".URL", "");
				String url = Utils.getLinkFromUrlFile(files[i]);

				insertIntoDb(new Feed(0, name, url));
			}
		}
	}

	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, feedUrl, id as _id "
				+ "FROM feeds ORDER BY name", null);
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

	public void insertIntoDb(Feed n) throws Exception {
		Log.d("TumCampus feeds replaceIntoDb", n.toString());

		if (n.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (n.feedUrl.length() == 0) {
			throw new Exception("Invalid feedUrl.");
		}

		db.execSQL("INSERT INTO feeds (name, feedUrl) VALUES (?, ?)",
				new String[] { n.name, n.feedUrl });
	}

	public void deleteAllFromDb() {
		Log.d("TumCampus feeds deleteAllFromDb", "");
		db.execSQL("DELETE FROM feeds");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS feeds ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, feedUrl VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}