package de.tum.in.tumcampus.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

		assertTrue(solo.searchText("Mensa Garching"));
		solo.clickOnText("Mensa Garching");

		SimpleDateFormat de = new SimpleDateFormat("dd.MM.yyyy");
		String today = de.format(new Date());

		assertTrue(solo.searchText("Mensa Garching: " + today));
		assertTrue(solo.searchText("Tagesgericht 1"));

		assertTrue(solo.searchText("Datum auswählen"));
		solo.clickOnText("Datum auswählen");

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		String tomorrow = de.format(c.getTime());

		assertTrue(solo.searchText(tomorrow));
		solo.clickOnText(tomorrow);

		assertTrue(solo.searchText("Mensa Garching: " + tomorrow));

		solo.goBack();
		assertTrue(solo.searchText("Hello World"));

		// TODO horizontal layout, inject test data
	}
}