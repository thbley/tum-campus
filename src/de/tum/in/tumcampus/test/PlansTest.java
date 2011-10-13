package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class PlansTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public PlansTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
		solo.scrollDown();
	}

	public void testPlansList() {
		assertTrue(solo.searchText("Umgebungspläne"));
		solo.clickOnText("Umgebungspläne");

		assertTrue(solo.searchText("Campus Garching"));
		assertTrue(solo.searchText("Campus Stammgelände"));
		assertTrue(solo.searchText("MVV-Schnellbahnnetz"));

		solo.clickOnText("Campus Garching");
		assertTrue(solo.searchText("Plan: Campus Garching"));

		solo.clickOnText("Plan auswählen");
		assertTrue(solo.searchText("MVV-Schnellbahnnetz"));

		solo.clickOnText("MVV-Schnellbahnnetz");
		assertTrue(solo.searchText("Plan: MVV-Schnellbahnnetz"));
	}
}