package models;

import java.util.ArrayList;

import play.db.jpa.Model;

public class JsonModelBindingDrug extends Model {
	 
	public TripleModelType ds ;
	
	
	
	public TripleModelType getDs() {
		return ds;
	}
	public void setDs(TripleModelType ds) {
		this.ds = ds;
	}
	
	
	

}
