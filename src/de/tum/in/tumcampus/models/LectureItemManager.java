package de.tum.in.tumcampus.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LectureItemManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	public SQLiteDatabase db;

	public LectureItemManager(Context context, String database) {
		super(context, database, null, DATABASE_VERSION);

		db = this.getWritableDatabase();
		onCreate(db);
	}

	public List<String> importFromInternal() throws Exception {
		List<String> list = new ArrayList<String>();

		File[] files = new File(Utils.getCacheDir("lectures")).listFiles();

		// TODO fix exceptions, db locking
		db.beginTransaction();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".csv")) {
				importCsv(files[i], "ISO-8859-1");
				list.add(files[i].getName());
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public void importCsv(File file, String encoding) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), encoding));

		Vector<String> headers = new Vector<String>();
		String reader = "";
		while ((reader = in.readLine()) != null) {
			// TODO fix splitting
			String[] row = reader.replaceAll("\"", "").split(";");

			if (headers.size() == 0) {
				headers = new Vector<String>(Arrays.asList(row));
				continue;
			}

			int terminTypId = headers.indexOf("TERMIN_TYP");
			if (row.length > terminTypId
					&& row[terminTypId].contains("abgesagt")) {
				continue;
			}

			String name = row[headers.indexOf("TITEL")];
			String location = row[headers.indexOf("ORT")];
			String lectureId = row[headers.indexOf("LV_NUMMER")];

			String module = "";
			if (name.contains("(") && name.contains(")")) {
				module = name.substring(name.indexOf("(") + 1,
						name.indexOf(")"));
				name = name.substring(0, name.indexOf("(")).trim();
			}

			String datum = row[headers.indexOf("DATUM")];
			String von = row[headers.indexOf("VON")];
			String bis = row[headers.indexOf("BIS")];

			Date start = Utils.getDateTimeDe(datum + " " + von);
			Date end = Utils.getDateTimeDe(datum + " " + bis);

			String id = row[headers.indexOf("LV_NUMMER")] + "_"
					+ String.valueOf(start.getTime());

			String seriesId = row[headers.indexOf("LV_NUMMER")] + "_"
					+ row[headers.indexOf("WOCHENTAG")] + "_"
					+ row[headers.indexOf("VON")];

			String note = "";
			int noteId = headers.indexOf("ANMERKUNG");
			if (row.length > noteId) {
				note = row[noteId];
			}
			String url = "";
			int urlId = headers.indexOf("URL");
			if (urlId != -1 && row.length > urlId) {
				url = row[headers.indexOf("URL")];
			}
			replaceIntoDb(new LectureItem(id, lectureId, start, end, name,
					module, location, note, url, seriesId));
		}
		in.close();
	}

	public Cursor getRecentFromDb() {
		return db.rawQuery("SELECT name, note, location, "
				+ "strftime('%w', start) as weekday, "
				+ "strftime('%H:%M', start) as start_de, "
				+ "strftime('%H:%M', end) as end_de, "
				+ "strftime('%d.%m.%Y', start) as start_dt, "
				+ "strftime('%d.%m.%Y', end) as end_dt, "
				+ "url, lectureId, id as _id "
				+ "FROM lectures_items WHERE end > datetime() AND "
				+ "start < date('now', '+7 day') ORDER BY start", null);
	}

	public Cursor getAllFromDb(String lectureId) {
		return db.rawQuery("SELECT name, note, location, "
				+ "strftime('%w', start) as weekday, "
				+ "strftime('%d.%m.%Y %H:%M', start) as start_de, "
				+ "strftime('%H:%M', end) as end_de, "
				+ "strftime('%d.%m.%Y', start) as start_dt, "
				+ "strftime('%d.%m.%Y', end) as end_dt, "
				+ "url, lectureId, id as _id "
				+ "FROM lectures_items WHERE lectureId = ? ORDER BY start",
				new String[] { lectureId });
	}

	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM lectures_items LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	public void replaceIntoDb(LectureItem l) throws Exception {
		Log.d("TumCampus lectureitems replaceIntoDb", l.toString());

		if (l.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (l.lectureId.length() == 0) {
			throw new Exception("Invalid lectureId.");
		}
		if (l.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (l.seriesId.length() == 0) {
			throw new Exception("Invalid id.");
		}

		db.execSQL(
				"REPLACE INTO lectures_items (id, lectureId, start, end, "
						+ "name, module, location, note, url, seriesId) VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new String[] { l.id, l.lectureId,
						Utils.getDateTimeString(l.start),
						Utils.getDateTimeString(l.end), l.name, l.module,
						l.location, l.note, l.url, l.seriesId });
	}

	public void deleteItemFromDb(String id) {
		db.execSQL("DELETE FROM lectures_items WHERE id = ?",
				new String[] { id });
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS lectures_items ("
				+ "id VARCHAR PRIMARY KEY, lectureId VARCHAR, start VARCHAR, "
				+ "end VARCHAR, name VARCHAR, module VARCHAR, location VARCHAR, "
				+ "note VARCHAR, url VARCHAR, seriesId VARCHAR)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}