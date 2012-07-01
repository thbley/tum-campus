﻿package de.tum.in.tumcampus.test;

import java.util.Date;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;
import de.tum.in.tumcampus.common.Utils;
import de.tum.in.tumcampus.models.Feed;
import de.tum.in.tumcampus.models.FeedItem;
import de.tum.in.tumcampus.models.FeedItemManager;
import de.tum.in.tumcampus.models.FeedManager;

public class FeedsTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	private int feedId;

	public FeedsTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());

		FeedManager fm = new FeedManager(getActivity());
		feedId = fm.insertUpdateIntoDb(new Feed("Test feed", "http://www.test.de"));

		// inject test data
		FeedItem fi = new FeedItem(feedId, "Test message", "http://www.test.de", "Test description",
				Utils.getDateTime("2011-05-04T14:00:00"), "");

		FeedItemManager fim = new FeedItemManager(getActivity());
		fim.insertIntoDb(fi);
	}

	@Override
	public void tearDown() throws Exception {
		// remove test data
		FeedItemManager fim = new FeedItemManager(getActivity());
		fim.removeCache();

		FeedManager fm = new FeedManager(getActivity());
		fm.deleteFromDb(feedId);
		super.tearDown();
	}

	public void testFeedsList() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed auswählen"));

		assertTrue(solo.searchText("Test feed"));
		solo.clickOnText("Test feed");

		assertTrue(solo.searchText("Nachrichten: Test feed"));
		assertTrue(solo.searchText("Test message"));
		assertTrue(solo.searchText("Test description"));

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

	public void testFeedsCreateDelete() {
		assertTrue(solo.searchText("RSS-Feeds"));
		solo.clickOnText("RSS-Feeds");

		assertTrue(solo.searchText("Feed auswählen"));

		// scrollDown not working here
		solo.drag(200, 200, 600, 200, 40);

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