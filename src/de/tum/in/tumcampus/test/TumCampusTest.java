package de.tum.in.tumcampus.test;

import com.jayway.android.robotium.solo.Solo;
import de.tum.in.tumcampus.TumCampus; //Klasse in der der Test stattfindet

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class TumCampusTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app, part of robotium

	public TumCampusTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	private void waitGui(int seconds) {
		synchronized (this) {
			try {
				wait(1000 * seconds);
			} catch (Exception e) {
				Log.e("TumCampus Test", e.getMessage());
			}
		}
	}

	public void testCafeterias() throws Exception {

		final String item = "Speisepläne";
		final String activityText = "Hallo Speisepläne";

		assertTrue("[0] Cafeterias not found", solo.searchText(item));

		solo.clickOnText(item);
		assertTrue("[1] Cafeterias Activity not correctly displayed",
				solo.searchText(activityText));

		solo.goBack();
		assertTrue("[2] Cafeterias not found after go back",
				solo.searchText(item));
	}

	public void testLectures() {

		final String item = "Vorlesungen";
		final String activityText = "Hallo Vorlesungen";

		assertTrue("[10] Lectures not found", solo.searchText(item));

		solo.clickOnText(item);
		assertTrue("[11] Lectures Activity not correctly displayed",
				solo.searchText(activityText));

		solo.goBack();
		assertTrue("[12] Lectures not found after go back",
				solo.searchText(item));
	}

	public void testOptions() {

		solo.clickOnText("Options");
		assertTrue("[20] SlidingDrawer not correctly displayed",
				solo.searchText("Button123"));

		solo.clickOnText("Options");
	}

	public void testZFailure() {
		assertTrue("[31] Some test failure", solo.searchText("ButtonBla"));
	}
}