package de.tum.in.tumcampus.models;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.tum.in.tumcampus.Const;

/**
 * Gallery Manager, handles database stuff, external imports
 */
public class GalleryManager extends SQLiteOpenHelper {

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * @param database Filename, e.g. database.db
	 * </pre>
	 */
	public GalleryManager(Context context, String database) {
		super(context, database, null, Const.dbVersion);

		db = getWritableDatabase();
		onCreate(db);
	}

	/**
	 * Download Gallery from external interface (JSON)
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * @throws Exception
	 * </pre>
	 */
	public void downloadFromExternal(boolean force) throws Exception {

		if (!force && !SyncManager.needSync(db, this, 21600)) { // 6h
			return;
		}

		String url = "https://graph.facebook.com/280074732057167/photos?"
				+ "fields=id,name,source,position&limit=50&access_token=";
		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

		JSONArray jsonArray = Utils
				.downloadJson(url + URLEncoder.encode(token)).getJSONArray(
						"data");

		int count = Utils.dbGetTableCount(db, "gallery");

		db.beginTransaction();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				replaceIntoDb(getFromJson(jsonArray.getJSONObject(i)));
			}
			SyncManager.replaceIntoDb(db, this);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "gallery") - count;
	}

	/**
	 * Get all gallery item from the database
	 * 
	 * @return Database cursor (_id, id)
	 */
	public Cursor getFromDb() {
		return db
				.rawQuery(
						"SELECT image as _id, id FROM gallery ORDER BY position LIMIT 50",
						null);
	}

	/**
	 * Get a gallery item from the database
	 * 
	 * <pre>
	 * @param id Item-ID
	 * @return Database cursor (id, name, image, position)
	 * </pre>
	 */
	public Cursor getDetailsFromDb(String id) {
		return db.rawQuery("SELECT * FROM gallery WHERE id=?",
				new String[] { id });
	}

	/**
	 * Convert JSON object to Gallery, download gallery picture
	 * 
	 * <pre>
	 * Example JSON: e.g.  {
	 *          "id": "280076022057038",
	 *          "name": "Poker Turnier 30.1.2012, 16:30",
	 *          "source": "http://a1.sphotos.ak.fbcdn.net/...jpg",
	 *          "position": 1,
	 *          "created_time": "2012-01-21T19:38:25+0000"
	 *       },
	 * 
	 * @param json see above
	 * @return Gallery
	 * @throws Exception
	 * </pre>
	 */
	public static Gallery getFromJson(JSONObject json) throws Exception {

		String id = json.getString("id");

		String picture = json.getString("source");

		String target = Utils.getCacheDir("gallery/cache") + id + ".jpg";
		Utils.downloadFileThread(picture, target);

		return new Gallery(id, json.getString("name"), target,
				json.getString("position"));
	}

	/**
	 * Replace or Insert a gallery item in the database
	 * 
	 * <pre>
	 * @param e Gallery object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(Gallery e) throws Exception {
		if (e.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (e.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		db.execSQL("REPLACE INTO gallery (id, name, image, position) "
				+ "VALUES (?, ?, ?, ?)", new String[] { e.id, e.name, e.image,
				e.position });
	}

	/**
	 * Removes all cache items
	 */
	public void removeCache() {
		db.execSQL("DELETE FROM gallery");
		Utils.emptyCacheDir("gallery/cache");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS gallery ("
				+ "id VARCHAR PRIMARY KEY, name VARCHAR, image VARCHAR, "
				+ "position INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}