package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import java.util.ArrayList;
import java.util.Date;
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
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import org.graphstream.graph.Node;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


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
public class ExploCoopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  myInfo;
	private HashMap<String,String>agents_pos;
	private List<String>pos_avant_next;

	/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */																												//add attribute
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> info,HashMap<String,String>agents_pos,List<String>pos_avant_next) {
		super(myagent);
		this.myMap=myMap;
		this.myInfo=info;	
		this.agents_pos=agents_pos;
		this.pos_avant_next=pos_avant_next;
	}

	@Override
	public void action() {
		
		//this.agents_pos=((ExploreCoopAgent) this.myAgent).get_agents_pos();
		//System.out.println("\n\n======================");
		//System.out.println(this.myAgent.getLocalName()+"explo begin agents_pos"+agents_pos);
		if(this.myMap==null) {
			MapRepresentation map=new MapRepresentation();
			//System.out.println("======================explocoop mymap init null \n");
			this.myMap= map;
			(( ExploreCoopAgent) this.myAgent).setMap(myMap);
		}
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			if(!((ExploreCoopAgent) this.myAgent).lstench().isEmpty()) {
				
			}
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//1) remove the current node from openlist and add it to closedNodes
			this.myMap.addNode(myPosition, MapAttribute.closed);
			Node nn= this.myMap.getG().getNode(myPosition);
			for(String s : this.myInfo.keySet()) {
				Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> c;
	
				SerializableSimpleGraph<String,MapAttribute> updateSG;
				if(this.myInfo.get(s).getLeft()==1) {
					
					updateSG = new SerializableSimpleGraph<String,MapAttribute>();
				}else {
					updateSG=this.myInfo.get(s).getRight();
				}
				updateSG.addNode(nn.getId(),MapAttribute.valueOf((String)nn.getAttribute("ui.class")));
				c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(this.myInfo.get(s).getLeft(), updateSG);
				this.myInfo.put(s, c);
				//chgt indice couple
				//Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> newc;
				// 1 car la carte a partage est vide puisque l'information bien d etre partage
				//newc=((ExploreCoopAgent) this.myAgent).setCouple(s,this.myInfo.get(s).getRight(),this.myInfo.get(s).getLeft());
				this.myInfo.put(s, c );
			}
		
			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//faire la meme chose dans chaque sg de myInfo
				//obtenir l identifiant de l arete creer en regardant si nbEdge a augmente
				Integer edgeIDbefore=this.myMap.getNbEdges();
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
				Integer edgeIDafter=this.myMap.getNbEdges();
				Node n=this.myMap.getG().getNode(nodeId);
				for(String s : this.myInfo.keySet()) {
					Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> c;
					SerializableSimpleGraph<String,MapAttribute> updateSG;
					updateSG=this.myInfo.get(s).getRight();
					updateSG.addNode( n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
					//System.out.println("node add "+updateSG.getNode(n.getId()));
					if (myPosition!=nodeId) {
						updateSG.addEdge(nn.getEdgeBetween(n).getId(),nn.getId(),n.getId());
					}
					c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(2, updateSG);
					this.myInfo.put(s, c);
				}
				
			}
			
			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					nextNode=this.myMap.getNextNode(myPosition,this.agents_pos);
					
				}else {
					if(this.agents_pos.containsValue(nextNode)) {
						List<String> nodeAdj=this.myMap.getnodeAdjacent(myPosition);
						Integer r=(int)( Math.random() *  nodeAdj.size()  );
						nextNode=nodeAdj.get(r);
					}
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				String avant=myPosition;
				String nextNodeblock=nextNode;
				this.pos_avant_next.add(0, avant);
				this.pos_avant_next.add(1, nextNodeblock);
				(( ExploreCoopAgent) this.myAgent).setmyTemps();
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}

		}
		//System.out.println("EXPLO COOP BEHAVIOUR\n\n\n======================");
	}

	
	
}
