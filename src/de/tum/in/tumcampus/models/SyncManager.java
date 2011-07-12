package de.tum.in.tumcampus.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SyncManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public SyncManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public SyncManager(Context context, SQLiteDatabase db) {
		super(context, db.getPath(), null, DATABASE_VERSION);

		this.db = db;
		onCreate(db);
	}

	public static void replaceIntoDb(SQLiteDatabase db, Object obj) {
		replaceIntoDb(db, obj.getClass().getName());
	}

	public static void replaceIntoDb(SQLiteDatabase db, String id) {
		Utils.Log(id);

		if (id.length() == 0) {
			return;
		}
		db.execSQL("REPLACE INTO syncs (id, lastSync) VALUES (?, datetime())",
				new String[] { id });
	}

	public static boolean needSync(SQLiteDatabase db, Object obj, int seconds) {
		return needSync(db, obj.getClass().getName(), seconds);
	}

	public static boolean needSync(SQLiteDatabase db, String id, int seconds) {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT lastSync FROM syncs "
				+ "WHERE lastSync > datetime('now', '-" + seconds
				+ " second') AND id=?", new String[] { id });
		if (c.getCount() == 1) {
			result = false;
		}
		c.close();
		return result;
	}

	public void deleteFromDb() {
		db.execSQL("DELETE FROM syncs");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS syncs ("
				+ "id VARCHAR PRIMARY KEY, lastSync VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}