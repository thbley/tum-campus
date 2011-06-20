package de.tum.in.tumcampus.models;

import java.util.Date;

public class News {

	String feedUrl;
	String title;
	String link;
	String description;
	Date date;
	String image;

	public News(String feedUrl, String title, String link, String description,
			Date date, String image) {

		this.feedUrl = feedUrl;
		this.title = title;
		this.link = link;
		this.description = description;
		this.date = date;
		this.image = image;
	}

	public String toString() {
		return "feedUrl=" + feedUrl + " title=" + title + " link=" + link
				+ " description=" + description + " date="
				+ Utils.getDateString(date) + " image=" + image;
	}
}