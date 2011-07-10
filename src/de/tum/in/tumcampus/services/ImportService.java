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
import android.util.Log;
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

		Log.d("TumCampus ImportService", "TumCampus service start");

		String action = intent.getStringExtra("action");

		if (action.equals("defaults")) {
			try {
				importTransportsDefaults();
				importFeedsDefaults();
				importLinksDefaults();
				importLectureItemsDefaults();
			} catch (Exception e) {
				Log.e("TumCampus ImportService",
						"TumCampus ImportService " + e.getMessage());
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

				// TODO implement
				String files = "";

				if (action.equals("feeds")) {
					importFeeds();
				}
				if (action.equals("links")) {
					importLinks();
				}
				if (action.equals("lectures")) {
					importLectureItems();
				}
				message("Daten importiert: " + files, "completed");
			} catch (Exception e) {
				message(e);
			}
			nm.cancel(1);
		}
	}

	public void importTransportsDefaults() throws Exception {
		TransportManager tm = new TransportManager(this, db);

		if (tm.empty()) {
			tm.replaceIntoDb("Garching-Forschungszentrum");
			tm.replaceIntoDb("Marienplatz");
		}
		tm.close();
	}

	public List<String> importFeeds() throws Exception {
		FeedManager nm = new FeedManager(this, db);
		List<String> list = nm.importFromInternal();
		nm.close();
		return list;
		// TODO fix
	}

	public void importFeedsDefaults() throws Exception {
		FeedManager nm = new FeedManager(this, db);
		if (nm.empty()) {
			nm.insertUpdateIntoDb(new Feed("Spiegel",
					"http://www.spiegel.de/schlagzeilen/index.rss"));

			nm.insertUpdateIntoDb(new Feed("N-tv", "http://www.n-tv.de/rss"));

			nm.insertUpdateIntoDb(new Feed("Zeit",
					"http://newsfeed.zeit.de/index"));

			nm.insertUpdateIntoDb(new Feed("Golem",
					"http://rss.golem.de/rss.php?feed=RSS1.0"));

			nm.insertUpdateIntoDb(new Feed("Heise",
					"http://www.heise.de/newsticker/heise.rdf"));

			nm.insertUpdateIntoDb(new Feed("MVG Störungsticker",
					"http://www.mvg-mobil.de/Tickerrss/CreateRssClass"));
		}
		nm.importFromInternal();
		nm.close();
	}

	public void importLectureItemsDefaults() throws Exception {

		// TODO add to resource directory
		LectureItemManager lim = new LectureItemManager(this, db);
		if (lim.empty()) {
			lim.replaceIntoDb(new LectureItem.Holiday("H1", Utils
					.getDate("2011-08-15"), "Mariä Himmelfahrt"));

			lim.replaceIntoDb(new LectureItem.Holiday("H2", Utils
					.getDate("2011-10-03"), "Tag der Deutschen Einheit"));

			lim.replaceIntoDb(new LectureItem.Holiday("H3", Utils
					.getDate("2011-11-01"), "Allerheiligen"));

			lim.replaceIntoDb(new LectureItem.Holiday("H4", Utils
					.getDate("2011-12-08"), "Dies Academicus"));

			lim.replaceIntoDb(new LectureItem.Vacation("V1", Utils
					.getDate("2011-08-01"), Utils.getDate("2011-09-30"),
					"Sommerferien"));

			lim.replaceIntoDb(new LectureItem.Vacation("V2", Utils
					.getDate("2011-12-24"), Utils.getDate("2012-01-06"),
					"Weihnachtsferien"));
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
			lm.insertUpdateIntoDb(new Link("Spiegel", "http://m.spiegel.de/"));

			lm.insertUpdateIntoDb(new Link("N-tv", "http://mobil.n-tv.de/"));

			lm.insertUpdateIntoDb(new Link("Zeit", "http://mobil.zeit.de/"));

			lm.insertUpdateIntoDb(new Link("Golem", "http://golem.mobi/"));

			lm.insertUpdateIntoDb(new Link("Heise", "http://heise-online.mobi/"));

			lm.insertUpdateIntoDb(new Link("MVV EFA",
					"http://efa.mvv-muenchen.de/mvv/XSLT_TRIP_REQUEST2?language=de"));

			lm.insertUpdateIntoDb(new Link("MVG Newsticker",
					"http://www.mvg-mobil.de/betriebsaenderungen/index.html"));

			lm.insertUpdateIntoDb(new Link("MVV- und Bahn-Auskunft",
					"http://mobile.bahn.de/bin/mobil/query2.exe/dox"));

			lm.insertUpdateIntoDb(new Link(
					"Informatik Infopoint",
					"http://www.in.tum.de/fuer-studierende-der-tum/service-fuer-studierende/infopoint.html"));

			lm.insertUpdateIntoDb(new Link("Informatik Studienberatung",
					"http://www.in.tum.de/fuer-studierende-der-tum/beratung.html"));

			lm.insertUpdateIntoDb(new Link("Stellenangebote",
					"http://portal.mytum.de/jobs/index_html"));

			lm.insertUpdateIntoDb(new Link("Fachschaft MPI",
					"http://mpi.fs.tum.de/"));

			lm.insertUpdateIntoDb(new Link("OPAC TU München",
					"http://opac.ub.tum.de/InfoGuideClient.tumsis/start.do?Login=wotum01"));

			lm.insertUpdateIntoDb(new Link(
					"OPAC LMU",
					"https://opacplus.ub.uni-muenchen.de/InfoGuideClient.ubmsis/start.do?Login=igubm"));

			lm.insertUpdateIntoDb(new Link("Informatik Fakultät",
					"http://www.in.tum.de/"));
		}
		lm.close();
	}

	public void importLinks() throws Exception {
		LinkManager lm = new LinkManager(this, db);
		lm.importFromInternal();
		lm.close();
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
		Intent intentSend = new Intent();
		intentSend.setAction(broadcast);
		intentSend.putExtra("message", message);
		intentSend.putExtra("action", action);
		this.sendBroadcast(intentSend);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("TumCampus ImportService", "TumCampus service destroy");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("TumCampus ImportService", "TumCampus service create");
	}
}