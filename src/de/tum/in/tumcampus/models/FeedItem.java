package de.tum.in.tumcampus.models;

import java.util.Date;

public class FeedItem {

	int feedId;
	String title;
	String link;
	String description;
	Date date;
	String image;

	public FeedItem(int feedId, String title, String link, String description,
			Date date, String image) {

		this.feedId = feedId;
		this.title = title;
		this.link = link;
		this.description = description;
		this.date = date;
		this.image = image;
	}

	public String toString() {
		return "feedId=" + feedId + " title=" + title + " link=" + link
				+ " description=" + description + " date="
				+ Utils.getDateString(date) + " image=" + image;
	}
}