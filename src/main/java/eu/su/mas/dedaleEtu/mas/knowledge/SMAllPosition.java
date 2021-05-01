package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class SMAllPosition implements Serializable {

	private static final long serialVersionUID = -7126302223478378640L;
	private HashMap<String,String>  listFini;
	private Date d;

	public SMAllPosition(Date d,HashMap<String,String> lfini) {
		this.listFini=lfini; 
		this.d=d;
	}
	
	public HashMap<String,String> getListFini() {
		return listFini;
	}
	
	public Date getdate() {
		return d;
	}
	
}
