package de.tum.in.tumcampus.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

public class Utils {

	// TODO optimize

	/*
	 * } catch (ClientProtocolException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } catch (JSONException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch (Exception e) {
	 * e.printStackTrace(); }
	 */
	public static JSONObject downloadJson(String url) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		String data = "";

		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (entity != null) {

			// JSON Response Read
			InputStream instream = entity.getContent();
			data = convertStreamToString(instream);

			Log.d("TumCampus Download", "TumCampus Download " + data);
			instream.close();
		}
		return new JSONObject(data);
	}

	public static String downloadFile(String url, String target)
			throws Exception {

		Log.d("TumCampus Download", "TumCampus Download " + url);

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);

		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (entity == null) {
			// TODO implement
			throw new Exception("error");
		}

		File file = new File(target);

		// JSON Response Read
		InputStream in = entity.getContent();

		FileOutputStream out = new FileOutputStream(file);
		byte[] buffer = new byte[8192];
		int count = -1;
		while ((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
		out.flush();
		out.close();
		in.close();

		return file.toString();
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public static String getCacheDir(String dir) throws Exception {
		File f = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/tumcampus/"+dir);
		if (!f.exists()) {
			f.mkdirs();
		}
		if (!f.canWrite()) {
			throw new Exception("Cannot write to "+f.getPath());
		}
		return f.getPath()+"/";
	}

	public static String md5(String s) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(s.getBytes());
			BigInteger bigInt = new BigInteger(1, m.digest());
			return bigInt.toString(16);
		} catch (Exception e) {
			// TODO implement
		}
		return "";
	}

	public static Date getDate(String s) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return dateFormat.parse(s);
		} catch (Exception e) {
			// TODO implement
		}
		return new Date();
	}

	public static String getDateString(Date d) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(d);
	}
}
