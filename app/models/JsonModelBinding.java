package models;

import java.util.ArrayList;

import play.db.jpa.Model;

public class JsonModelBinding extends Model {
	 
	public TripleModelType ds ;
	public TripleModelType dsn ;
	
	
	public TripleModelType getDs() {
		return ds;
	}
	public void setDs(TripleModelType ds) {
		this.ds = ds;
	}
	public TripleModelType getDsn() {
		return dsn;
	}
	public void setDsn(TripleModelType dsn) {
		this.dsn = dsn;
	}
	
	

}
