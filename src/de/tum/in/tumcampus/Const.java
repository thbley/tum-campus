package de.tum.in.tumcampus;

/**
 * defines constants for database and settings
 */
public final class Const {

	/**
	 * database filename
	 */
	public final static String db = "database.db";

	/**
	 * database version used by SQLiteOpenHelper
	 */
	public final static int dbVersion = 1;

	/**
	 * constants for application settings
	 */
	public static final class settings {
		/**
		 * filter cafeterias by a substring
		 */
		public final static String cafeteriaFilter = "cafeteriaFilter";

		/**
		 * activate debug mode (debug activity and detailed error handling)
		 */
		public final static String debug = "debug";

		/**
		 * enable silence service, silence the mobile during lectures
		 */
		public final static String silence = "silence";
	}
}