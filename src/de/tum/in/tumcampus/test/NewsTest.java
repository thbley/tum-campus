package de.tum.in.tumcampus.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.Const;
import de.tum.in.tumcampus.TumCampus;
import de.tum.in.tumcampus.models.News;
import de.tum.in.tumcampus.models.NewsManager;
import de.tum.in.tumcampus.models.Utils;

public class NewsTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public NewsTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());

		// inject test data
		News n = new News("N1", "Test message", "http://www.test.de", "",
				Utils.getDate("2011-12-13"));

		NewsManager nm = new NewsManager(getActivity(), Const.db);
		nm.replaceIntoDb(n);
		nm.close();
	}

	@Override
	public void tearDown() throws Exception {
		// remove test data
		NewsManager nm = new NewsManager(getActivity(), Const.db);
		nm.removeCache();
		nm.close();
		super.tearDown();
	}

	public void testNews() {
		assertTrue(solo.searchText("Nachrichten"));

		solo.clickOnText("Nachrichten");
		assertTrue(solo.searchText("Test message"));
		assertTrue(solo.searchText("13.12.2011"));

		solo.clickOnText("Test message");
	}

	public void testNewsContextMenu() {
		assertTrue(solo.searchText("Nachrichten"));
		solo.clickOnText("Nachrichten");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Aktualisieren");
		solo.sleep(10000);

		assertTrue(solo.searchText("Statistiken"));
		assertTrue(solo.searchText("04.10.2011"));

		assertTrue(solo.searchText("Öffnungszeiten"));
		solo.clickOnText("Öffnungszeiten");
	}
}