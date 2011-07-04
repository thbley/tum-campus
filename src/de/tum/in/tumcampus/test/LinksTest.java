package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class LinksTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public LinksTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testLinks() {
		assertTrue(solo.searchText("Links"));
		solo.clickOnText("Links");

		assertTrue(solo.searchText("Golem"));
		assertTrue(solo.searchText("N-tv"));
		
		solo.clickOnText("Heise");

		// TODO inject test data
	}
}