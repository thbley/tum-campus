package de.tum.in.tumcampus.models;

/**
 * Location object
 */
public class Location {

	/**
	 * Location ID
	 */
	int id;

	/**
	 * Category
	 */
	String category;

	/**
	 * Location name
	 */
	String name;

	/**
	 * Address
	 */
	String address;

	/**
	 * Next transport station
	 */
	String transport;

	/**
	 * Opening hours
	 */
	String hours;

	/**
	 * Opening hours #2
	 */
	String hours2;

	/**
	 * Remark
	 */
	String remark;

	/**
	 * URL
	 */
	String url;

	/**
	 * New Location
	 * 
	 * <pre>
	 * @param id Location ID, e.g. 100
	 * @param category Location category, e.g. library, cafeteria, info
	 * @param name Location name, e.g. Studentenwerksbibliothek
	 * @param address Address, e.g. Arcisstr. 21
	 * @param transport Transportation station name, e.g. U2 Königsplatz
	 * @param hours Opening hours, e.g. Mo–Fr 8–24
	 * @param hours2 Opening hours2, e.g. Sa, So 10–22
	 * @param remark Additional information, e.g. Tel: 089-11111
	 * @param url Location URL, e.g. http://stud.ub.uni-muenchen.de/
	 * </pre>
	 */
	public Location(int id, String category, String name, String address,
			String transport, String hours, String hours2, String remark,
			String url) {
		this.id = id;
		this.category = category;
		this.name = name;
		this.address = address;
		this.transport = transport;
		this.hours = hours;
		this.hours2 = hours2;
		this.remark = remark;
		this.url = url;
	}

	@Override
	public String toString() {
		return "id=" + id + ", category=" + category + ", name=" + name
				+ ", address=" + address + ", transport=" + transport
				+ ", hours=" + hours + ", hours2=" + hours2 + ", remark="
				+ remark + ", url=" + url;
	}
}