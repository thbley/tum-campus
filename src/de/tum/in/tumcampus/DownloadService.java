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
import de.tum.in.tumcampus.models.Links;
import de.tum.in.tumcampus.models.LinksManager;
import de.tum.in.tumcampus.models.News;
import de.tum.in.tumcampus.models.NewsItemManager;
import de.tum.in.tumcampus.models.NewsManager;

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

		Log.d("TumCampus DownloadService", "TumCampus service start");

		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);

		notification = new Notification(android.R.drawable.stat_sys_download,
				"Aktualisiere ...", System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, TumCampus.class), 0);

		try {
			notification.setLatestEventInfo(this, "TumCampus download ...",
					"1/3", contentIntent);
			mNotificationManager.notify(1, notification);
			message("Aktualisiere: Mensen", "");

			CafeteriaManager cm = new CafeteriaManager(this, "database.db");
			cm.downloadFromExternal();

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"2/3", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", Menus", "");

				CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
						"database.db");
				cmm.downloadFromExternal(cm.getAllIdsFromDb());
				cmm.close();
			}
			cm.close();

			if (!destroyed) {
				NewsManager nm = new NewsManager(this, "database.db");

				// TODO remove
				nm.replaceIntoDb(new News(1, "Spiegel",
						"http://www.spiegel.de/schlagzeilen/index.rss"));
				nm.replaceIntoDb(new News(2, "N-tv", "http://www.n-tv.de/rss"));
				nm.replaceIntoDb(new News(3, "Zeit",
						"http://newsfeed.zeit.de/index"));
				nm.replaceIntoDb(new News(4, "Golem",
						"http://rss.golem.de/rss.php?feed=RSS1.0"));
				nm.replaceIntoDb(new News(5, "Heise",
						"http://www.heise.de/newsticker/heise.rdf"));

				notification.setLatestEventInfo(this, "TumCampus download ...",
						"3/3", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", RSS", "");

				NewsItemManager nim = new NewsItemManager(this, "database.db");

				nim.downloadFromExternal(nm.getAllIdsFromDb());
				nim.close();

				nm.close();
			}

			// TODO remove
			LinksManager lm = new LinksManager(this, "database.db");
			lm.replaceIntoDb(new Links(1, "Spiegel", "http://www.spiegel.de/"));
			lm.replaceIntoDb(new Links(2, "N-tv", "http://www.n-tv.de/"));
			lm.replaceIntoDb(new Links(3, "Zeit", "http://www.zeit.de/"));
			lm.replaceIntoDb(new Links(4, "Golem", "http://www.golem.de/"));
			lm.replaceIntoDb(new Links(5, "Heise", "http://www.heise.de/"));
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
