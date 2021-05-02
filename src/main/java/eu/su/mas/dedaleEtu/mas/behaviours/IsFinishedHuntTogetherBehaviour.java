package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class IsFinishedHuntTogetherBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	private MapRepresentation myMap;
	private HashMap<String,String>agents_pos;
	private int exitvalue=1;					//1:continue;
												//2:fini chasse solo fsm(fini block ou passer chasse together fsm)
	private List<String>finiexpl;
	private List<String>pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
	public IsFinishedHuntTogetherBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String> pos,List<String>pos_avant_next,List<String>finiexpl) {
		super(myagent);
		this.myMap=myMap;
		this.myAgent=myagent;
		this.agents_pos=pos;
		this.pos_avant_next=pos_avant_next;
		this.finiexpl=finiexpl;
		
	}

	@Override
	public void action() {
		if(this.myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		
		String posavant=this.pos_avant_next.get(0);
		String nextNode=this.pos_avant_next.get(1);
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(posavant.equals(myPosition)&& !this.agents_pos.containsValue(nextNode)) {
			List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
			nodeAdj.remove(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			if(this.agents_pos.values().containsAll(nodeAdj)) {
				exitvalue=2;
			}
		}

		
		
	}
	public int onEnd() {return exitvalue ;}


}
