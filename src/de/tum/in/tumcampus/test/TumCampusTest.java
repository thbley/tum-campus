package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class TumCampusTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public TumCampusTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testMenu() {
		assertTrue(solo.searchText("Hello World"));
		assertTrue(solo.searchText("Aktualisieren"));
		assertTrue(solo.searchText("Vorlesungen"));
		assertTrue(solo.searchText("Speisepläne"));
		assertTrue(solo.searchText("MVV"));
		assertTrue(solo.searchText("Nachrichten"));
		assertTrue(solo.searchText("RSS-Feeds"));
		assertTrue(solo.searchText("Veranstaltungen"));
		assertTrue(solo.searchText("Links"));
		assertTrue(solo.searchText("App-Info"));
	}

	public void testRefresh() {
		assertTrue(solo.searchText("Aktualisieren"));
		solo.clickOnText("Aktualisieren");

		int duration = 0;
		while (!solo.searchText("Fertig!") && duration <= 60) {
			assertFalse(solo.searchText("Exception"));
			solo.sleep(1000);
			duration++;
		}
		assertTrue(solo.searchText("Aktualisiere: RSS Nachrichten "
				+ "Veranstaltungen Mensen Fertig!"));
	}

	public void testClearCache() {
		solo.sendKey(Solo.MENU);

		assertTrue(solo.searchText("Cache leeren"));
		solo.clickOnText("Cache leeren");
	}

	public void testAppinfo() {
		assertTrue(solo.searchText("App-Info"));

		solo.clickOnText("App-Info");
		assertTrue(solo.searchText("TUM Campus App for Android"));

		assertTrue(solo.searchText("GNU GPL v3"));
		assertTrue(solo.searchText("Source-Code"));

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}
}