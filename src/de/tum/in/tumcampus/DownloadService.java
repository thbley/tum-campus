package de.tum.in.tumcampus;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.EventManager;
import de.tum.in.tumcampus.models.Feed;
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.FeedManager;
import de.tum.in.tumcampus.models.Link;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.TransportManager;
import de.tum.in.tumcampus.models.Utils;

public class DownloadService extends IntentService {

	private volatile boolean destroyed = false;

	private NotificationManager mNotificationManager;

	private Notification notification;

	public DownloadService() {
		super("DownloadService");
	}

	String message = "";

	@Override
	protected void onHandleIntent(Intent intent) {

		// TODO show progress bar in GUI
		// TODO avoid database locking / deadlocking exceptions
		// TODO move constants to class header

		Log.d("TumCampus DownloadService", "TumCampus service start");

		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);

		notification = new Notification(android.R.drawable.stat_sys_download,
				"Aktualisiere ...", System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, TumCampus.class), 0);

		try {
			// check if sd card available
			Utils.getCacheDir("");
			
			notification.setLatestEventInfo(this, "TumCampus download ...",
					"1/4", contentIntent);
			mNotificationManager.notify(1, notification);
			message("Aktualisiere: Mensen", "");

			CafeteriaManager cm = new CafeteriaManager(this, "database.db");
			cm.downloadFromExternal();

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"2/4", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", Menus", "");

				CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
						"database.db");
				cmm.downloadFromExternal(cm.getAllIdsFromDb());
				cmm.close();
			}
			cm.close();

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"3/4", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", Veranstaltungen", "");

				EventManager em = new EventManager(this, "database.db");
				em.downloadFromExternal();
				em.close();
			}

			if (!destroyed) {
				FeedManager nm = new FeedManager(this, "database.db");
				nm.downloadFromExternal();

				// TODO remove
				nm.insertIntoDb(new Feed(0, "Spiegel",
						"http://www.spiegel.de/schlagzeilen/index.rss"));
				nm.insertIntoDb(new Feed(0, "N-tv", "http://www.n-tv.de/rss"));
				nm.insertIntoDb(new Feed(0, "Zeit",
						"http://newsfeed.zeit.de/index"));
				nm.insertIntoDb(new Feed(0, "Golem",
						"http://rss.golem.de/rss.php?feed=RSS1.0"));
				nm.insertIntoDb(new Feed(0, "Heise",
						"http://www.heise.de/newsticker/heise.rdf"));

				notification.setLatestEventInfo(this, "TumCampus download ...",
						"4/4", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", RSS", "");

				FeedItemManager nim = new FeedItemManager(this, "database.db");

				nim.downloadFromExternal(nm.getAllIdsFromDb());
				nim.close();

				nm.close();
			}

			TransportManager tm = new TransportManager(this, "database.db");
			tm.replaceIntoDb("Garching-Forschungszentrum");
			tm.replaceIntoDb("Marienplatz");
			tm.replaceIntoDb("Ottobrunn");
			tm.close();

			LinkManager lm = new LinkManager(this, "database.db");
			lm.downloadFromExternal();

			// TODO remove
			String target = Utils.getCacheDir("links/cache") + "1.ico";
			Utils.downloadIconFile("http://m.spiegel.de/", target);
			lm.insertIntoDb(new Link(0, "Spiegel", "http://m.spiegel.de/",
					target));

			target = Utils.getCacheDir("links/cache") + "2.ico";
			Utils.downloadIconFile("http://www.n-tv.de/", target);
			lm.insertIntoDb(new Link(0, "N-tv", "http://www.n-tv.de/", target));

			target = Utils.getCacheDir("links/cache") + "3.ico";
			Utils.downloadIconFile("http://www.zeit.de/", target);
			lm.insertIntoDb(new Link(0, "Zeit", "http://www.zeit.de/", target));

			target = Utils.getCacheDir("links/cache") + "4.ico";
			Utils.downloadIconFile("http://golem.mobi/", target);
			lm.insertIntoDb(new Link(0, "Golem", "http://golem.mobi/", target));

			target = Utils.getCacheDir("links/cache") + "5.ico";
			Utils.downloadIconFile("http://www.heise.de/", target);
			lm.insertIntoDb(new Link(0, "Heise", "http://www.heise.de/", target));

			lm.close();

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			message(e.getMessage() + sw.toString(), "error");
		}

		mNotificationManager.cancel(1);

		message(", Fertig.", "completed");
	}

	public void message(String message, String action) {

		if (action.equals("error")) {
			this.message = "";
		}

		this.message += message;

		Intent intentSend = new Intent();
		intentSend
				.setAction("de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD");
		intentSend.putExtra("message", this.message);
		intentSend.putExtra("action", action);
		this.sendBroadcast(intentSend);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mNotificationManager.cancel(1);

		Log.d("TumCampus DownloadService", "TumCampus service destroy");

		destroyed = true;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("TumCampus DownloadService", "TumCampus service create");
	}
}