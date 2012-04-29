package de.tum.in.tumcampus.test;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.Const;
import de.tum.in.tumcampus.TumCampus;
import de.tum.in.tumcampus.models.LectureItem;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.LectureManager;
import de.tum.in.tumcampus.models.Utils;

public class LecturesTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public LecturesTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());

		// inject test data
		LectureItem li = new LectureItem("T1", "T1",
				Utils.getDateTime("2011-05-04T14:00:00"),
				Utils.getDateTime("2011-05-04T16:00:00"), "CSCW 2", "IN2119",
				"01.07.023", "", "", "T1");

		LectureItem li2 = new LectureItem.Holiday("TH1",
				Utils.getDate("2011-12-13"), "Some Holiday");

		LectureItemManager lim = new LectureItemManager(getActivity());
		lim.replaceIntoDb(li);
		lim.replaceIntoDb(li2);

		LectureManager lm = new LectureManager(getActivity(), Const.db);
		lm.updateLectures();
	}

	@Override
	public void tearDown() throws Exception {
		// remove test data
		LectureItemManager lim = new LectureItemManager(getActivity());
		lim.deleteLectureFromDb("T1");
		lim.deleteLectureFromDb("TH1");

		LectureManager lm = new LectureManager(getActivity(), Const.db);
		lm.deleteItemFromDb("T1");
		super.tearDown();
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

		assertTrue(solo.searchText("CSCW"));
		solo.clickOnText("CSCW");
	}

	public void testLecturesItemDelete() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		solo.clickOnText("Feiertag");

		assertTrue(solo.searchText("Some Holiday"));
		solo.clickLongOnText("Some Holiday");

		assertTrue(solo.searchButton("Ja"));
		solo.clickLongOnText("Ja");

		assertFalse(solo.searchText("Some Holiday"));
	}

	public void testLecturesDelete() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		assertTrue(solo.searchText("CSCW"));
		solo.clickLongOnText("CSCW");

		assertTrue(solo.searchButton("Ja"));
		solo.clickLongOnText("Ja");

		assertFalse(solo.searchText("CSCW"));
	}

	public void testLecturesContextMenu() {
		assertTrue(solo.searchText("Vorlesungen"));
		solo.clickOnText("Vorlesungen");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Roomfinder");
		solo.sleep(2000);
	}

	private void _testLectures() {
		assertTrue(solo.searchText("Nächste Vorlesungen"));

		assertTrue(solo.searchText("Feiertag"));
		solo.clickOnText("Feiertag");
		assertTrue(solo.searchText("Deutschen Einheit"));
		assertTrue(solo.searchText("Mo, 03.10.2011"));

		assertTrue(solo.searchText("Ferien"));
		solo.clickOnText("Ferien");
		assertTrue(solo.searchText("Sommerferien"));
		assertTrue(solo.searchText("01.08.2011 - 30.09.2011"));

		solo.clickOnText("CSCW");
		assertTrue(solo.searchText("Mi, 04.05.2011 14:00 - 16:00, 01.07.023"));
		assertTrue(solo.searchText("IN2119"));
		solo.clickOnText("IN2119");
	}
}