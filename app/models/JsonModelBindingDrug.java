package models;

import java.util.ArrayList;

import play.db.jpa.Model;



public class JsonModelBindingDrug extends Model {
	 
	public TripleModelType dg;
	public TripleModelType dgn;

	public void setDg(TripleModelType dg) {
		this.dg = dg;
	}
	public TripleModelType getDg() {
		return dg;
	}
	public void setDgn(TripleModelType dgn) {
		this.dgn = dgn;
	}
	public TripleModelType getDgn() {
		return dgn;
	}
}
