package de.tum.in.tumcampus.models;

public class Feed {
	int id;
	String name;
	String feedUrl;

	public Feed(int id, String name, String feedUrl) {
		this.id = id;
		this.name = name;
		this.feedUrl = feedUrl;
	}

	public String toString() {
		return "id=" + this.id + " name=" + this.name + " feedUrl="
				+ this.feedUrl;
	}
}