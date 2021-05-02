package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class IsAllFinishedBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	private MapRepresentation myMap;
	private HashMap<String,String>agents_pos;
	private int exitvalue=1;					//1:continue;
												//2:fini chasse solo fsm(fini block ou passer chasse together fsm)
	
	private List<String>pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */				
	private List<String>CgChasse;
	public IsAllFinishedBehaviour(final Agent myagent,List<String> cgChasse2) {
		super(myagent);
		this.CgChasse=cgChasse2;
	}

	@Override
	public void action() {
		if(myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		if(CgChasse.size()==((ExploreCoopAgent) this.myAgent).getAgentsListDF("coureur").size()) {
			exitvalue=2;
		}
			
		
	}
	public int onEnd() {return exitvalue ;}


}
