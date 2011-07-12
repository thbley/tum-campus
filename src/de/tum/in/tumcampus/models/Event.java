package de.tum.in.tumcampus.models;

import java.util.Date;

public class Event {

	String id;
	String name;
	Date start;
	Date end;
	String location;
	String description;
	String link;
	String image;

	public Event(String id, String name, Date start, Date end, String location,
			String description, String link, String image) {

		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.location = location;
		this.description = description;
		this.link = link;
		this.image = image;
	}

	public String toString() {
		return "id=" + id + " name=" + name + " start="
				+ Utils.getDateString(start) + " end="
				+ Utils.getDateString(end) + " location=" + location
				+ " description=" + description + " link=" + link + " image="
				+ image;
	}
}