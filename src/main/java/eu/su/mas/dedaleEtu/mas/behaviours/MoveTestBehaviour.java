package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import org.graphstream.graph.Node;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

//attribute agents_pos add ligne 69,78,82
//ligne 90 ajouter les behaviours sharemap(tickbehaviour),receivemap(simplebehaviour),receivedeclare(position,simplebehaviour)

//ligne 186 nextNode=this.myMap.getNextNode(myPosition,agents_pos,lobs);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);


//CHANGEMENT A PARTIR DE ligne180



/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class MoveTestBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private List<String> myAgentToAsk;
	private List<String> strenchPos;
	private String myPosition; //CHG
	private String myNexNode; // pour verifier quand un agent echange avec myAgent que ce n est pas la position d'un autre agent
	private List<String> posAgentNear;
	
/**
 * 
 * @param myAgent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */																												
	public MoveTestBehaviour(final Agent myAgent, MapRepresentation myMap) {
		super(myAgent);
		this.myMap=myMap;	
		this.strenchPos=new ArrayList<String>();
	}

	@Override
	public void action() {
		if(this.myMap==null) {
			System.out.println("Map is null, forget to explore the map");
		}
		
		if(!finished) {
			//_________________________________________________________________________
			this.myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}	
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			//System.out.println(this.myAgent.getLocalName()+lobs.toString());
			while(iter.hasNext()){
				String no=iter.next().getLeft();
				//System.out.println(no);
				this.strenchPos.add(no);
			}
			this.strenchPos.remove(this.myPosition);
			if(!this.strenchPos.isEmpty()) {
				String nn=null;
				
				Integer r=(int)( Math.random() *  (this.strenchPos.size())  );
				nn=this.strenchPos.get(r);
				this.strenchPos.clear();
				
				((AbstractDedaleAgent)this.myAgent).moveTo(nn);
			}
		
			if(((ExploreCoopAgent) this.myAgent).getFini()==1) {
				finished=true;
			}
		}
	}

	@Override
	public boolean done() {
		if(finished) {
			System.out.println(this.myAgent.getLocalName()+" block the Wumpus");
		}
		return finished;
	}	
}
