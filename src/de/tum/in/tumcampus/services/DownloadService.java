package de.tum.in.tumcampus.services;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import de.tum.in.tumcampus.Const;
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

				// wait until images are loaded
				synchronized (this) {
					try {
						int count = 0;
						while (Utils.openDownloads > 0 && count < 10) {
							Utils.Log(String.valueOf(Utils.openDownloads));
							wait(1000);
							count++;
						}
					} catch (Exception e) {
						Utils.Log(e, "");
					}
				}

				// resume activity
				Intent intent2 = new Intent(context, context.getClass());
				intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(intent2);
			}
		}
	};

	@Override
	protected void onHandleIntent(Intent intent) {

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
		Utils.Log(action);

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
		message("RSS ", "");

		FeedManager nm = new FeedManager(this, Const.db);
		List<Integer> list = nm.getAllIdsFromDb();
		nm.close();

		FeedItemManager nim = new FeedItemManager(this, Const.db);
		for (int id : list) {
			if (destroyed) {
				break;
			}
			try {
				nim.downloadFromExternal(id, force);
			} catch (Exception e) {
				message(e, nim.lastInfo);
			}
		}
		nim.close();
	}

	public void downloadNews(boolean force) {
		if (!destroyed) {
			message("Nachrichten ", "");
			NewsManager nm = new NewsManager(this, Const.db);
			try {
				nm.downloadFromExternal(force);
			} catch (Exception e) {
				message(e, "");
			}
			nm.close();
		}
	}

	public void downloadEvents(boolean force) {
		if (!destroyed) {
			message("Veranstaltungen ", "");
			EventManager em = new EventManager(this, Const.db);
			try {
				em.downloadFromExternal(force);
			} catch (Exception e) {
				message(e, "");
			}
			em.close();
		}
	}

	public void downloadCafeterias(boolean force) {
		if (!destroyed) {
			message("Mensen ", "");

			CafeteriaManager cm = new CafeteriaManager(this, Const.db);
			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
			try {
				cm.downloadFromExternal(force);
				cmm.downloadFromExternal(cm.getAllIdsFromDb(), force);
			} catch (Exception e) {
				message(e, "");
			}
			cmm.close();
			cm.close();
		}
	}

	public void downloadLinks() {
		if (!destroyed) {
			LinkManager lm = new LinkManager(this, Const.db);
			try {
				lm.downloadMissingIcons();
			} catch (Exception e) {
				message(e, "");
			}
			lm.close();
		}
	}

	public void message(Exception e, String info) {
		Utils.Log(e, info);

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		String message = e.getMessage();
		if (Utils.getSettingBool(this, Const.settings.debug)) {
			message += sw.toString();
		}
		message("Fehler: " + message + " " + info + "\n", "error");
	}

	public void message(String message, String action) {

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
		Utils.Log("");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.Log("");

		try {
			// check if sd card available
			Utils.getCacheDir("");

			// init sync table
			SyncManager sm = new SyncManager(this, Const.db);
			sm.close();
		} catch (Exception e) {
			message(e, "");
			destroyed = true;
		}
	}
}