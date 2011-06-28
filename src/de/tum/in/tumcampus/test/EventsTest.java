package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class EventsTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public EventsTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testEvents() {
		assertTrue(solo.searchText("Veranstaltungen"));

		solo.clickOnText("Veranstaltungen");

		assertTrue(solo.searchText("R�ckmeldung f�r Wintersemester"));
		assertTrue(solo.searchText("Mo, 15.08.2011 00:00 - 03:00"));
		assertTrue(solo.searchText("TU M�nchen"));
		solo.clickOnText("R�ckmeldung f�r Wintersemester");

		assertTrue(solo.searchText("Details"));
		assertTrue(solo.searchText("R�ckmeldefrist"));

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));

		// TODO inject test data
	}
}