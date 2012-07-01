package de.tum.in.tumcampus.test;

import java.net.URLEncoder;

import android.test.AndroidTestCase;
import de.tum.in.tumcampus.common.Utils;

public class PersonsInterfaceTest extends AndroidTestCase {

	final String token = "6DF858979FE40038840B4ED7C9800C55";

	public final void testIsTokenConfirmed() throws Exception {

		String url = "https://campus.tum.de/tumonline/wbservicesbasic.isTokenConfirmed?pToken=";
		String data = Utils.downloadString(url + token);

		assertTrue(data.length() > 0);
		assertTrue(data.indexOf("<confirmed>true</confirmed>") != -1);
	}

	public final void testPersonenSuche() throws Exception {
		String url = "https://campus.tum.de/tumonline/wbservicesbasic.personenSuche?pToken=";
		String search = "baumgarten";

		String data = Utils.downloadString(url + token + "&pSuche=" + URLEncoder.encode(search));

		assertTrue(data.length() > 0);
		assertTrue(data.indexOf("<familienname>Baumgarten</familienname>") != -1);
		assertTrue(data.indexOf("<titel>Prof. Dr.</titel>") != -1);
		assertTrue(data.indexOf("<obfuscated_id>") != -1);
		// TODO extend validation
	}

	public final void testPersonenSuche2() throws Exception {
		String url = "https://campus.tum.de/tumonline/wbservicesbasic.personenSuche?pToken=";
		String search = "uwe baumgarten";

		String data = Utils.downloadString(url + token + "&pSuche=" + URLEncoder.encode(search));

		assertTrue(data.length() > 0);
		assertTrue(data.indexOf("<familienname>Baumgarten</familienname>") != -1);
		assertTrue(data.indexOf("<titel>Prof. Dr.</titel>") != -1);
		assertTrue(data.indexOf("<obfuscated_id>") != -1);
		// TODO extend validation
	}

	public final void testPersonenDetails() throws Exception {
		String url = "https://campus.tum.de/tumonline/wbservicesbasic.personenDetails?pToken=";
		String search = "61702";

		String data = Utils.downloadString(url + token + "&pIdentNr=" + URLEncoder.encode(search));

		assertTrue(data.length() > 0);
		assertTrue(data.indexOf("<familienname>Baumgarten</familienname>") != -1);
		assertTrue(data.indexOf("<email>baumgaru@tum.de</email>") != -1);
		assertTrue(data.indexOf("<telefonnummer>+49 (89) 289 - 18564</telefonnummer>") != -1);
		// TODO extend validation
	}

}