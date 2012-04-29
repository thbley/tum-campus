package de.tum.in.tumcampus.models;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.tum.in.tumcampus.Const;

/**
 * Link Manager, handles database stuff, internal imports, external downloads
 * (icons)
 */
public class LinkManager extends SQLiteOpenHelper {

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
	public LinkManager(Context context, String database) {
		super(context, database, null, Const.dbVersion);

		db = getWritableDatabase();
		onCreate(db);
	}

	/**
	 * Import links from internal sd-card directory
	 * 
	 * @throws Exception
	 */
	public void importFromInternal() throws Exception {
		File[] files = new File(Utils.getCacheDir("links")).listFiles();

		int count = Utils.dbGetTableCount(db, "links");

		db.beginTransaction();
		try {
			for (File file : files) {
				String filename = file.getName();
				if (filename.toLowerCase().endsWith(".url")) {
					lastInfo = filename;
					String name = filename.substring(0, filename.length() - 4);
					String url = Utils.getLinkFromUrlFile(file);

					insertUpdateIntoDb(new Link(name, url));
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "links") - count;
	}

	/**
	 * Check if all icons are available in the cache directory
	 */
	public void checkExistingIcons() {
		Cursor c = db.rawQuery(
				"SELECT DISTINCT icon FROM links WHERE icon!=''", null);

		while (c.moveToNext()) {
			String icon = c.getString(0);

			File f = new File(icon);
			if (!f.exists()) {
				db.execSQL("UPDATE links SET icon='' WHERE icon=?",
						new String[] { icon });
			}
		}
		c.close();
	}

	/**
	 * Download missing link icons
	 * 
	 * @throws Exception
	 */
	public void downloadMissingIcons() throws Exception {
		checkExistingIcons();

		Cursor c = db.rawQuery("SELECT DISTINCT url FROM links WHERE icon=''",
				null);

		while (c.moveToNext()) {
			String url = c.getString(0);

			String target = Utils.getCacheDir("links/cache") + Utils.md5(url)
					+ ".ico";
			Utils.downloadIconFileThread(url, target);

			db.execSQL("UPDATE links SET icon=? WHERE url=?", new String[] {
					target, url });
		}
		c.close();
	}

	/**
	 * Get all links from the database
	 * 
	 * @return Database cursor (icon, name, url, _id)
	 */
	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT icon, name, url, id as _id "
				+ "FROM links ORDER BY name", null);
	}

	/**
	 * Checks if the links table is empty
	 * 
	 * @return true if no links are available, else false
	 */
	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM links LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Insert or Update a link in the database
	 * 
	 * <pre>
	 * @param l Link object
	 * @throws Exception
	 * </pre>
	 */
	public void insertUpdateIntoDb(Link l) throws Exception {
		Utils.log(l.toString());

		l.name = l.name.trim();
		l.url = l.url.trim();

		if (l.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (l.url.length() == 0) {
			throw new Exception("Invalid url.");
		}

		Cursor c = db.rawQuery("SELECT id FROM links WHERE name = ?",
				new String[] { l.name });

		if (c.moveToNext()) {
			db.execSQL("UPDATE links SET url=?, icon='' WHERE id=?",
					new String[] { l.url, c.getString(0) });

		} else {
			db.execSQL("INSERT INTO links (name, url, icon) VALUES (?, ?, '')",
					new String[] { l.name, l.url });
		}
	}

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("UPDATE links SET icon = ''");
		Utils.emptyCacheDir("links/cache");
	}

	/**
	 * Delete Link from database
	 * 
	 * <pre>
	 * @param id Link id
	 * </pre>
	 */
	public void deleteFromDb(int id) {
		db.execSQL("DELETE FROM links WHERE id = ?",
				new String[] { String.valueOf(id) });
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS links ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, "
				+ "url VARCHAR, icon VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}