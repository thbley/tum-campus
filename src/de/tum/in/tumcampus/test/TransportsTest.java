package de.tum.in.tumcampus.test;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class TransportsTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public TransportsTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testTransportsPortrait() {
		assertTrue(solo.searchText("MVV"));
		solo.clickOnText("MVV");

		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		_testTransports();

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testTransportsLandscape() {
		assertTrue(solo.searchText("MVV"));
		solo.clickOnText("MVV");

		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		_testTransports();

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testTransportsSearchDelete() {
		assertTrue(solo.searchText("MVV"));
		solo.clickOnText("MVV");

		// search station
		solo.enterText(0, "kie");
		solo.sleep(3000);

		assertTrue(solo.searchText("Kieferngarten"));
		solo.clickOnText("Kieferngarten");
		assertTrue(solo.searchText("Abfahrt: Kieferngarten"));
		assertTrue(solo.searchText("U6 Klinikum"));

		solo.clickOnText("Marienplatz");
		solo.sleep(3000);

		// delete item
		solo.clickLongOnText("Kieferngarten");

		assertTrue(solo.searchButton("Ja"));
		solo.clickOnText("Ja");

		assertFalse(solo.searchText("Kieferngarten"));
	}

	public void testTransportsContextMenu() {
		assertTrue(solo.searchText("MVV"));
		solo.clickOnText("MVV");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("MVV EFA");
	}

	private void _testTransports() {
		// departures
		assertTrue(solo.searchText("Marienplatz"));
		solo.clickOnText("Marienplatz");
		solo.sleep(3000);
		assertTrue(solo.searchText("Abfahrt: Marienplatz"));
		assertTrue(solo.searchText("U3 Moosach"));

		assertTrue(solo.searchText("Garching-Forschungszentrum"));
		solo.clickOnText("Garching-Forschungszentrum");
		solo.sleep(3000);

		assertTrue(solo.searchText("Abfahrt: Garching-Forschungszentrum"));
		assertTrue(solo.searchText("U6 Klinikum"));
	}
}