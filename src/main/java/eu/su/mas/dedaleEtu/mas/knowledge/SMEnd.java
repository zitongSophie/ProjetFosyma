package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class SMEnd implements Serializable {

	private static final long serialVersionUID = -7126302223478378640L;
	private List<String> listFini;


	public SMEnd(List<String> lfini) {
		this.listFini=lfini; 
	}
	
	public List<String> getListFini() {
		return listFini;
	}
}
