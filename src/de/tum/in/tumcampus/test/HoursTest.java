package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class HoursTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public HoursTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
		solo.scrollDown();
	}

	public void testHoursList() {
		assertTrue(solo.searchText("Öffnungszeiten"));
		solo.clickOnText("Öffnungszeiten");

		assertTrue(solo.searchText("Bibliotheken"));
		assertTrue(solo.searchText("Mensa Garching"));
		assertTrue(solo.searchText("Information"));

		solo.clickOnText("Bibliotheken");
		assertTrue(solo.searchText("Öffnungszeiten: Bibliotheken"));
		assertTrue(solo.searchText("Boltzmannstr. 3, Garching"));

		solo.clickOnText("Kategorie auswählen");
		assertTrue(solo.searchText("Mensa Garching"));

		solo.clickOnText("Mensa Garching");
		assertTrue(solo.searchText("Öffnungszeiten: Mensa Garching"));
	}
}