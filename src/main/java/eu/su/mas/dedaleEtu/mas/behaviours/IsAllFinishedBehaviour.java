package eu.su.mas.dedaleEtu.mas.behaviours;

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
	 */																												//add attribute
	public IsAllFinishedBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String>agents_pos,List<String>pos_avant_next,List<String>lstench) {
		super(myagent);
		this.myMap=myMap;
		this.agents_pos=agents_pos;
		this.pos_avant_next=pos_avant_next;
	}

	@Override
	public void action() {
		if(myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		String posavant=this.pos_avant_next.get(0);
		String nextNode=this.pos_avant_next.get(1);
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(posavant.equals(myPosition)&& !this.agents_pos.containsValue(nextNode)) {
			Boolean isblock=true;
			List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
			if(this.myMap.getOpenNodes().contains(nextNode)) {
				isblock=false;
			}
			for (String node:nodeAdj) {
				if(this.myMap.getOpenNodes().contains(node)) {
					isblock=false;
				}
				if(!node.equals(myPosition) && !this.agents_pos.containsValue(node)) {
					isblock=false;
				}
			}
			
			if(isblock==true) {
				exitvalue=2;
				((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(2);
				System.out.println(this.myAgent.getLocalName()+"Exploration finished because block wumpus"+" pos_wumpus "+nextNode+"nodeadj"+nodeAdj+" agents_pos "+agents_pos);

			}
		}
		if(!myMap.hasOpenNode()) {
			if(!this.agents_pos.isEmpty()) {
				((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(0);//a changer
				
			}
			exitvalue=2;
			System.out.println(this.myAgent.getLocalName()+"\t"+" \n\n\n- Exploration successufully done, behaviour removed.\n\n\n");
			
			
		}
		
		
		
		
	}
	public int onEnd() {return exitvalue ;}


}
