package models;

import org.hibernate.annotations.Entity;

import play.db.jpa.Model;

@Entity
public class JsonModelInitial extends Model {
	public JsonModelHead head;
	public JsonModelResult results;
	
}
