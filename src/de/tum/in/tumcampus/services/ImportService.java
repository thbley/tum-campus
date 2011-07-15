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
import de.tum.in.tumcampus.Const;
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

			notification.setLatestEventInfo(this, "TUMCampus import ...", "",
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
				message(e, "");
			}
			nm.cancel(1);
		}
	}

	public void importFeeds() {
		FeedManager nm = new FeedManager(this, Const.db);
		try {
			nm.importFromInternal();
		} catch (Exception e) {
			message(e, nm.lastInfo);
		}
		nm.close();
	}

	public void importLinks() {
		LinkManager lm = new LinkManager(this, Const.db);
		try {
			lm.importFromInternal();
		} catch (Exception e) {
			message(e, lm.lastInfo);
		}
		lm.close();
	}

	public void importLectureItems() {
		LectureItemManager lim = new LectureItemManager(this, Const.db);
		try {
			lim.importFromInternal();
		} catch (Exception e) {
			message(e, lim.lastInfo);
		}
		lim.close();

		LectureManager lm = new LectureManager(this, Const.db);
		lm.updateLectures();
		lm.close();
	}

	public void importTransportsDefaults() throws Exception {

		TransportManager tm = new TransportManager(this, Const.db);
		if (tm.empty()) {
			List<String[]> rows = Utils.readCsv(
					getAssets().open("transports.csv"), "ISO-8859-1");

			for (String[] row : rows) {
				tm.replaceIntoDb(row[0]);
			}
		}
		tm.close();
	}

	public void importFeedsDefaults() throws Exception {

		FeedManager nm = new FeedManager(this, Const.db);
		if (nm.empty()) {
			List<String[]> rows = Utils.readCsv(getAssets().open("feeds.csv"),
					"ISO-8859-1");

			for (String[] row : rows) {
				nm.insertUpdateIntoDb(new Feed(row[0], row[1]));
			}
		}
		nm.close();
	}

	public void importLectureItemsDefaults() throws Exception {
		LectureItemManager lim = new LectureItemManager(this, Const.db);
		if (lim.empty()) {
			List<String[]> rows = Utils.readCsv(
					getAssets().open("lectures_holidays.csv"), "ISO-8859-1");

			for (String[] row : rows) {
				lim.replaceIntoDb(new LectureItem.Holiday(row[0], Utils
						.getDate(row[1]), row[2]));
			}

			rows = Utils.readCsv(getAssets().open("lectures_vacations.csv"),
					"ISO-8859-1");

			for (String[] row : rows) {
				lim.replaceIntoDb(new LectureItem.Vacation(row[0], Utils
						.getDate(row[1]), Utils.getDate(row[2]), row[3]));
			}
		}
		lim.close();

		LectureManager lm = new LectureManager(this, Const.db);
		lm.updateLectures();
		lm.close();
	}

	public void importLinksDefaults() throws Exception {
		LinkManager lm = new LinkManager(this, Const.db);
		if (lm.empty()) {
			List<String[]> rows = Utils.readCsv(getAssets().open("links.csv"),
					"ISO-8859-1");

			for (String[] row : rows) {
				lm.insertUpdateIntoDb(new Link(row[0], row[1]));
			}
		}
		lm.close();
	}

	public void message(Exception e, String info) {
		Utils.Log(e, info);

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		String message = e.getMessage();
		if (Utils.getSettingBool(this, Const.settings.debug)) {
			message += sw.toString();
		}
		message(info + " " + message, "error");
	}

	public void message(String message, String action) {
		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra("message", message);
		intentSend.putExtra("action", action);
		sendBroadcast(intentSend);
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