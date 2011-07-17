package de.tum.in.tumcampus.models;

import java.util.Date;

public class CafeteriaMenu {
	int id;
	int mensaId;
	Date date;
	String typeShort;
	String typeLong;
	int typeNr;
	String name;
	boolean addendum; // Beilage

	public CafeteriaMenu(int id, int mensaId, Date date, String typeShort,
			String typeLong, int typeNr, String name) {

		this.id = id;
		this.mensaId = mensaId;
		this.date = date;
		this.typeShort = typeShort;
		this.typeLong = typeLong;
		this.typeNr = typeNr;
		this.name = name;
		if (typeNr == 0) {
			this.addendum = true;
		}
	}

	@Override
	public String toString() {
		return "id=" + this.id + " mensaId=" + this.mensaId + " date="
				+ Utils.getDateString(this.date) + " typeShort="
				+ this.typeShort + " typeLong=" + this.typeLong + " typeNr="
				+ this.typeNr + " name=" + this.name + " addendum="
				+ this.addendum;
	}
}