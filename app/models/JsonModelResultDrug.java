package models;

import java.util.ArrayList;

import play.db.jpa.Model;

public class JsonModelResultDrug extends Model {
	public ArrayList<JsonModelBindingDrug> bindings;

	public ArrayList<JsonModelBindingDrug> getBindings() {
		return bindings;
	}

	public void setBindings(ArrayList<JsonModelBindingDrug> bindings) {
		this.bindings = bindings;
	}
	
}
