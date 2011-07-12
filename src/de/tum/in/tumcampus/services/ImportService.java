package de.tum.in.tumcampus.services;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.tum.in.tumcampus.TumCampus;
import de.tum.in.tumcampus.models.Feed;
import de.tum.in.tumcampus.models.FeedManager;
import de.tum.in.tumcampus.models.LectureItem;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.LectureManager;
import de.tum.in.tumcampus.models.Link;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.TransportManager;
import de.tum.in.tumcampus.models.Utils;

public class ImportService extends IntentService {

	public ImportService() {
		super("ImportService");
	}

	final static String db = "database.db";

	public final static String broadcast = "de.tum.in.tumcampus.intent.action.BROADCAST_IMPORT";

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getStringExtra("action");
		Utils.Log(action);

		if (action.equals("defaults")) {
			try {
				importTransportsDefaults();
				importFeedsDefaults();
				importLinksDefaults();
				importLectureItemsDefaults();
			} catch (Exception e) {
				Utils.Log(e, "");
			}
		} else {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nm = (NotificationManager) getSystemService(ns);

			Notification notification = new Notification(
					android.R.drawable.stat_sys_download, "Importiere ...",
					System.currentTimeMillis());

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, TumCampus.class), 0);

			notification.setLatestEventInfo(this, "TumCampus import ...", "",
					contentIntent);
			nm.notify(1, notification);

			try {
				// check if sd card available
				Utils.getCacheDir("");

				if (action.equals("feeds")) {
					importFeeds();
				}
				if (action.equals("links")) {
					importLinks();
				}
				if (action.equals("lectures")) {
					importLectureItems();
				}
				message("Fertig!", "completed");
			} catch (Exception e) {
				message(e);
			}
			nm.cancel(1);
		}
	}

	public void importTransportsDefaults() throws Exception {

		TransportManager tm = new TransportManager(this, db);
		if (tm.empty()) {
			List<String[]> rows = Utils.readCsv(
					getAssets().open("transports.csv"), "ISO-8859-1");

			for (int i = 0; i < rows.size(); i++) {
				tm.replaceIntoDb(rows.get(i)[0]);
			}
		}
		tm.close();
	}

	public void importFeeds() throws Exception {
		FeedManager nm = new FeedManager(this, db);
		nm.importFromInternal();
		nm.close();
	}

	public void importFeedsDefaults() throws Exception {

		FeedManager nm = new FeedManager(this, db);
		if (nm.empty()) {
			List<String[]> rows = Utils.readCsv(getAssets().open("feeds.csv"),
					"ISO-8859-1");

			for (int i = 0; i < rows.size(); i++) {
				nm.insertUpdateIntoDb(new Feed(rows.get(i)[0], rows.get(i)[1]));
			}
		}
		nm.importFromInternal();
		nm.close();
	}

	public void importLectureItemsDefaults() throws Exception {
		LectureItemManager lim = new LectureItemManager(this, db);
		if (lim.empty()) {
			List<String[]> rows = Utils.readCsv(
					getAssets().open("lectures_holidays.csv"), "ISO-8859-1");

			for (int i = 0; i < rows.size(); i++) {
				lim.replaceIntoDb(new LectureItem.Holiday(rows.get(i)[0], Utils
						.getDate(rows.get(i)[1]), rows.get(i)[2]));
			}

			rows = Utils.readCsv(getAssets().open("lectures_vacations.csv"),
					"ISO-8859-1");

			for (int i = 0; i < rows.size(); i++) {
				lim.replaceIntoDb(new LectureItem.Vacation(rows.get(i)[0],
						Utils.getDate(rows.get(i)[1]), Utils.getDate(rows
								.get(i)[2]), rows.get(i)[3]));
			}
		}
		lim.close();

		LectureManager lm = new LectureManager(this, db);
		lm.updateLectures();
		lm.close();
	}

	public void importLectureItems() throws Exception {
		LectureItemManager lim = new LectureItemManager(this, db);
		lim.importFromInternal();
		lim.close();

		LectureManager lm = new LectureManager(this, db);
		lm.updateLectures();
		lm.close();
	}

	public void importLinksDefaults() throws Exception {
		LinkManager lm = new LinkManager(this, db);
		if (lm.empty()) {
			List<String[]> rows = Utils.readCsv(getAssets().open("links.csv"),
					"ISO-8859-1");
			for (int i = 0; i < rows.size(); i++) {
				lm.insertUpdateIntoDb(new Link(rows.get(i)[0], rows.get(i)[1]));
			}
		}
		lm.close();
	}

	public void importLinks() throws Exception {
		LinkManager lm = new LinkManager(this, db);
		lm.importFromInternal();
		lm.close();
	}

	public void message(Exception e) {
		Utils.Log(e, "");

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		String message = e.getMessage();
		if (Utils.getSettingBool(this, "debug")) {
			message += sw.toString();
		}
		message(message, "error");
	}

	public void message(String message, String action) {
		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra("message", message);
		intentSend.putExtra("action", action);
		this.sendBroadcast(intentSend);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.Log("");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.Log("");
	}
}