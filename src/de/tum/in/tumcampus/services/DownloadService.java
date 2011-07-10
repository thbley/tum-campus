package de.tum.in.tumcampus.services;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import de.tum.in.tumcampus.TumCampus;
import de.tum.in.tumcampus.models.CafeteriaManager;
import de.tum.in.tumcampus.models.CafeteriaMenuManager;
import de.tum.in.tumcampus.models.EventManager;
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.FeedManager;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.models.SyncManager;
import de.tum.in.tumcampus.models.Utils;

public class DownloadService extends IntentService {

	private volatile boolean destroyed = false;

	public final static String broadcast = "de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD";

	public DownloadService() {
		super("DownloadService");
	}

	final static String db = "database.db";

	String message = "";

	public static BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(DownloadService.broadcast)) {
				return;
			}
			if (intent.getStringExtra("action").length() != 0) {
				Toast.makeText(context, intent.getStringExtra("message"),
						Toast.LENGTH_LONG).show();

				// TODO wait until pictures are loaded?

				// resume activity
				Intent intent2 = new Intent(context, context.getClass());
				intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(intent2);

				// unregister receiver
				context.unregisterReceiver(DownloadService.receiver);
			}
		}
	};

	@Override
	protected void onHandleIntent(Intent intent) {

		// TODO show progress bar in GUI

		// TODO avoid database locking / deadlocking exceptions
		// see
		// http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html
		// beginTransactionNonExclusive() => Api Level 11
		// enableWriteAheadLogging() => Api Level 11

		// TODO move constants to class header
		// TODO add locking

		Log.d("TumCampus DownloadService", "TumCampus service start");

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager) getSystemService(ns);

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download, "Aktualisiere ...",
				System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, TumCampus.class), 0);

		notification.setLatestEventInfo(this, "TumCampus download ...", "",
				contentIntent);
		nm.notify(1, notification);

		message("Aktualisiere: ", "");

		String action = intent.getStringExtra("action");

		boolean force = false;
		if (action != null) {
			force = true;
		}
		if (action == null || action.equals("feeds")) {
			downloadFeeds(force);
		}
		if (action == null || action.equals("news")) {
			downloadNews(force);
		}
		if (action == null || action.equals("events")) {
			downloadEvents(force);
		}
		if (action == null || action.equals("cafeterias")) {
			downloadCafeterias(force);
		}
		if (action == null || action.equals("links")) {
			downloadLinks();
		}
		message("Fertig!", "completed");
		nm.cancel(1);
	}

	public void downloadFeeds(boolean force) {
		try {
			if (!destroyed) {
				message("RSS ", "");

				// TODO refresh single?
				FeedManager nm = new FeedManager(this, db);
				FeedItemManager nim = new FeedItemManager(this, db);
				nim.downloadFromExternal(nm.getAllIdsFromDb(), force);
				nim.close();
				nm.close();
			}
		} catch (Exception e) {
			message(e);
		}
	}

	public void downloadNews(boolean force) {
		try {
			if (!destroyed) {
				message("Nachrichten ", "");

				NewsManager nm = new NewsManager(this, db);
				nm.downloadFromExternal(force);
				nm.close();
			}
		} catch (Exception e) {
			message(e);
		}
	}

	public void downloadEvents(boolean force) {
		try {
			if (!destroyed) {
				message("Veranstaltungen ", "");

				EventManager em = new EventManager(this, db);
				em.downloadFromExternal(force);
				em.close();
			}
		} catch (Exception e) {
			message(e);
		}
	}

	public void downloadCafeterias(boolean force) {
		try {
			if (!destroyed) {
				message("Mensen ", "");

				CafeteriaManager cm = new CafeteriaManager(this, db);
				cm.downloadFromExternal(force);

				CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, db);
				cmm.downloadFromExternal(cm.getAllIdsFromDb(), force);
				cmm.close();
				cm.close();
			}
		} catch (Exception e) {
			message(e);
		}
	}

	public void downloadLinks() {
		try {
			if (!destroyed) {
				LinkManager lm = new LinkManager(this, db);
				lm.downloadMissingIcons();
				lm.close();
			}
		} catch (Exception e) {
			message(e);
		}
	}

	public void message(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		String message = e.getMessage();
		if (Utils.getSettingBool(this, "debug")) {
			message += sw.toString();
		}
		message(message, "error");
	}

	public void message(String message, String action) {

		if (action.equals("error")) {
			this.message = "";
		}
		// TODO fix
		this.message += message;

		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra("message", this.message);
		intentSend.putExtra("action", action);
		this.sendBroadcast(intentSend);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		destroyed = true;
		Log.d("TumCampus DownloadService", "TumCampus service destroy");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("TumCampus DownloadService", "TumCampus service create");

		try {
			// check if sd card available
			Utils.getCacheDir("");

			// init sync table
			SyncManager sm = new SyncManager(this, db);
			sm.close();
		} catch (Exception e) {
			message(e);
		}
	}
}