package de.tum.in.tumcampus.models;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DownloadManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public DownloadManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public DownloadManager(Context context, SQLiteDatabase db) {
		super(context, db.getPath(), null, DATABASE_VERSION);

		this.db = db;
		onCreate(db);
	}

	public void processDownloads() throws Exception {
		Cursor c = db
				.rawQuery("SELECT * FROM downloads ORDER BY id DESC", null);

		while (c.moveToNext()) {
			String url = c.getString(c.getColumnIndex("url"));
			String target = c.getString(c.getColumnIndex("target"));

			if (!new File(target).exists()) {
				Utils.downloadFile(url, target);
			}
			db.execSQL("DELETE FROM downloads WHERE id=?",
					new String[] { c.getString(c.getColumnIndex("id")) });
		}
		c.close();
	}

	public void insertIntoDb(String url, String target) {
		Log.d("TumCampus downloads insertIntoDb", url);
		onCreate(db);

		if (url.length() == 0) {
			return;
		}
		if (target.length() == 0) {
			return;
		}
		db.execSQL("INSERT INTO downloads (url, target) VALUES (?, ?)",
				new String[] { url, target });
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS downloads ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, url VARCHAR, target VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}