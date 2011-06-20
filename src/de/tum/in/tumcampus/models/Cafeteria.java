package de.tum.in.tumcampus.models;

public class Cafeteria {
	int id;
	String name;
	String address;

	public Cafeteria(int id, String name, String address) {
		this.id = id;
		this.name = name;
		this.address = address;
	}

	public String toString() {
		return "id=" + this.id + " name=" + this.name + " address="
				+ this.address;
	}
}
