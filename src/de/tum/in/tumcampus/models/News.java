package de.tum.in.tumcampus.models;

import java.util.Date;

/**
 * News Object
 */
public class News {

	String id;
	String message;
	String link;
	String image;
	Date date;

	/**
	 * New News
	 * 
	 * <pre>
	 * @param id News Facebook-ID
	 * @param message News message, e.g. X released
	 * @param link Url, e.g. http://www.in.tum.de
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/news/cache/xy.jpg
	 * @param date News date
	 * </pre>
	 */
	public News(String id, String message, String link, String image, Date date) {

		this.id = id;
		this.message = message;
		this.link = link;
		this.image = image;
		this.date = date;
	}

	@Override
	public String toString() {
		return "id=" + id + " message=" + message + " link=" + link + " iamge="
				+ image + " date=" + Utils.getDateString(date);
	}
}