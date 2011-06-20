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

public class DownloadService extends IntentService {

	private volatile boolean destroyed = false;

	private NotificationManager mNotificationManager;

	private Notification notification;

	public DownloadService() {
		super("DownloadService");
	}

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
					"1/2", contentIntent);
			mNotificationManager.notify(1, notification);
			message("Aktualisiere: Mensen", "");

			CafeteriaManager cm = new CafeteriaManager(
					this.getApplicationContext(), "database.db");
			cm.downloadFromExternal();

			if (!destroyed) {
				notification.setLatestEventInfo(this, "TumCampus download ...",
						"2/2", contentIntent);
				mNotificationManager.notify(1, notification);
				message("Aktualisiere: Mensen, Menus", "");

				CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
						"database.db");
				cmm.downloadFromExternal(cm.getAllIdsFromDb());
				cmm.close();
			}
			cm.close();

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			message(e.getMessage() + sw.toString(), "");
		}

		mNotificationManager.cancel(1);

		message("Aktualisiere: Mensen, Menus, Fertig.", "completed");
	}

	public void message(String message, String action) {
		Intent intentSend = new Intent();
		intentSend
				.setAction("de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD");
		intentSend.putExtra("message", message);
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
