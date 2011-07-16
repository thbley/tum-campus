package de.tum.in.tumcampus.models;

public class Link {
	String name;
	String url;

	public Link(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String toString() {
		return "name=" + this.name + " url=" + this.url;
	}
}