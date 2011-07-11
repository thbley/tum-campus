package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class ImportTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public ImportTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testImport() {
		assertTrue(solo.searchText("Daten importieren"));
		solo.clickOnText("Daten importieren");
		
		solo.clickOnButton("Vorlesungen importieren");
		solo.sleep(5000);
		assertTrue(solo.searchText("Fertig!"));
		
		solo.clickOnButton("Links importieren");
		solo.sleep(3000);
		assertTrue(solo.searchText("Fertig!"));
		
		solo.clickOnButton("RSS-Feeds importieren");
		solo.sleep(5000);
		assertTrue(solo.searchText("Fertig!"));
		
		solo.clickOnText("Daten importieren");
	}
}