package de.tum.in.tumcampus.models;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TransportManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	public SQLiteDatabase db;

	public TransportManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public List<Map<String, String>> getFromExternal(String location)
			throws Exception {
		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle="
				+ location;
		String query = URLEncoder
				.encode("select content from html where url=\"" + lookupUrl
						+ "\" and xpath=\"//td[contains(@class,'Column')]/p\"");

		JSONArray jsonArray = Utils.downloadJson(baseUrl + query)
				.getJSONObject("query").getJSONObject("results")
				.getJSONArray("p");

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		for (int j = 2; j < jsonArray.length(); j = j + 3) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name",
					jsonArray.getString(j) + " "
							+ jsonArray.getString(j + 1).trim());
			map.put("desc", jsonArray.getString(j + 2) + " min");
			list.add(map);
		}
		return list;
	}

	public Cursor getAllFromDb() {
		return db.rawQuery("SELECT name, name as _id " + "FROM transports "
				+ "ORDER BY name", null);
	}

	public void replaceIntoDb(String name) throws Exception {
		Log.d("TumCampus transports replaceIntoDb", name);

		if (name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		db.execSQL("REPLACE INTO transports (name) VALUES (?)",
				new String[] { name });
	}

	public void deleteAllFromDb() {
		Log.d("TumCampus transports deleteAllFromDb", "");

		db.execSQL("DELETE FROM transports");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS transports ("
				+ "name VARCHAR PRIMARY KEY)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}