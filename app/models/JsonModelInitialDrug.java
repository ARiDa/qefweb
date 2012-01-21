package models;

import org.hibernate.annotations.Entity;

import play.db.jpa.Model;

@Entity
public class JsonModelInitialDrug extends Model {
	public JsonModelHead head;
	public JsonModelResultDrug results;
	
	
}
