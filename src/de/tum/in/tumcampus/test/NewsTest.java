package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class NewsTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public NewsTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testNews() {
		assertTrue(solo.searchText("Nachrichten"));

		solo.clickOnText("Nachrichten");
		assertTrue(solo.searchText("Testing"));

		assertTrue(solo.searchText("Öffnungszeit"));
		solo.clickOnText("Öffnungszeit");
	}
	
	public void testNewsContextMenu() {
		assertTrue(solo.searchText("Nachrichten"));
		solo.clickOnText("Nachrichten");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Aktualisieren");
		solo.sleep(10000);
	}
}