package de.tum.in.tumcampus.test;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.test.AndroidTestCase;
import de.tum.in.tumcampus.models.Utils;

public class InterfacesTest extends AndroidTestCase {

	/**
	 * Facebook date time format e.g. 2011-08-15T03:00:00
	 */
	private static final String dateTimeFormat = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

	/**
	 * ISO date format e.g. 2011-08-15
	 */
	private static final String dateFormat = "\\d{4}-\\d{2}-\\d{2}";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testFacebookNews() throws Exception {
		String url = "https://graph.facebook.com/162327853831856/feed/?access_token=";
		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

		JSONObject json = Utils.downloadJson(url + URLEncoder.encode(token));

		assertTrue(json.has("data"));
		JSONArray jsonArray = json.getJSONArray("data");

		assertTrue(jsonArray.length() > 0);
		json = jsonArray.getJSONObject(0);

		assertTrue(json.getString("id").length() > 0);
		assertTrue(json.getString("object_id").length() > 0);
		assertTrue(json.getString("type").length() > 0);
		assertTrue(json.getString("name").length() > 0);
	}

	public final void testFacebookEvents() throws Exception {
		String url = "https://graph.facebook.com/162327853831856/events?limit=25&access_token=";
		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

		JSONObject json = Utils.downloadJson(url + URLEncoder.encode(token));

		assertTrue(json.has("data"));
		JSONArray jsonArray = json.getJSONArray("data");

		assertTrue(jsonArray.length() > 0);
		json = jsonArray.getJSONObject(0);

		assertTrue(json.getString("id").length() > 0);
		assertTrue(json.getString("name").length() > 0);
		assertTrue(json.getString("start_time").length() > 0);
		assertTrue(json.getString("end_time").length() > 0);
		assertTrue(json.getString("location").length() > 0);

		assertTrue(json.getString("start_time").matches(dateTimeFormat));
		assertTrue(json.getString("end_time").matches(dateTimeFormat));
	}

	public final void testFacebookEventsDetails() throws Exception {
		String url = "https://graph.facebook.com/";
		String id = "166478443419659";

		JSONObject json = Utils.downloadJson(url + id);

		assertTrue(json.getString("id").length() > 0);
		assertTrue(json.getString("name").length() > 0);
		assertTrue(json.getString("start_time").length() > 0);
		assertTrue(json.getString("end_time").length() > 0);
		assertTrue(json.getString("location").length() > 0);
		assertTrue(json.getString("description").length() > 0);

		assertTrue(json.getString("start_time").matches(dateTimeFormat));
		assertTrue(json.getString("end_time").matches(dateTimeFormat));
	}

	public final void testYqlRss() throws Exception {
		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
		String feedUrl = "http://www.spiegel.de/schlagzeilen/index.rss";
		String query = URLEncoder
				.encode("SELECT title, link, description, pubDate, enclosure.url "
						+ "FROM rss WHERE url=\"" + feedUrl + "\" LIMIT 25");

		JSONObject json = Utils.downloadJson(baseUrl + query);

		assertTrue(json.has("query"));
		json = json.getJSONObject("query");

		assertTrue(json.has("results"));
		json = json.getJSONObject("results");

		assertTrue(json.has("item"));
		JSONArray jsonArray = json.getJSONArray("item");

		assertTrue(jsonArray.length() > 0);
		json = jsonArray.getJSONObject(0);

		assertTrue(json.getString("title").length() > 0);
		assertTrue(json.getString("link").length() > 0);
		assertTrue(json.getString("description").length() > 0);
		assertTrue(json.getString("pubDate").length() > 0);

		assertTrue(json.has("enclosure"));
		json = json.getJSONObject("enclosure");

		assertTrue(json.getString("url").length() > 0);
	}

	public final void testCafeteria() throws Exception {
		String url = "http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php";

		JSONObject json = Utils.downloadJson(url);

		assertTrue(json.has("mensa_mensen"));
		JSONArray jsonArray = json.getJSONArray("mensa_mensen");

		assertTrue(jsonArray.length() > 0);
		json = jsonArray.getJSONObject(0);

		assertTrue(json.getString("id").length() > 0);
		assertTrue(json.getString("name").length() > 0);
		assertTrue(json.getString("anschrift").length() > 0);
	}

	public final void testCafeteriaMenu() throws Exception {
		String id = "422";
		String url = "http://lu32kap.typo3.lrz.de/mensaapp/exportDB.php?mensa_id=";

		JSONObject json = Utils.downloadJson(url + id);

		assertTrue(json.has("mensa_menu"));
		JSONArray jsonArray = json.getJSONArray("mensa_menu");

		assertTrue(jsonArray.length() > 0);
		JSONObject json2 = jsonArray.getJSONObject(0);

		assertTrue(json2.getString("id").length() > 0);
		assertTrue(json2.getString("mensa_id").length() > 0);
		assertTrue(json2.getString("date").length() > 0);
		assertTrue(json2.getString("type_short").length() > 0);
		assertTrue(json2.getString("type_long").length() > 0);
		assertTrue(json2.getString("type_nr").length() > 0);
		assertTrue(json2.getString("name").length() > 0);

		assertTrue(json2.getString("date").matches(dateFormat));

		assertTrue(json.has("mensa_beilagen"));
		jsonArray = json.getJSONArray("mensa_beilagen");

		assertTrue(jsonArray.length() > 0);
		json2 = jsonArray.getJSONObject(0);

		assertTrue(json2.getString("mensa_id").length() > 0);
		assertTrue(json2.getString("date").length() > 0);
		assertTrue(json2.getString("name").length() > 0);
		assertTrue(json2.getString("type_short").length() > 0);
		assertTrue(json2.getString("type_long").length() > 0);

		assertTrue(json2.getString("date").matches(dateFormat));
	}

	public final void testYqlMvvFind() throws Exception {
		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle=Gar";

		String query = URLEncoder
				.encode("select content from html where url=\"" + lookupUrl
						+ "\" and xpath=\"//a[contains(@href,'haltestelle')]\"");

		JSONObject json = Utils.downloadJson(baseUrl + query);

		assertTrue(json.has("query"));
		json = json.getJSONObject("query");

		assertTrue(json.has("results"));
		json = json.getJSONObject("results");

		assertTrue(json.has("a"));
		JSONArray jsonArray = json.getJSONArray("a");

		assertTrue(jsonArray.length() > 0);

		assertEquals(jsonArray.getString(1), "Garching");
		assertEquals(jsonArray.getString(2), "Garching-Forschungszentrum");
	}

	public final void testYqlMvvGet() throws Exception {
		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle=Marienplatz";

		String query = URLEncoder
				.encode("select content from html where url=\"" + lookupUrl
						+ "\" and xpath=\"//td[contains(@class,'Column')]/p\"");

		JSONObject json = Utils.downloadJson(baseUrl + query);

		assertTrue(json.has("query"));
		json = json.getJSONObject("query");

		assertTrue(json.has("results"));
		json = json.getJSONObject("results");

		assertTrue(json.has("p"));
		JSONArray jsonArray = json.getJSONArray("p");

		assertTrue(jsonArray.length() > 0);

		assertEquals(jsonArray.getString(0), "Marienplatz");
		assertTrue(jsonArray.getString(1).length() > 0);

		assertTrue(jsonArray.getString(2).length() > 0); // Linie
		assertTrue(jsonArray.getString(3).length() > 0); // Ziel
		assertTrue(jsonArray.getString(4).length() > 0); // Abfahrt in x min.
	}
}