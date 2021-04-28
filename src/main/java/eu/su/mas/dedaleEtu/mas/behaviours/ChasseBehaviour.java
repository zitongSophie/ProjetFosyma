package eu.su.mas.dedaleEtu.mas.behaviours;

import java.time.LocalTime;
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

public class ChasseBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 8567689731496787661L;


	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private List<String> agentsNames;
	private HashMap<String,String>agents_pos;
	private HashMap<String,List<String>> myStench;
	private String posavant="-1";
	private Date myTemps;
	private List<String> agentproche;
	private int exitvalue=2;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
		public ChasseBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String> pos,HashMap<String,List<String>> myStench,Date tps,List<String>ata,String posavant) {
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
			
		}

	@Override
	public void action() {
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

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
			
			String nextNode=caseproche.get(r);
			//mettre a jour l'odeurs obseve
			List<String>lstench=((ExploreCoopAgent) this.myAgent).lstench();
			
			//pas d'agent en communication.suive l'odeur
			lstench.remove(myPosition);
			if(!lstench.isEmpty()) {
				nextNode=lstench.get(0);
			}
			
			//agent en communication
			if(!this.myStench.isEmpty()) {
				this.myStench.put(this.myAgent.getLocalName(), lstench);
				HashMap<String, Integer> count=new HashMap<String, Integer>();
				for (List<String>listpos:myStench.values()) {
					for (String pos:listpos) {
						if(count.containsKey(pos)) {
							count.put(pos, count.get(pos)+1);
						}else {
							count.put(pos, 1);
						}
					}
				}
				String pos = myPosition;
				int i=0;
				for (String key:count.keySet()) {
					if(count.get(key)>i) {
						pos=key;
						i=count.get(key);
					}
				}
				caseproche=this.myMap.getnodeAdjacent(pos) ;
				r=(int)( Math.random() *  caseproche.size()  );
				nextNode=this.myMap.getShortestPath(myPosition, caseproche.get(r)).get(0);
				System.out. println ( "-------mytime"+this.myTemps+"stench "+lstench+this.myAgent.getLocalName()+" next move"+nextNode+"agentname"+this.agentsNames+"\n--------" ) ;
			}
			System.out. println ( "----mytime "+this.myTemps+" agentproche "+this.agentproche+" stench "+lstench+this.myStench+this.myAgent.getLocalName()+" next move"+nextNode+"agentname"+this.agentsNames+" chassebehaviour\n--------" ) ;
			myTemps=new java.util.Date();
			this.posavant=myPosition;
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			this.myAgent.addBehaviour(new IsTermineBehaviour(myAgent, myMap, agents_pos, myStench, myTemps, agentproche,posavant,nextNode));
		}
		
	}
		


	@Override
	public int onEnd() {return exitvalue ;}
	
	

}
