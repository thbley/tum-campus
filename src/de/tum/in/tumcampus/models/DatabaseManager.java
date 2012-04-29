package de.tum.in.tumcampus.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import de.tum.in.tumcampus.Const;

/**
 * Database singleton
 */
abstract public class DatabaseManager {

	/**
	 * Database connection
	 */
	private static SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param c Context
	 * @return SQLiteDatabase Db
	 * </pre>
	 */
	public static SQLiteDatabase getDb(Context c) {
		if (db == null) {
			db = SQLiteDatabase.openDatabase(c.getDatabasePath(Const.db)
					.toString(), null, SQLiteDatabase.OPEN_READWRITE
					| SQLiteDatabase.CREATE_IF_NECESSARY);
		}
		return db;
	}
}