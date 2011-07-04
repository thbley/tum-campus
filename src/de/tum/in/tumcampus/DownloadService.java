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
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.FeedManager;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.models.Utils;

public class DownloadService extends IntentService {

	private volatile boolean destroyed = false;

	private NotificationManager mNotificationManager;

	private Notification notification;

	public DownloadService() {
		super("DownloadService");
	}

	final static String db = "database.db";

	String message = "";

	@Override
	protected void onHandleIntent(Intent intent) {

		// TODO show progress bar in GUI
		// TODO avoid database locking / deadlocking exceptions
		// TODO move constants to class header
		// TODO add locking

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
					"1/5", contentIntent);
			mNotificationManager.notify(1, notification);
			message("Aktualisiere: Mensen", "");

			CafeteriaManager cm = new CafeteriaManager(this, db);
			cm.downloadFromExternal();

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"2/5", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", Menus", "");

				CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, db);
				cmm.downloadFromExternal(cm.getAllIdsFromDb());
				cmm.close();
			}
			cm.close();

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"3/5", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", Veranstaltungen", "");

				EventManager em = new EventManager(this, db);
				em.downloadFromExternal();
				em.close();
			}

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"4/5", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", RSS", "");

				FeedManager nm = new FeedManager(this, db);
				FeedItemManager nim = new FeedItemManager(this, db);

				nim.downloadFromExternal(nm.getAllIdsFromDb());

				nim.close();
				nm.close();
			}

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"5/5", contentIntent);
				mNotificationManager.notify(1, notification);
				message(", News", "");

				NewsManager nm = new NewsManager(this, db);
				nm.downloadFromExternal();
				nm.close();
			}

			if (!destroyed) {
				LinkManager lm = new LinkManager(this, db);
				lm.checkExistingIcons();
				lm.downloadMissingIcons();
				lm.close();
			}

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