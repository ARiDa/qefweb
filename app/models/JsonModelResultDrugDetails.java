package models;

import java.util.ArrayList;

import play.db.jpa.Model;

public class JsonModelResultDrugDetails extends Model {
	public ArrayList<JsonModelBindingDrugDetails> bindings;

	public ArrayList<JsonModelBindingDrugDetails> getBindings() {
		return bindings;
	}

	public void setBindings(ArrayList<JsonModelBindingDrugDetails> bindings) {
		this.bindings = bindings;
	}
	
}
