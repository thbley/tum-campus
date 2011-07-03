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

	private void waitGui(int seconds) {
		synchronized (this) {
			try {
				wait(1000 * seconds);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
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
	
	private void _testTransports() {
		// departures
		assertTrue(solo.searchText("Marienplatz"));
		solo.clickOnText("Marienplatz");
		waitGui(3);
		assertTrue(solo.searchText("Abfahrt: Marienplatz"));
		assertTrue(solo.searchText("U3 Moosach"));

		assertTrue(solo.searchText("Garching-Forschungszentrum"));
		solo.clickOnText("Garching-Forschungszentrum");
		waitGui(3);
		assertTrue(solo.searchText("Abfahrt: Garching-Forschungszentrum"));
		assertTrue(solo.searchText("U6 Klinikum"));

		// search station
		solo.enterText(0, "kie");
		waitGui(3);
		
		assertTrue(solo.searchText("Kieferngarten"));
		solo.clickOnText("Kieferngarten");
		assertTrue(solo.searchText("Abfahrt: Kieferngarten"));
		assertTrue(solo.searchText("U6 Klinikum"));

		solo.clickOnText("Marienplatz");
		waitGui(3);

		// delete item
		solo.clickLongOnText("Kieferngarten");
		assertTrue(solo.searchButton("Ja"));
		solo.clickOnText("Ja");
		assertFalse(solo.searchText("Kieferngarten"));
	}
}