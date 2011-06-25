package de.tum.in.tumcampus.models;

import java.util.Date;

public class Event {

	String id;
	String name;
	Date start_time;
	Date end_time;
	String location;
	String description;
	String link;
	String image;

	public Event(String id, String name, Date start_time, Date end_time,
			String location, String description, String link, String image) {

		this.id = id;
		this.name = name;
		this.start_time = start_time;
		this.end_time = end_time;
		this.location = location;
		this.description = description;
		this.link = link;
		this.image = image;
	}

	public String toString() {
		return "id=" + id + " name=" + name + " start_time="
				+ Utils.getDateString(start_time) + " end_time="
				+ Utils.getDateString(end_time) + " location=" + location
				+ " description=" + description + " link=" + link + " image="
				+ image;
	}
}