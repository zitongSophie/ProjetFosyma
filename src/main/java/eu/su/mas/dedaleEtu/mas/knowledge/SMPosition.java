package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class SMPosition implements Serializable {

	private static final long serialVersionUID = -7126302223478378640L;
	private String position;
	private List<String> predictPosGolem;
	private List<String> agentsArgue;

	public SMPosition(String pos,List<String>predicPosGolem,List<String> agentsArgue) {
		position=pos; //position du golem
		this.predictPosGolem=predictPosGolem; //liste des positions possibles du golem
		this.agentsArgue=agentsArgue;
		// TODO Auto-generated constructor stub
	}
	public String getpos() {
		return position;
	}

	public List<String> getPredicPosGolem() {
		return predictPosGolem;
	}
	public List<String> getAgentsAgue() {
		return agentsArgue;
	}

}
