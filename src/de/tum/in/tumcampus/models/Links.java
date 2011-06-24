package de.tum.in.tumcampus.models;

public class Links {
	int id;
	String name;
	String url;

	public Links(int id, String name, String url) {
		this.id = id;
		this.name = name;
		this.url = url;
	}

	public String toString() {
		return "id=" + this.id + " name=" + this.name + " url="
				+ this.url;
	}
}
