package de.tum.in.tumcampus.models;

import de.tum.in.tumcampus.Const;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Lecture Manager, handles database stuff
 */
public class LectureManager extends SQLiteOpenHelper {

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * @param database Filename, e.g. database.db
	 * </pre>
	 */
	public LectureManager(Context context, String database) {
		super(context, database, null, Const.dbVersion);

		db = getWritableDatabase();
		onCreate(db);
	}

	/**
	 * Get all lectures from the database
	 * 
	 * @return Database cursor (name, module, _id)
	 */
	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, module, id as _id "
				+ "FROM lectures ORDER BY name", null);
	}

	/**
	 * Refresh lectures from the lectures_items table
	 */
	public void updateLectures() {
		db.execSQL("REPLACE INTO lectures (id, name, module) "
				+ "SELECT DISTINCT lectureId, name, module FROM lectures_items");
	}

	/**
	 * Delete a lecture from the database
	 * 
	 * <pre>
	 * @param id Lecture ID
	 * </pre>
	 */
	public void deleteItemFromDb(String id) {
		db.execSQL("DELETE FROM lectures WHERE id = ?", new String[] { id });
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS lectures ("
				+ "id VARCHAR PRIMARY KEY, name VARCHAR, module VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}