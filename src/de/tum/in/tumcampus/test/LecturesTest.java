package de.tum.in.tumcampus.test;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class LecturesTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public LecturesTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testLecturesPortrait() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		_testLectures();
	}

	public void testLecturesLandscape() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		_testLectures();
	}

	public void testLecturesLink() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		assertTrue(solo.searchText("Nächste Vorlesungen"));

		assertTrue(solo.searchText("Logik"));
		solo.clickOnText("Logik");
	}

	public void testLecturesDelete() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		solo.clickOnText("Feiertag");

		assertTrue(solo.searchText("Allerheiligen"));
		solo.clickLongOnText("Allerheiligen");

		assertTrue(solo.searchButton("Ja"));
		solo.clickLongOnText("Ja");
		
		assertFalse(solo.searchText("Allerheiligen"));
		
		// TODO add lecture delete
	}

	private void _testLectures() {
		assertTrue(solo.searchText("Nächste Vorlesungen"));

		assertTrue(solo.searchText("Feiertag"));
		solo.clickOnText("Feiertag");
		assertTrue(solo.searchText("Academicus"));
		assertTrue(solo.searchText("Do, 08.12.2011"));

		assertTrue(solo.searchText("Ferien"));
		solo.clickOnText("Ferien");
		assertTrue(solo.searchText("Sommerferien"));
		assertTrue(solo.searchText("01.08.2011 - 30.09.2011"));

		solo.clickOnText("CSCW");
		assertTrue(solo.searchText("Mi, 04.05.2011 14:00 - 16:00, 01.07.023"));
		assertTrue(solo.searchText("IN2119"));
		solo.clickOnText("IN2119");

		// TODO inject test data
	}
}