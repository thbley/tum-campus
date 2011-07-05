package de.tum.in.tumcampus.models;

import java.net.URLEncoder;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	private Context context;

	public NewsManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);
		this.context = context;

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public void downloadFromExternal() throws Exception {

		String baseUrl = "https://graph.facebook.com/162327853831856/feed/?access_token=";
		String token = URLEncoder
				.encode("141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU");

		JSONArray jsonArray = Utils.downloadJson(baseUrl + token).getJSONArray(
				"data");

		cleanupDb();
		db.beginTransaction();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);

			// events, empty items
			if (obj.has("properties")
					|| (!obj.has("message") && !obj.has("description") && !obj
							.has("caption"))) {
				continue;
			}
			replaceIntoDb(getFromJson(obj));
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public Cursor getAllFromDb() {
		return db.rawQuery(
				"SELECT image, message, strftime('%d.%m.%Y', date) as date_de, "
						+ "link, id as _id FROM news ORDER BY date DESC", null);
	}

	/**
	 * 
	 * 
	 * Example JSON: e.g. { "id": "162327853831856_174943842570257", "from": {
	 * ... }, "message": "Testing ...", "picture":
	 * "http://photos-d.ak.fbcdn.net/hphotos-ak-ash4/268937_174943835903591_162327853831856_476156_7175901_s.jpg"
	 * , "link":
	 * "https://www.facebook.com/photo.php?fbid=174943835903591&set=a.174943832570258.47966.162327853831856&type=1"
	 * , "name": "Wall Photos", "icon":
	 * "http://static.ak.fbcdn.net/rsrc.php/v1/yz/r/StEh3RhPvjk.gif", "type":
	 * "photo", "object_id": "174943835903591", "created_time":
	 * "2011-07-04T01:58:25+0000", "updated_time": "2011-07-04T01:58:25+0000" },
	 * 
	 * @param json
	 * @return News
	 * @throws JSONException
	 */
	public News getFromJson(JSONObject json) throws Exception {

		String target = "";
		if (json.has("picture")) {
			String picture = json.getString("picture");
			target = Utils.getCacheDir("news/cache") + Utils.md5(picture)
					+ ".jpg";
			Utils.downloadFileQueue(context, db, picture, target);
		}
		String link = "";
		if (json.has("link")) {
			link = json.getString("link");
		}
		String message = "";
		if (json.has("message")) {
			message = json.getString("message");
		} else if (json.has("description")) {
			message = json.getString("description");
		} else if (json.has("caption")) {
			message = json.getString("caption");
		}
		Date date = Utils.getDate(json.getString("created_time"));

		return new News(json.getString("id"), message, link, target, date);
	}

	public void replaceIntoDb(News n) throws Exception {
		Log.d("TumCampus news replaceIntoDb", n.toString());

		if (n.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (n.message.length() == 0) {
			throw new Exception("Invalid message.");
		}
		db.execSQL("REPLACE INTO news (id, message, link, image, date) "
				+ "VALUES (?, ?, ?, ?, ?)", new String[] { n.id, n.message,
				n.link, n.image, Utils.getDateString(n.date) });
	}

	public void removeCache() {
		Log.d("TumCampus news deleteAllFromDb", "");

		db.execSQL("DELETE FROM news");
		Utils.emptyCacheDir("news/cache");
	}

	public void cleanupDb() {
		db.execSQL("DELETE FROM news WHERE date < date('now','-1 month')");
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS news ("
				+ "id VARCHAR PRIMARY KEY, message VARCHAR, link VARCHAR, image VARCHAR, date VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}