package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class DebugTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public DebugTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testDebug() {
		assertFalse(solo.searchText("Debug"));
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Einstellungen");
		solo.clickOnText("Debug-Modus");
		solo.goBack();

		assertTrue(solo.searchText("Debug"));
		solo.clickOnText("Debug");

		assertTrue(solo.searchText("Debug SQLite"));

		solo.clickOnText("syncs");
		solo.clickOnText("cafeterias");
		solo.clickOnText("cafeterias_menus");
		solo.clickOnText("feeds");
		solo.clickOnText("feeds_items");
		solo.clickOnText("lectures");
		solo.clickOnText("lectures_items");
		solo.clickOnText("links");
		solo.clickOnText("events");
		solo.clickOnText("news");
		solo.clickOnText("time");

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Einstellungen");
		solo.clickOnText("Debug-Modus");
		solo.goBack();
		
		assertFalse(solo.searchText("Debug"));
	}
}