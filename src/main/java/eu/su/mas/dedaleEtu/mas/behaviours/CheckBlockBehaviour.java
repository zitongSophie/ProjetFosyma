package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;

public class CheckBlockBehaviour extends OneShotBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282088595445153555L;
	private HashMap<String,String>check_pos;
	private MapRepresentation myMap;
	private List<String>pos_avant_next;
	private int exitvalue=1;
	public CheckBlockBehaviour(Agent myagent,MapRepresentation myMap,List<String>pos_avant_next,HashMap<String,String>check) {
		super(myagent);
		this.pos_avant_next=pos_avant_next;
		this.check_pos=check;
		this.myMap=myMap;
	}
	@Override
	public void action() {
		if(myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		
		
	String posavant=this.pos_avant_next.get(0);
	String nextNode=this.pos_avant_next.get(1);
	String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	if(posavant.equals(myPosition)&& !this.check_pos.containsValue(nextNode)&& !((ExploreCoopAgent) this.myAgent).lstench().isEmpty()) {
			Boolean isblock=true;
			List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
			for (String node:nodeAdj) {
				if(!node.equals(myPosition) && !this.check_pos.containsValue(node)) {
					exitvalue=1;
					isblock=false;
					break;
				}
			}
			
			if(isblock==true) {
				exitvalue=2;	
			}
		
		}
		

	}
	@Override
	public int onEnd() {
		return exitvalue;
	}

}
