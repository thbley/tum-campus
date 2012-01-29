package de.tum.in.tumcampus.models;

/**
 * Gallery object
 */
public class Gallery {

	/**
	 * Gallery Facebook-ID
	 */
	String id;

	/**
	 * Name, e.g. PartyX
	 */
	String name;

	/**
	 * Local image file, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 */
	String image;

	/**
	 * Position in gallery
	 */
	String position;

	/**
	 * New Event
	 * 
	 * <pre>
	 * @param id Event Facebook-ID
	 * @param name Name, e.g. PartyX
	 * @param image Local image, e.g. /mnt/sdcard/tumcampus/events/cache/xy.jpg
	 * @param position Position in gallery
	 * </pre>
	 */
	public Gallery(String id, String name, String image, String position) {

		this.id = id;
		this.name = name;
		this.image = image;
		this.position = position;
	}

	@Override
	public String toString() {
		return "id=" + id + " name=" + name + " image=" + image + " position="
				+ position;
	}
}