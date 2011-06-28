package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class TumCampusTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app, part of robotium

	public TumCampusTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	private void waitGui(int seconds) {
		synchronized (this) {
			try {
				wait(1000 * seconds);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
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
		assertTrue(solo.searchText("Debug"));
	}
	
	public void testRefresh() {
		assertTrue(solo.searchText("Aktualisieren"));
		solo.clickOnText("Aktualisieren");
		
		int duration = 0;
		while (!solo.searchText("Fertig.") && duration<=30) {
			waitGui(1);
			duration++;
		}
		assertTrue(solo.searchText("Aktualisiere: Mensen, Menus, Veranstaltungen, RSS, Fertig."));
	}
	
	public void testClearCache() {
		solo.sendKey(Solo.MENU);
		
		assertTrue(solo.searchText("Cache leeren"));
		solo.clickOnText("Cache leeren");
	}

	public void testLectures() {
		assertTrue(solo.searchText("Vorlesungen"));

		solo.clickOnText("Vorlesungen");
		assertTrue(solo.searchText("Hallo Vorlesungen"));
		
		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testTransport() {
		assertTrue(solo.searchText("MVV"));

		solo.clickOnText("MVV");
		assertTrue(solo.searchText("Hallo MVV"));
		
		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testNews() {
		assertTrue(solo.searchText("Nachrichten"));

		solo.clickOnText("Nachrichten");
		assertTrue(solo.searchText("Hallo Nachrichten"));
		
		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testLinks() {
		assertTrue(solo.searchText("Links"));

		solo.clickOnText("Links");
		assertTrue(solo.searchText("Hallo Links"));
		
		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
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

	public void testOptions() {
		solo.clickOnText("Options");
	}
}