package de.tum.in.tumcampus.models;

import android.content.Context;
import android.database.Cursor;
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

	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, module, id as _id "
				+ "FROM lectures ORDER BY name", null);
	}

	public void updateLectures() {
		db.execSQL("REPLACE INTO lectures (id, name, module) "
				+ "SELECT DISTINCT lectureId, name, module FROM lectures_items");
	}

	public void deleteItemFromDb(String id) {
		db.execSQL("DELETE FROM lectures WHERE id = ?", new String[] { id });
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS lectures ("
				+ "id VARCHAR PRIMARY KEY, name VARCHAR, module VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}