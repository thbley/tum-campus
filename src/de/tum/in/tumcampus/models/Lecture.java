package de.tum.in.tumcampus.models;

public class Lecture {
	int id;
	String name;

	public Lecture(int id, String name) {
		this.id = id;
		this.name = name;
		
		// TODO add module
	}

	public String toString() {
		return "id=" + id + ", name=" + this.name;
	}
}