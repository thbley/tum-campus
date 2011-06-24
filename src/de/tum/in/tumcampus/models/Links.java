package de.tum.in.tumcampus.models;

public class Links {
	int id;
	String name;
	String url;
	String icon;

	public Links(int id, String name, String url, String icon) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.icon = icon;
	}

	public String toString() {
		return "id=" + this.id + " name=" + this.name + " url="
				+ this.url + " icon="+icon;
	}
}
