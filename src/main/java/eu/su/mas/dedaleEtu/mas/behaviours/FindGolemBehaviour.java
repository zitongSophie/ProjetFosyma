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
public class FindGolemBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  myInfo;
	private List<String> myAgentToAsk;
	private HashMap<String,String>agents_pos;
	private List<String> strenchPos;

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */																												//add attribute
	public FindGolemBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> info,List<String> ata,HashMap<String,String> pos) {
		super(myagent);
		this.myMap=myMap;
		this.myInfo=info;	
		this.myAgentToAsk=ata;
		this.myAgentToAsk=((ExploreCoopAgent) this.myAgent).setAgentToAsk();
		this.myAgent=myagent;
		this.agents_pos=pos;
		this.strenchPos=new ArrayList<String>();
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			System.out.println("Map is null, forget to explore the map");

		}

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}


			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String obstype=iter.next().getRight().get(0).getLeft().getName();
				if(obstype=="strench") {
					return;
				}
			
			}
			
			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				finished=true;
				System.out.println(this.myAgent.getLocalName()+"\t"+myPosition+" \n\n\n- Exploration successufully done, behaviour removed.\n\n\n");
			}else{
				
				if (nextNode==null){
					//nextNode=this.myMap.getNextNode(myPosition,this.agents_pos,this.myAgentToShareMap,lobs);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
				
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("WHO_IS_HERE_PROTOCOL");
				msg.setContent(myPosition);
				for (String agentName : this.myAgentToAsk) {
					msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
				
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
		}
	}



	@Override
	public boolean done() {
		return finished;
	}
	
	
	
	
}
