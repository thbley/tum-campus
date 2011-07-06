package de.tum.in.tumcampus.models;

import java.util.Date;

public class LectureItem {
	String id;
	String lectureId;
	Date start;
	Date end;
	String name;
	String module;
	String location;
	String note;
	String url;
	String seriesId;

	public LectureItem(String id, String lectureId, Date start, Date end,
			String name, String module, String location, String note,
			String url, String seriesId) {
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

	public String toString() {
		return "id=" + id + ", lectureId=" + lectureId + ", start="
				+ Utils.getDateTimeString(start) + ", end="
				+ Utils.getDateTimeString(end) + ", name=" + name + ", module="
				+ module + ", location=" + location + ", note=" + note
				+ ", seriesId=" + seriesId + ", url=" + url;
	}
}