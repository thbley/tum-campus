package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class FeedsTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public FeedsTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testFeeds() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed ausw�hlen"));

		assertTrue(solo.searchText("Spiegel"));
		solo.clickOnText("Spiegel");

		assertTrue(solo.searchText("Nachrichten: Spiegel"));

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));

		// TODO add detailed test, test data
		
		// TODO add feed
	}
	
	public void testFeedsContextMenu() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed ausw�hlen"));

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Aktualisieren");
		solo.sleep(10000);
	}
	
	public void testFeedsDelete() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed ausw�hlen"));

		assertTrue(solo.searchText("Heise"));
		solo.clickLongOnText("Heise");
		
		assertTrue(solo.searchButton("Ja"));
		solo.clickOnText("Ja");

		assertFalse(solo.searchText("Heise"));
	}
}