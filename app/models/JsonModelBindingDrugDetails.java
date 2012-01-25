package models;

import java.util.ArrayList;

import play.db.jpa.Model;

public class JsonModelBindingDrugDetails extends Model {
	 
	
	public TripleModelType dgn;
	public TripleModelType dg_act_ing;
	public TripleModelType dg_frm;
	public TripleModelType sd_eff;
	
	public TripleModelType getDgn() {
		return dgn;
	}
	public void setDgn(TripleModelType dgn) {
		this.dgn = dgn;
	}
	
	public TripleModelType getDg_act_ing() {
		return dg_act_ing;
	}
	public void setDg_act_ing(TripleModelType dg_act_ing) {
		this.dg_act_ing = dg_act_ing;
	}
	public TripleModelType getDg_frm() {
		return dg_frm;
	}
	public void setDg_frm(TripleModelType dg_frm) {
		this.dg_frm = dg_frm;
	}
	public TripleModelType getSd_eff() {
		return sd_eff;
	}
	public void setSd_eff(TripleModelType sd_eff) {
		this.sd_eff = sd_eff;
	}
	
}
