package de.tum.in.tumcampus.models;

/**
 * Link object
 */
public class Link {
	/**
	 * Name, e.g. TUM
	 */
	String name;

	/**
	 * Url, e.g. http://www.in.tum.de
	 */
	String url;

	/**
	 * New Link
	 * 
	 * <pre>
	 * @param name Name, e.g. TUM
	 * @param url Url, e.g. http://www.in.tum.de
	 * </pre>
	 */
	public Link(String name, String url) {
		this.name = name;
		this.url = url;
	}

	@Override
	public String toString() {
		return "name=" + this.name + " url=" + this.url;
	}
}