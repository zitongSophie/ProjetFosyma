package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class SerializableMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7126302223478378640L;
	private String position;
	private SerializableSimpleGraph<String, MapAttribute> sg;
	private String name;
	
	public SerializableMessage(String name,String pos,SerializableSimpleGraph<String, MapAttribute> map) {
		position=pos;
		sg=map;
		this.name=name;
		
		// TODO Auto-generated constructor stub
	}
	public String getpos() {
		return position;
	}
	public SerializableSimpleGraph<String, MapAttribute> getsg(){
		return sg;
	}
	public String getname() {
		return name;
	}

}
