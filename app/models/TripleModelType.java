package models;

import play.db.jpa.Model;

public class TripleModelType extends Model {
	public String type;
	public String value;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
