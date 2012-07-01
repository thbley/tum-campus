package de.tum.in.tumcampus.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;
import de.tum.in.tumcampus.common.Utils;

public class ImportTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public ImportTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());

		Utils.getCacheDir("links");
		Utils.getCacheDir("rss");
		Utils.getCacheDir("lectures");

		String path = Environment.getExternalStorageDirectory().getPath() + "/tumcampus/";

		BufferedWriter out = new BufferedWriter(new FileWriter(path + "links/test1.url"));
		out.write("[InternetShortcut]\nURL=http://www.in.tum.de/");
		out.close();

		out = new BufferedWriter(new FileWriter(path + "rss/test2.url"));
		out.write("[InternetShortcut]\nURL=http://www.spiegel.de/schlagzeilen/index.rss");
		out.close();

		out = new BufferedWriter(new FileWriter(path + "lectures/test3.csv"));
		out.write("WOCHENTAG;DATUM;VON;BIS;LV_NUMMER;TITEL;ORT;TERMIN_TYP;ANMERKUNG;URL\n");
		out.write("Do;14.07.2011;12:00;14:00;12345;Vorlesung1 (IN0007);00.01.1234;;;");
		out.close();
	}

	@Override
	public void tearDown() throws Exception {
		String path = Environment.getExternalStorageDirectory().getPath() + "/tumcampus/";

		new File(path + "links/test1.url").delete();
		new File(path + "rss/test2.url").delete();
		new File(path + "lectures/test3.csv").delete();

		super.tearDown();
	}

	public void testImportLectures() {
		assertTrue(solo.searchButton("Daten importieren"));
		solo.clickOnButton("Daten importieren");

		solo.clickOnButton("Vorlesungen importieren");
		solo.sleep(1000);

		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		solo.clickOnText("Feiertag");

		assertTrue(solo.searchText("Vorlesung1"));
		solo.clickOnText("Vorlesung1");

		assertTrue(solo.searchText("Do, 14.07.2011 12:00 - 14:00, 00.01.1234"));
		assertTrue(solo.searchText("IN0007"));
	}

	public void testImportLinks() {
		assertTrue(solo.searchButton("Daten importieren"));
		solo.clickOnButton("Daten importieren");

		solo.clickOnButton("Links importieren");
		solo.sleep(1000);
		solo.scrollDown();

		assertTrue(solo.searchText("Links"));
		solo.clickOnText("Links");

		assertTrue(solo.searchText("test1"));
		solo.clickOnText("test1");
		solo.sleep(2000);
	}

	public void testImportFeeds() {
		assertTrue(solo.searchButton("Daten importieren"));
		solo.clickOnButton("Daten importieren");

		solo.clickOnButton("RSS-Feeds importieren");
		solo.sleep(1000);

		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("test2"));
		solo.clickOnText("test2");
		solo.sleep(2000);
	}
}