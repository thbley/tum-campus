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
import de.tum.in.tumcampus.models.GalleryManager;
import de.tum.in.tumcampus.models.LinkManager;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.models.SyncManager;
import de.tum.in.tumcampus.models.Utils;

/**
 * Service used to download files from external pages
 */
public class DownloadService extends IntentService {

	/**
	 * Indicator to avoid starting new downloads
	 */
	private volatile boolean destroyed = false;

	/**
	 * Download broadcast identifier
	 */
	public final static String broadcast = "de.tum.in.tumcampus.intent.action.BROADCAST_DOWNLOAD";

	/**
	 * default init (run intent in new thread)
	 */
	public DownloadService() {
		super("DownloadService");
	}

	/**
	 * Notificaiton message
	 */
	private String message = "";

	/**
	 * Default receiver: output feedback as toast and resume activity
	 */
	public static BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(DownloadService.broadcast)) {
				return;
			}
			if (intent.getStringExtra("action").length() != 0) {
				Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_LONG).show();

				// wait until images are loaded
				synchronized (this) {
					try {
						int count = 0;
						while (Utils.openDownloads > 0 && count < 10) {
							Utils.log(String.valueOf(Utils.openDownloads));
							wait(1000);
							count++;
						}
					} catch (Exception e) {
						Utils.log(e, "");
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

		// show download notification
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager) getSystemService(ns);

		Notification notification = new Notification(android.R.drawable.stat_sys_download, "Aktualisiere ...",
				System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TumCampus.class), 0);

		notification.setLatestEventInfo(this, "TUMCampus download ...", "", contentIntent);
		nm.notify(1, notification);

		message("Aktualisiere: ", "");

		String action = intent.getStringExtra("action");
		Utils.log(action);

		boolean force = false;
		if (action != null) {
			force = true;
		}
		// download all or only one action
		if ((action == null || action.equals("feeds")) && !destroyed && Utils.getSettingBool(this, "feeds")) {
			message("RSS ", "");
			downloadFeeds(force);
		}
		if ((action == null || action.equals("news")) && !destroyed && Utils.getSettingBool(this, "news")) {
			message("Nachrichten ", "");
			downloadNews(force);
		}
		if ((action == null || action.equals("events")) && !destroyed && Utils.getSettingBool(this, "events")) {
			message("Veranstaltungen ", "");
			downloadEvents(force);
		}
		if ((action == null || action.equals("gallery")) && !destroyed && Utils.getSettingBool(this, "gallery")) {
			message("Kurz notiert ", "");
			downloadGallery(force);
		}
		if ((action == null || action.equals("cafeterias")) && !destroyed && Utils.getSettingBool(this, "cafeterias")) {
			message("Mensen ", "");
			downloadCafeterias(force);
		}
		if ((action == null || action.equals("links")) && !destroyed && Utils.getSettingBool(this, "links")) {
			downloadLinks();
		}
		message("Fertig!", "completed");
		nm.cancel(1);
	}

	/**
	 * Download items for all feeds
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * </pre>
	 */
	public void downloadFeeds(boolean force) {
		FeedManager nm = new FeedManager(this, Const.db);
		List<Integer> list = nm.getAllIdsFromDb();

		FeedItemManager nim = new FeedItemManager(this, Const.db);
		for (int id : list) {
			if (destroyed) {
				break;
			}
			try {
				nim.downloadFromExternal(id, false, force);
			} catch (Exception e) {
				message(e, nim.lastInfo);
			}
		}
	}

	/**
	 * Download news elements
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * </pre>
	 */
	public void downloadNews(boolean force) {
		NewsManager nm = new NewsManager(this, Const.db);
		try {
			nm.downloadFromExternal(force);
		} catch (Exception e) {
			message(e, "");
		}
	}

	/**
	 * Download events
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * </pre>
	 */
	public void downloadEvents(boolean force) {
		EventManager em = new EventManager(this, Const.db);
		try {
			em.downloadFromExternal(force);
		} catch (Exception e) {
			message(e, "");
		}
	}

	/**
	 * Download gallery
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * </pre>
	 */
	public void downloadGallery(boolean force) {
		GalleryManager gm = new GalleryManager(this, Const.db);
		try {
			gm.downloadFromExternal(force);
		} catch (Exception e) {
			message(e, "");
		}
	}

	/**
	 * Download cafeterias
	 * 
	 * <pre>
	 * @param force True to force download over normal sync period, else false
	 * </pre>
	 */
	public void downloadCafeterias(boolean force) {
		CafeteriaManager cm = new CafeteriaManager(this, Const.db);
		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this, Const.db);
		try {
			cm.downloadFromExternal(force);
			cmm.downloadFromExternal(force);
		} catch (Exception e) {
			message(e, "");
		}
	}

	/**
	 * Download missing icons for links
	 */
	public void downloadLinks() {
		LinkManager lm = new LinkManager(this, Const.db);
		try {
			lm.downloadMissingIcons();
		} catch (Exception e) {
			message(e, "");
		}
	}

	/**
	 * Send notification message to service caller
	 * 
	 * <pre>
	 * @param e Exception, get message and stacktrace from 
	 * @param info Notification info, appended to exception message
	 * </pre>
	 */
	public void message(Exception e, String info) {
		Utils.log(e, info);

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		String message = e.getMessage();
		if (Utils.getSettingBool(this, Const.Settings.debug)) {
			message += sw.toString();
		}
		message("Fehler: " + message + " " + info + "\n", "error");
	}

	/**
	 * Send notification message to service caller
	 * 
	 * <pre>
	 * @param message Notification message
	 * @param action Notification action (e.g. error, completed)
	 * </pre>
	 */
	public void message(String message, String action) {
		this.message += message;

		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra("message", this.message);
		intentSend.putExtra("action", action);
		sendBroadcast(intentSend);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// don't start new downloads
		destroyed = true;
		Utils.log("");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.log("");

		try {
			// check if sd card available
			Utils.getCacheDir("");

			// init sync table
			new SyncManager(this, Const.db);
		} catch (Exception e) {
			message(e, "");

			// don't start new downloads
			destroyed = true;
		}
	}
}