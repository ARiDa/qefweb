package models;

import org.hibernate.annotations.Entity;

import play.db.jpa.Model;

@Entity
public class JsonModelInitialDrugDetails extends Model {
	public JsonModelHead head;
	public JsonModelResultDrugDetails results;
	
	
}
