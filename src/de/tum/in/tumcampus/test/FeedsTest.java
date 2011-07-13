package de.tum.in.tumcampus.test;

import java.util.Date;

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

	public void testFeedsList() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed auswählen"));

		assertTrue(solo.searchText("Spiegel"));
		solo.clickOnText("Spiegel");

		assertTrue(solo.searchText("Nachrichten: Spiegel"));

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testFeedsContextMenu() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed auswählen"));

		assertTrue(solo.searchText("Spiegel"));
		solo.clickOnText("Spiegel");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Aktualisieren");
		solo.sleep(10000);

		solo.clickInList(0, 0);
		solo.sleep(2000);
	}

	public void testAFeedsCreateDelete() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed auswählen"));

		solo.clickOnEditText(0);
		String name = "some name " + new Date();
		solo.enterText(0, "http://www.heise.de");
		solo.enterText(1, name);

		solo.clickOnText("Hinzufügen");

		assertTrue(solo.searchText(name));
		solo.clickLongOnText(name);

		assertTrue(solo.searchButton("Ja"));
		solo.clickOnText("Ja");

		assertFalse(solo.searchText(name));
	}
}