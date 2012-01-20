package models;

import java.util.ArrayList;

import play.db.jpa.Model;

public class JsonModelResult extends Model {
	public ArrayList<JsonModelBinding> bindings;

	public ArrayList<JsonModelBinding> getBindings() {
		return bindings;
	}

	public void setBindings(ArrayList<JsonModelBinding> bindings) {
		this.bindings = bindings;
	}
	
}
