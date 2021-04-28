package eu.su.mas.dedaleEtu.mas.behaviours;

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
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class IsTermineBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	private boolean isBlock=false;
	
	private MapRepresentation myMap;
	private List<String> agentsNames;
	private HashMap<String,String>agents_pos;
	private HashMap<String,List<String>> myStench;
	private Date myTemps;
	private List<String> agentproche;
	private int exitvalue=2;
	private String posavant;
	private String nextNode;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
	public IsTermineBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String> pos,HashMap<String,List<String>> myStench,Date tps,List<String>ata,String posavant,String nextNode) {
		super(myagent);
		this.myMap=myMap;
		this.myStench=myStench;	
		this.myAgent=myagent;
		this.agents_pos=pos;
		this.myTemps=tps;
		this.agentsNames=new ArrayList<String>();
		this.agentsNames.add("1stAgent");
		this.agentsNames.add("2ndAgent");
		this.agentsNames.remove(this.myAgent.getLocalName());
		this.agentproche=ata;
		this.posavant=posavant;
		this.nextNode=nextNode;
		
	}

	@Override
	public void action() {
		if(posavant==((AbstractDedaleAgent)this.myAgent).getCurrentPosition()) {
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
		}
		
		if(posavant==((AbstractDedaleAgent)this.myAgent).getCurrentPosition()) {
			if(!agents_pos.containsValue(nextNode)) {
				isBlock=true;
				exitvalue=1;
				myTemps=new java.util.Date();
				this.myAgent.addBehaviour(new SendBehaviour(myAgent, agentproche, myStench));
				this.myAgent.addBehaviour(new ReceiveBehaviour(myAgent, agentproche, agents_pos, myStench, myTemps));
				this.myAgent.addBehaviour(new IsTermineBehaviour(myAgent, myMap, agents_pos, myStench, myTemps, agentproche,posavant,nextNode));
				System.out. println ( "---block---mytime "+this.myTemps+"-------stench "+this.myStench+this.myAgent.getLocalName()+" isterminebehaviour\n--------" ) ;
			}
		}
		if(!isBlock) {
			this.myAgent.addBehaviour(new ChasseBehaviour(myAgent, myMap, agents_pos, myStench, myTemps, agentproche,posavant));
			this.myAgent.addBehaviour(new SendBehaviour(myAgent, agentproche, myStench));
			this.myAgent.addBehaviour(new ReceiveBehaviour(myAgent, agentproche, agents_pos, myStench, myTemps));
			System.out. println ( "mytime "+this.myTemps+"-------stench "+this.myStench+this.myAgent.getLocalName()+" isterminebehaviour\n--------" ) ;
			
		}
	}
	public int onEnd() {return exitvalue ;}


}
