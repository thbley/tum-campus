package de.tum.in.tumcampus.models;

import java.util.Date;

public class News {

	String id;
	String message;
	String link;
	String image;
	Date date;

	public News(String id, String message, String link, String image, Date date) {

		this.id = id;
		this.message = message;
		this.link = link;
		this.image = image;
		this.date = date;
	}

	public String toString() {
		return "id=" + id + " message=" + message + " link=" + link + " iamge="
				+ image + " date=" + Utils.getDateString(date);
	}
}