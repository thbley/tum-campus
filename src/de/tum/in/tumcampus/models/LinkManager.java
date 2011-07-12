package de.tum.in.tumcampus.models;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LinkManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;
	
	public static int lastInserted = 0;

	public LinkManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public void importFromInternal() throws Exception {
		File[] files = new File(Utils.getCacheDir("links")).listFiles();

		int count = Utils.getCount(db, "links");		
		db.beginTransaction();
		for (int i = 0; i < files.length; i++) {
			String filename = files[i].getName();
			if (filename.toLowerCase().endsWith(".url")) {
				String name = filename.substring(0, filename.length() - 4);
				String url = Utils.getLinkFromUrlFile(files[i]);

				insertUpdateIntoDb(new Link(name, url));
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		
		lastInserted += Utils.getCount(db, "links") - count;
	}

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

	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT icon, name, url, id as _id "
				+ "FROM links ORDER BY name", null);
	}

	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM links LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	public void insertUpdateIntoDb(Link l) throws Exception {
		Utils.Log(l.toString());

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

	public void removeCache() {
		db.execSQL("UPDATE links SET icon = ''");
		Utils.emptyCacheDir("links/cache");
	}

	public void deleteFromDb(String id) {
		db.execSQL("DELETE FROM links WHERE id = ?", new String[] { id });
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