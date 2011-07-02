package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class DebugTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public DebugTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testDebug() {
		assertTrue(solo.searchText("Debug"));
		solo.clickOnText("Debug");

		assertTrue(solo.searchText("sqlite"));

		solo.clickOnText("cafeterias");
		solo.clickOnText("cafeterias_menus");
		solo.clickOnText("feeds");
		solo.clickOnText("feeds_items");
		solo.clickOnText("links");
		solo.clickOnText("events");

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}
}