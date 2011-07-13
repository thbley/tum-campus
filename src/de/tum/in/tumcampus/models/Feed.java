package de.tum.in.tumcampus.models;

public class Feed {
	String name;
	String feedUrl;

	public Feed(String name, String feedUrl) {
		this.name = name;
		// TODO optimize
		this.feedUrl = Utils.getRssLinkFromUrl(feedUrl);
	}

	public String toString() {
		return "name=" + this.name + " feedUrl=" + this.feedUrl;
	}
}