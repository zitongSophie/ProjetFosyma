package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dataStructures.tuple.Couple;

public class SMPosition implements Serializable {

	private static final long serialVersionUID = -7126302223478378640L;
	private String position; //ma POSITION ou NEXT POSITION
	private Couple<Date,List<String>> predictPosGolem;
	private Date myDate;

	public SMPosition(String pos,Date date, Couple<Date,List<String>> predictPosGolem) {// CHG ENLEVER agenArgue
		position=pos; //position du golem
		this.predictPosGolem=predictPosGolem; //liste des positions possibles du golem
		//this.agentsArgue=agentsArgue;
		this.myDate=date;
	}
	public String getpos() {
		return position;
	}

	public Couple<Date,List<String>> getPredicPosGolem() {
		return predictPosGolem;
	}
	
	public Date getDate() {
		return myDate;
	}
	/*
	public List<String> getAgentsAgue() {
		return agentsArgue;
	}*/

}
