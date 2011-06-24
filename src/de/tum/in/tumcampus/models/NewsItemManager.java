package de.tum.in.tumcampus.models;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsItemManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	public SQLiteDatabase db;

	public NewsItemManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
	}

	public void downloadFromExternal(List<Integer> ids) throws Exception {

		cleanupDb();
		db.beginTransaction();
		for (int i = 0; i < ids.size(); i++) {
			deleteFromDb(ids.get(i));

			Cursor feed = db.rawQuery("SELECT feedUrl FROM news WHERE id = ?",
					new String[] { String.valueOf(ids.get(i)) });
			feed.moveToNext();
			String feedUrl = feed.getString(0);
			feed.close();

			String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
			String query = URLEncoder
					.encode("select title, link, description, pubDate, enclosure.url from rss where url=\""
							+ feedUrl + "\" limit 25");

			JSONArray jsonArray = Utils.downloadJson(baseUrl + query)
					.getJSONObject("query").getJSONObject("results")
					.getJSONArray("item");

			for (int j = 0; j < jsonArray.length(); j++) {
				insertIntoDb(getFromJson(ids.get(i), jsonArray.getJSONObject(j)));
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public List<NewsItem> getAllFromDb(int feedId) {
		List<NewsItem> list = new ArrayList<NewsItem>();

		Cursor c = db.rawQuery(
				"SELECT * FROM news_items WHERE feedId = ? ORDER BY date desc "
						+ "LIMIT 25", new String[] { String.valueOf(feedId) });

		while (c.moveToNext()) {
			list.add(new NewsItem(c.getInt(c.getColumnIndex("feedId")), c
					.getString(c.getColumnIndex("title")), c.getString(c
					.getColumnIndex("link")), c.getString(c
					.getColumnIndex("description")), Utils.getDate(c
					.getString(c.getColumnIndex("date"))), c.getString(c
					.getColumnIndex("image"))));
		}
		c.close();
		return list;
	}

	/**
	 * 
	 * 
	 * Example JSON: e.g. { "title":
	 * "US-Truppenabzug aus Afghanistan: \"Verlogen und verkorkst\"",
	 * "description":
	 * "Die USA werden konkret. Präsident Obama verkündet den Abzug der ersten Soldaten. Die deutsche Presse attestiert ihm, vor allem seine Wiederwahl im Blick zu haben. Dass Afghanistan bald wieder im Bürgerkrieg versinken könnte, scheint da nebensächlich."
	 * , "link":
	 * "http://www.n-tv.de/politik/pressestimmen/Verlogen-und-verkorkst-article3650731.html"
	 * , "pubDate": "Thu, 23 Jun 2011 20:06:53 GMT", "enclosure": { "url":
	 * "http://www.n-tv.de/img/30/304801/Img_4_3_220_Pressestimmen.jpg" }
	 * 
	 * @param json
	 * @return News
	 * @throws JSONException
	 */
	public static NewsItem getFromJson(int feedId, JSONObject json)
			throws Exception {

		String enclosure = "";
		if (json.has("enclosure")) {
			enclosure = json.getJSONObject("enclosure").getString("url");

			// TODO add download queue + extra thread?
			String target = Utils.getCacheDir("rss") + Utils.md5(enclosure)
					+ ".jpg";
			enclosure = Utils.downloadFile(enclosure, target);
		}
		Date pubDate = new Date();
		if (json.has("pubDate")) {
			pubDate = Utils.getDate(json.getString("pubDate"));
		}
		String description = "";
		if (json.has("description")) {
			description = json.getString("description").replaceAll("\\<.*?\\>", "");
		}

		return new NewsItem(feedId, json.getString("title"),
				json.getString("link"), description, pubDate, enclosure);
	}

	public void insertIntoDb(NewsItem n) throws Exception {
		Log.d("TumCampus news replaceIntoDb", n.toString());

		if (n.feedId == 0) {
			throw new Exception("Invalid feedId.");
		}
		if (n.link.length() == 0) {
			throw new Exception("Invalid link.");
		}
		if (n.title.length() == 0) {
			throw new Exception("Invalid title.");
		}
		db.execSQL(
				"INSERT INTO news_items (feedId, title, link, description, date, image) VALUES (?, ?, ?, ?, ?, ?)",
				new String[] { String.valueOf(n.feedId), n.title, n.link,
						n.description, Utils.getDateString(n.date), n.image });
	}

	public void deleteAllFromDb() {
		Log.d("TumCampus news deleteAllFromDb", "");
		db.execSQL("DELETE FROM news_items");
	}

	public void deleteFromDb(int feedId) {
		db.execSQL("DELETE FROM news_items WHERE feedId = ?",
				new String[] { String.valueOf(feedId) });
	}

	public void cleanupDb() {
		db.execSQL("DELETE FROM news_items WHERE date < date('now','-1 week')");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS news_items ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, feedId INTEGER, title VARCHAR, link VARCHAR, description VARCHAR, date VARCHAR, image VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}