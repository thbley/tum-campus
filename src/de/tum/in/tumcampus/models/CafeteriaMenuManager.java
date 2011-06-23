package de.tum.in.tumcampus.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CafeteriaMenuManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;

	private SQLiteDatabase db;

	public CafeteriaMenuManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
	}

	public void downloadFromExternal(List<Integer> ids) throws Exception {

		cleanupDb();
		for (int i = 0; i < ids.size(); i++) {
			db.beginTransaction();
			Cursor c = db.rawQuery("SELECT 1 FROM cafeterias_menus "
					+ "WHERE mensaId = ? AND "
					+ "date > date('now', '+7 day') LIMIT 1",
					new String[] { String.valueOf(ids.get(i)) });

			if (c.getCount() > 0) {
				c.close();
				continue;
			}

			JSONObject json = Utils
					.downloadJson("http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php?mensa_id="
							+ ids.get(i));

			JSONArray menu = json.getJSONArray("mensa_menu");
			for (int j = 0; j < menu.length(); j++) {
				replaceIntoDb(getFromJson(menu.getJSONObject(j)));
			}

			JSONArray beilagen = json.getJSONArray("mensa_beilagen");
			for (int j = 0; j < beilagen.length(); j++) {
				replaceIntoDb(getFromJsonAddendum(beilagen.getJSONObject(j)));
			}

			// TODO crawl prices
			// http://www.studentenwerk-muenchen.de/mensa/unsere-preise/

			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}

	public List<HashMap<String, String>> getFromDb(String mensaId, String date) {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		Cursor c = db.rawQuery("SELECT typeLong, group_concat(name, '\n') "
				+ "FROM cafeterias_menus WHERE mensaId = ? AND "
				+ "date = ? GROUP BY typeLong ORDER BY typeNr, typeLong, name",
				new String[] { mensaId, date });

		while (c.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("typeLong", c.getString(0));
			map.put("names", c.getString(1));
			list.add(map);
		}
		c.close();
		return list;
	}

	// TODO add mensa_id as param?
	public List<CafeteriaMenu> getFromDb(Date begin, Date end) throws Exception {
		List<CafeteriaMenu> list = new ArrayList<CafeteriaMenu>();

		Cursor c = db
				.rawQuery(
						"SELECT * FROM cafeterias_menus WHERE date BETWEEN ? AND ? ORDER BY date, mensaId, typeNr",
						new String[] { getDateString(begin), getDateString(end) });

		while (c.moveToNext()) {
			list.add(new CafeteriaMenu(c.getInt(c.getColumnIndex("id")), c
					.getInt(c.getColumnIndex("mensaId")), getDate(c.getString(c
					.getColumnIndex("date"))), c.getString(c
					.getColumnIndex("typeShort")), c.getString(c
					.getColumnIndex("typeLong")), c.getInt(c
					.getColumnIndex("typeNr")), c.getString(c
					.getColumnIndex("name"))));
		}
		c.close();
		return list;
	}

	/**
	 * 
	 * 
	 * Example JSON: e.g.
	 * {"id":"25544","mensa_id":"411","date":"2011-06-20","type_short"
	 * :"tg","type_long":"Tagesgericht 3","type_nr":"3","name":
	 * "Cordon bleu vom Schwein (mit Formfleischhinterschinken) (S) (1,2,3,8)"}
	 * 
	 * @param json
	 * @return CafeteriaMenu
	 * @throws JSONException
	 */
	public static CafeteriaMenu getFromJson(JSONObject json) throws Exception {

		return new CafeteriaMenu(json.getInt("id"), json.getInt("mensa_id"),
				getDate(json.getString("date")), json.getString("type_short"),
				json.getString("type_long"), json.getInt("type_nr"),
				json.getString("name"));
	}

	public static CafeteriaMenu getFromJsonAddendum(JSONObject json)
			throws Exception {

		return new CafeteriaMenu(0, json.getInt("mensa_id"),
				getDate(json.getString("date")), json.getString("type_short"),
				json.getString("type_long"), 10, json.getString("name"));
	}

	public void replaceIntoDb(CafeteriaMenu c) throws Exception {
		Log.d("TumCampus cafeterias_menus replaceIntoDb", c.toString());

		if (c.mensaId <= 0) {
			throw new Exception("Invalid mensaId.");
		}
		if (c.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (c.typeLong.length() == 0) {
			throw new Exception("Invalid typeLong.");
		}
		if (c.typeShort.length() == 0) {
			throw new Exception("Invalid typeShort.");
		}
		if (c.date.before(getDate("2011-01-01"))) {
			throw new Exception("Invalid date.");
		}

		db.execSQL(
				"REPLACE INTO cafeterias_menus (id, mensaId, date, typeShort, typeLong, typeNr, name) VALUES (?, ?, ?, ?, ?, ?, ?)",
				new String[] { String.valueOf(c.id), String.valueOf(c.mensaId),
						getDateString(c.date), c.typeShort, c.typeLong,
						String.valueOf(c.typeNr), c.name });
	}

	public void deleteAllFromDb() {
		db.execSQL("DELETE FROM cafeterias_menus");
	}

	public void cleanupDb() {
		db.execSQL("DELETE FROM cafeterias_menus WHERE date < date('now','-1 week')");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias_menus ("
				+ "id INTEGER, mensaId INTEGER, date VARCHAR, typeShort VARCHAR, typeLong VARCHAR, typeNr INTEGER, name VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	private static Date getDate(String s) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.parse(s);
	}

	public static String getDateString(Date d) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(d);
	}
}