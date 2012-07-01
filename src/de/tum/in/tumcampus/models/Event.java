package de.tum.in.tumcampus.models;

import java.util.Date;

import de.tum.in.tumcampus.common.Utils;

/**
 * Event object
 */
public class Event {

	/**
	 * Event Facebook-ID
	 */
	String id;

	/**
	 * Name, e.g. PartyX
	 */
	String name;

	/**
	 * Event start DateTime
	 */
	Date start;

	/**
	 * Event end DateTime
	 */
	Date end;

	/**
	 * Location, e.g. Munich
	 */
	String location;

	/**
	 * Description, multiline
	 */
	String description;

	/**
	 * Event link, e.g. http://www.xyz.de
	 */
	String link;

	/**
	 * Local image, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 */
	String image;

	/**
	 * New Event
	 * 
	 * <pre>
	 * @param id Event Facebook-ID
	 * @param name Name, e.g. PartyX
	 * @param start Event start DateTime
	 * @param end Event end DateTime
	 * @param location Location, e.g. Munich
	 * @param description Description, multiline
	 * @param link Event link, e.g. http://www.xyz.de
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 * </pre>
	 */
	public Event(String id, String name, Date start, Date end, String location, String description, String link,
			String image) {

		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.location = location;
		this.description = description;
		this.link = link;
		this.image = image;
	}

	@Override
	public String toString() {
		return "id=" + id + " name=" + name + " start=" + Utils.getDateString(start) + " end="
				+ Utils.getDateString(end) + " location=" + location + " description=" + description + " link=" + link
				+ " image=" + image;
	}
}