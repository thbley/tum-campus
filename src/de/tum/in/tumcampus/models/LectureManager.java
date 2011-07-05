package de.tum.in.tumcampus.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LectureManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public LectureManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public void updateLectures() {
		db.execSQL("REPLACE INTO lectures (id, name) "
				+ "SELECT DISTINCT lectureId, name FROM lectures_items");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS lectures ("
				+ "id VARCHAR PRIMARY KEY, name VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}