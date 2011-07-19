package de.tum.in.tumcampus.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.tum.in.tumcampus.Const;

/**
 * Feed Manager, handles database stuff, internal imports
 */
public class FeedManager extends SQLiteOpenHelper {

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	/**
	 * Additional information for exception messages
	 */
	public String lastInfo = "";

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * @param database Filename, e.g. database.db
	 * </pre>
	 */
	public FeedManager(Context context, String database) {
		super(context, database, null, Const.dbVersion);

		db = getWritableDatabase();
		onCreate(db);
	}

	/**
	 * Import feeds from internal sd-card directory
	 * 
	 * @throws Exception
	 */
	public void importFromInternal() throws Exception {
		File[] files = new File(Utils.getCacheDir("rss")).listFiles();

		int count = Utils.getCount(db, "feeds");

		db.beginTransaction();
		for (File file : files) {
			String filename = file.getName();
			if (filename.toLowerCase().endsWith(".url")) {
				lastInfo = filename;
				String name = filename.substring(0, filename.length() - 4);
				String url = Utils.getLinkFromUrlFile(file);

				insertUpdateIntoDb(new Feed(name, url));
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();

		// update last insert counter
		lastInserted += Utils.getCount(db, "feeds") - count;
	}

	/**
	 * Get all feeds from the database
	 * 
	 * @return Database cursor (name, feedUrl, _id)
	 */
	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, feedUrl, id as _id "
				+ "FROM feeds ORDER BY name", null);
	}

	/**
	 * Checks if the feeds table is empty
	 * 
	 * @return true if no feeds are available, else false
	 */
	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM feeds LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Get all Feed IDs from the database
	 * 
	 * @return List of feed IDs
	 */
	public List<Integer> getAllIdsFromDb() {
		List<Integer> list = new ArrayList<Integer>();

		Cursor c = db.rawQuery("SELECT id FROM feeds ORDER BY id", null);
		while (c.moveToNext()) {
			list.add(c.getInt(0));
		}
		c.close();
		return list;
	}

	/**
	 * Insert or Update a feed in the database
	 * 
	 * <pre>
	 * @param f Feed object
	 * @return Feed ID
	 * @throws Exception
	 * </pre>
	 */
	public int insertUpdateIntoDb(Feed f) throws Exception {
		Utils.log(f.toString());

		if (f.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (f.feedUrl.length() == 0) {
			throw new Exception("Invalid feedUrl.");
		}

		Cursor c = db.rawQuery("SELECT id FROM feeds WHERE name = ?",
				new String[] { f.name });

		if (c.moveToNext()) {
			db.execSQL("UPDATE feeds SET name=?, feedUrl=? WHERE id=?",
					new String[] { f.name, f.feedUrl, c.getString(0) });
			return c.getInt(0);
		} else {
			db.execSQL("INSERT INTO feeds (name, feedUrl) VALUES (?, ?)",
					new String[] { f.name, f.feedUrl });

			c = db.rawQuery("SELECT last_insert_rowid()", null);
			c.moveToNext();
			return c.getInt(0);
		}
	}

	/**
	 * Delete feed from database
	 * 
	 * <pre>
	 * @param id Feed ID
	 * </pre>
	 */
	public void deleteFromDb(int id) {
		db.execSQL("DELETE FROM feeds WHERE id = ?",
				new String[] { String.valueOf(id) });
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS feeds ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, feedUrl VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}