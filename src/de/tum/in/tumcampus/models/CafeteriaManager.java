package de.tum.in.tumcampus.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CafeteriaManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public CafeteriaManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public void downloadFromExternal(boolean force) throws Exception {
		if (!force && !SyncManager.needSync(db, this, 86400)) {
			return;
		}
		JSONArray jsonArray = Utils.downloadJson(
				"http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php")
				.getJSONArray("mensa_mensen");

		removeCache();
		db.beginTransaction();
		for (int i = 0; i < jsonArray.length(); i++) {
			replaceIntoDb(getFromJson(jsonArray.getJSONObject(i)));
		}
		SyncManager.replaceIntoDb(db, this);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public List<Integer> getAllIdsFromDb() {
		List<Integer> list = new ArrayList<Integer>();

		Cursor c = db.rawQuery("SELECT id FROM cafeterias ORDER BY id", null);

		while (c.moveToNext()) {
			list.add(c.getInt(0));
		}
		c.close();
		return list;
	}

	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, address, id as _id "
				+ "FROM cafeterias "
				+ "ORDER BY address like '%Garching%' desc, name", null);
	}

	/**
	 * 
	 * 
	 * Example JSON: e.g.
	 * {"id":"411","name":"Mensa Leopoldstra\u00dfe","anschrift"
	 * :"Leopoldstra\u00dfe 13a, M\u00fcnchen"}
	 * 
	 * @param json
	 * @return Cafeteria
	 * @throws JSONException
	 */
	public static Cafeteria getFromJson(JSONObject json) throws JSONException {

		return new Cafeteria(json.getInt("id"), json.getString("name"),
				json.getString("anschrift"));
	}

	public void replaceIntoDb(Cafeteria c) throws Exception {
		Log.d("TumCampus cafeterias replaceIntoDb", c.toString());

		if (c.id <= 0) {
			throw new Exception("Invalid id.");
		}
		if (c.name.length() == 0) {
			throw new Exception("Invalid name.");
		}

		db.execSQL(
				"REPLACE INTO cafeterias (id, name, address) VALUES (?, ?, ?)",
				new String[] { String.valueOf(c.id), c.name, c.address });
	}

	public void removeCache() {
		Log.d("TumCampus cafeterias deleteAllFromDb", "");
		db.execSQL("DELETE FROM cafeterias");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias ("
				+ "id INTEGER PRIMARY KEY, name VARCHAR, address VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}