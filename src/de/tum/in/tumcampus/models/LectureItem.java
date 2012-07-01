package de.tum.in.tumcampus.models;

import java.util.Date;

import de.tum.in.tumcampus.common.Utils;

/**
 * LectureItem object
 */
public class LectureItem {

	/**
	 * Lecture item ID (LectureId_Start-Unix-Timestamp)
	 */
	String id;

	/**
	 * Lecture ID
	 */
	String lectureId;

	/**
	 * Start DateTime
	 */
	Date start;

	/**
	 * End DateTime
	 */
	Date end;

	/**
	 * Lecture name
	 */
	String name;

	/**
	 * Lecture module
	 */
	String module;

	/**
	 * Lecture item location
	 */
	String location;

	/**
	 * Lecture item note, e.g. Übung
	 */
	String note;

	/**
	 * Lecture item URL
	 */
	String url;

	/**
	 * Lecture item series ID (LectureID_Week-Day_Start-Time)
	 */
	String seriesId;

	/**
	 * New Lecture item
	 * 
	 * <pre>
	 * @param id Lecture item ID (LectureId_Start-Unix-Timestamp)
	 * @param lectureId Lecture ID
	 * @param start Start DateTime
	 * @param end End DateTime
	 * @param name Lecture name
	 * @param module Lecture module
	 * @param location Lecture item location
	 * @param note Lecture item note, e.g. Übung
	 * @param url Lecture item URL
	 * @param seriesId Lecture item series ID (LectureID_Week-Day_Start-Time)
	 * </pre>
	 */
	public LectureItem(String id, String lectureId, Date start, Date end, String name, String module, String location,
			String note, String url, String seriesId) {
		this.id = id;
		this.lectureId = lectureId;
		this.start = start;
		this.end = end;
		this.name = name;
		this.module = module;
		this.location = location;
		this.note = note;
		this.url = url;
		this.seriesId = seriesId;
	}

	@Override
	public String toString() {
		return "id=" + id + ", lectureId=" + lectureId + ", start=" + Utils.getDateTimeString(start) + ", end="
				+ Utils.getDateTimeString(end) + ", name=" + name + ", module=" + module + ", location=" + location
				+ ", note=" + note + ", seriesId=" + seriesId + ", url=" + url;
	}

	/**
	 * Holiday object (extends LectureItem)
	 */
	public static class Holiday extends LectureItem {

		/**
		 * New Holiday
		 * 
		 * <pre>
		 * @param id Holiday ID
		 * @param date Date
		 * @param name Name, e.g. Allerheiligen
		 * </pre>
		 */
		public Holiday(String id, Date date, String name) {
			super(id, "holiday", date, date, "Feiertag", "", "", name, "about:blank", id);
		}
	}

	/**
	 * Vacation object (extends LectureItem)
	 */
	public static class Vacation extends LectureItem {

		/**
		 * New Vacation
		 * 
		 * <pre>
		 * @param id Vacation ID
		 * @param start Begin Date
		 * @param end End Date
		 * @param name Name, e.g. Sommerferien
		 * </pre>
		 */
		public Vacation(String id, Date start, Date end, String name) {
			super(id, "vacation", start, end, "Ferien", "", "", name, "about:blank", id);
		}
	}
}