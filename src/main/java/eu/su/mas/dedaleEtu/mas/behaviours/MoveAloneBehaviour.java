package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Node;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class MoveAloneBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 8567689731496787661L;


	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private Date myTemps;
	private List<String> pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
		public MoveAloneBehaviour(final Agent myagent, MapRepresentation myMap,Date tps,List<String>pos_avant_next) {
			super(myagent);
			this.myMap=myMap;	
			this.myAgent=myagent;
			this.myTemps=tps;
			this.pos_avant_next=pos_avant_next;
		}

	@Override
	public void action() {
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String posavant=this.pos_avant_next.get(0);
		String nextNode=this.pos_avant_next.get(1);
		if (myPosition!=null){
			//List of observable from the agent's current position
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//cas normale :chercher aleatoire dans le map
			List<String>caseproche=this.myMap.getnodeAdjacent(myPosition) ;
			caseproche.remove(posavant);
			if(caseproche.isEmpty() && !posavant.equals("-1")) {
				caseproche.add(posavant);
			}
			Integer r=(int)( Math.random() *  caseproche.size()  );
			
			nextNode=caseproche.get(r);
			//mettre a jour l'odeurs obseve
			List<String>lstench=((ExploreCoopAgent) this.myAgent).lstench();
			
			//pas d'agent en communication.suive l'odeur
			lstench.remove(myPosition);
			lstench.remove(posavant);
			if(!lstench.isEmpty()) {
				nextNode=lstench.get(0);
			}
			
			
			posavant=myPosition;
			this.pos_avant_next.add(0, posavant);
			this.pos_avant_next.add(1, nextNode);
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			((ExploreCoopAgent) this.myAgent).setmyTemps();
			System.out. println ( "----mytime "+this.myTemps+" stench "+lstench+this.myAgent.getLocalName()+"current pos"+((AbstractDedaleAgent)this.myAgent).getCurrentPosition()+"next move"+nextNode+"agentname chassebehaviour\n--------" ) ;
			
		}
		
	}
		


	

}
