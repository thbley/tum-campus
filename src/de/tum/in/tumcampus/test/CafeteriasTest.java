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
	}

	public void testCafeterias() {
		assertTrue(solo.searchText("Speisepläne"));
		solo.clickOnText("Speisepläne");
		
		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		

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
		assertTrue(solo.searchText("Tagesgericht 1"));

		assertTrue(solo.searchText("Datum auswählen"));
		solo.clickOnText("Datum auswählen");

		calendar.add(Calendar.DATE, 1);
		String tomorrow = de.format(calendar.getTime());

		assertTrue(solo.searchText(tomorrow));
		solo.clickOnText(tomorrow);

		assertTrue(solo.searchText("Mensa Garching: " + tomorrow));

		
		solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		assertTrue(solo.searchText("Mensa Garching"));
		solo.clickOnText("Mensa Garching");

		assertTrue(solo.searchText("Mensa Garching: " + today));
		assertTrue(solo.searchText("Tagesgericht 1"));

		assertTrue(solo.searchText("Datum auswählen"));
		solo.clickOnText("Datum auswählen");

		calendar.add(Calendar.DATE, 1);
		tomorrow = de.format(calendar.getTime());

		assertTrue(solo.searchText(tomorrow));
		solo.clickOnText(tomorrow);

		assertTrue(solo.searchText("Mensa Garching: " + tomorrow));
		
		solo.goBack();
		assertTrue(solo.searchText("Hello World"));

		// TODO inject test data
	}
}