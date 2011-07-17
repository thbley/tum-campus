package de.tum.in.tumcampus.models;

/**
 * Feed object
 */
public class Feed {

	/**
	 * Feed name, e.g. Spiegel
	 */
	String name;

	/**
	 * Feed Url, e.g. http://www.spiegel.de/schlagzeilen/index.rss
	 */
	String feedUrl;

	/**
	 * New Feed
	 * 
	 * <pre>
	 * @param name Feed name, e.g. Spiegel
	 * @param feedUrl Feed Url, e.g. http://www.spiegel.de/schlagzeilen/index.rss
	 * </pre>
	 */
	public Feed(String name, String feedUrl) {
		this.name = name;
		this.feedUrl = feedUrl;
	}

	@Override
	public String toString() {
		return "name=" + this.name + " feedUrl=" + this.feedUrl;
	}
}