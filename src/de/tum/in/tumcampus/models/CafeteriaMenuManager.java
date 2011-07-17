package de.tum.in.tumcampus.models;

import static de.tum.in.tumcampus.models.Utils.getDate;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.tum.in.tumcampus.Const;

public class CafeteriaMenuManager extends SQLiteOpenHelper {

	private SQLiteDatabase db;

	public static int lastInserted = 0;

	public CafeteriaMenuManager(Context context, String database) {
		super(context, database, null, Const.dbVersion);

		db = getWritableDatabase();
		onCreate(db);
	}

	public void downloadFromExternal(List<Integer> ids, boolean force)
			throws Exception {

		if (!force && !SyncManager.needSync(db, this, 86400)) {
			return;
		}
		cleanupDb();
		int count = Utils.getCount(db, "cafeterias_menus");

		for (int id : ids) {
			Cursor c = db.rawQuery("SELECT 1 FROM cafeterias_menus "
					+ "WHERE cafeteriaId = ? AND "
					+ "date > date('now', '+7 day') LIMIT 1",
					new String[] { String.valueOf(id) });

			if (c.getCount() > 0) {
				c.close();
				continue;
			}
			c.close();

			String url = "http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php?mensa_id=";
			JSONObject json = Utils.downloadJson(url + id);

			db.beginTransaction();
			JSONArray menu = json.getJSONArray("mensa_menu");
			for (int j = 0; j < menu.length(); j++) {
				replaceIntoDb(getFromJson(menu.getJSONObject(j)));
			}

			JSONArray beilagen = json.getJSONArray("mensa_beilagen");
			for (int j = 0; j < beilagen.length(); j++) {
				replaceIntoDb(getFromJsonAddendum(beilagen.getJSONObject(j)));
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		SyncManager.replaceIntoDb(db, this);

		lastInserted += Utils.getCount(db, "cafeterias_menus") - count;
	}

	public Cursor getDatesFromDb() {
		return db.rawQuery(
				"SELECT DISTINCT strftime('%d.%m.%Y', date) as date_de, date as _id "
						+ "FROM cafeterias_menus WHERE "
						+ "date >= date() ORDER BY date", null);
	}

	public Cursor getTypeNameFromDb(String cafeteriaId, String date) {
		return db
				.rawQuery(
						"SELECT typeLong, group_concat(name, '\n') as names, id as _id "
								+ "FROM cafeterias_menus WHERE cafeteriaId = ? AND "
								+ "date = ? GROUP BY typeLong ORDER BY typeNr, typeLong, name",
						new String[] { cafeteriaId, date });
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
	 * @throws Exception
	 */
	public static CafeteriaMenu getFromJson(JSONObject json) throws Exception {

		return new CafeteriaMenu(json.getInt("id"), json.getInt("mensa_id"),
				Utils.getDate(json.getString("date")),
				json.getString("type_short"), json.getString("type_long"),
				json.getInt("type_nr"), json.getString("name"));
	}

	public static CafeteriaMenu getFromJsonAddendum(JSONObject json)
			throws Exception {

		return new CafeteriaMenu(0, json.getInt("mensa_id"), Utils.getDate(json
				.getString("date")), json.getString("type_short"),
				json.getString("type_long"), 10, json.getString("name"));
	}

	public void replaceIntoDb(CafeteriaMenu c) throws Exception {
		if (c.cafeteriaId <= 0) {
			throw new Exception("Invalid cafeteriaId.");
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
				"REPLACE INTO cafeterias_menus (id, cafeteriaId, date, typeShort, "
						+ "typeLong, typeNr, name) VALUES (?, ?, ?, ?, ?, ?, ?)",
				new String[] { String.valueOf(c.id),
						String.valueOf(c.cafeteriaId),
						Utils.getDateString(c.date), c.typeShort, c.typeLong,
						String.valueOf(c.typeNr), c.name });
	}

	public void removeCache() {
		db.execSQL("DELETE FROM cafeterias_menus");
	}

	public void cleanupDb() {
		db.execSQL("DELETE FROM cafeterias_menus WHERE date < date('now','-7 day')");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO rename column cafeteriaId on upgrade
		db.execSQL("CREATE TABLE IF NOT EXISTS cafeterias_menus ("
				+ "id INTEGER, cafeteriaId INTEGER, date VARCHAR, typeShort VARCHAR, "
				+ "typeLong VARCHAR, typeNr INTEGER, name VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}