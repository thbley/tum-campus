package de.tum.in.tumcampus.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.tum.in.tumcampus.TumCampus;

public class CafeteriasTest extends ActivityInstrumentationTestCase2<TumCampus> {

	private Solo solo; // simulates the user of the app

	public CafeteriasTest() {
		super("de.tum.in.tumcampus", TumCampus.class);
	}

	public void setUp() {
		solo = new Solo(getInstrumentation(), getActivity());

		assertTrue(solo.searchText("Speisepläne"));
		solo.clickOnText("Speisepläne");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Aktualisieren");
		solo.sleep(10000);
		solo.goBack();
	}

	public void testCafeteriasPortrait() {
		assertTrue(solo.searchText("Speisepläne"));
		solo.clickOnText("Speisepläne");

		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		_testCafeterias();

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testCafeteriasLandscape() {
		assertTrue(solo.searchText("Speisepläne"));
		solo.clickOnText("Speisepläne");

		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		_testCafeterias();

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));
	}

	public void testCafeteriasSettings() {
		assertTrue(solo.searchText("Speisepläne"));
		solo.clickOnText("Speisepläne");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Einstellungen");
		solo.clickOnText("Mensa-Filter");
		solo.clearEditText(0);
		solo.enterText(0, "Garching");
		solo.goBack();
		solo.clickOnText("OK");
		solo.goBack();
		assertFalse(solo.searchText("München"));

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Einstellungen");
		solo.clickOnText("Mensa-Filter");
		solo.clearEditText(0);
		solo.goBack();
		solo.clickOnText("OK");
		solo.goBack();
		assertTrue(solo.searchText("München"));
	}

	public void testCafeteriasContextMenu() {
		assertTrue(solo.searchText("Speisepläne"));
		solo.clickOnText("Speisepläne");

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Preise");
	}
	
	private void _testCafeterias() {
		assertTrue(solo.searchText("Mensa Garching"));
		solo.clickOnText("Mensa Garching");

		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY) {
			calendar.add(Calendar.DATE, 2);
		}
		if (dayOfWeek == Calendar.SUNDAY) {
			calendar.add(Calendar.DATE, 1);
		}

		SimpleDateFormat de = new SimpleDateFormat("dd.MM.yyyy");
		String today = de.format(calendar.getTime());

		assertTrue(solo.searchText("Mensa Garching: " + today));
		assertTrue(solo.searchText("Beilagen"));
		assertTrue(solo.searchText("Tagesgericht"));

		assertTrue(solo.searchText("Datum auswählen"));
		solo.clickOnText("Datum auswählen");

		calendar.add(Calendar.DATE, 1);
		String tomorrow = de.format(calendar.getTime());

		assertTrue(solo.searchText(tomorrow));
		solo.clickOnText(tomorrow);

		assertTrue(solo.searchText("Mensa Garching: " + tomorrow));
	}
}