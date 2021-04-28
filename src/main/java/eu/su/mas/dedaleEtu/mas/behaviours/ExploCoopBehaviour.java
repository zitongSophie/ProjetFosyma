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
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import org.graphstream.graph.Node;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
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
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  myInfo;
	private List<String> myAgentToShareMap; //agents to share the map
	private List<String> myAgentToAsk;
	private HashMap<String,String>agents_pos;
	private HashMap<String,String> finiExpl;
	private String posavant="-1";
	private int count=0;
	private String nextnodeblock;
/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */																												//add attribute
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> info,List<String> ata,List<String> atsm,HashMap<String,String> pos) {
		super(myagent);
		this.myMap=myMap;
		this.myInfo=info;	
		this.myAgentToAsk=ata;
		this.myAgentToAsk=((ExploreCoopAgent) this.myAgent).setAgentToAsk();
		this.myAgent=myagent;
		this.myAgentToShareMap=atsm;
		this.myAgentToShareMap=((ExploreCoopAgent) this.myAgent).setAgentToAsk();
		this.agents_pos=pos;
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
//===============add behaviour=====
			this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,this.myMap,this.myAgentToShareMap,this.myInfo));
			this.myAgent.addBehaviour(new ReceiveMapBehaviour(this.myAgent,this.myMap));
			//this.myAgent.addBehaviour(new ReceiveDeclareBehaviour(this.myAgent,this.agents_pos));
			this.myAgent.addBehaviour(new MeBehaviour(this.myAgent,this.agents_pos));
			this.myAgent.addBehaviour(new ReceiveNameBehaviour(this.myAgent,this.myAgentToShareMap));
		}
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<String> agentproche=new ArrayList<String>();
		for(String s:this.myAgentToShareMap) {
			agentproche.add(this.agents_pos.get(s));
		}
		if(posavant==myPosition && agentproche.contains(nextnodeblock)) {
			
			
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol("WHO_IS_HERE_PROTOCOL");
			msg.setContent(myPosition);
			for (String agentName : this.myAgentToAsk) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			
			
			((AbstractDedaleAgent)this.myAgent).moveTo(nextnodeblock);
			boolean isblock=true;
			for( String s:this.myMap.getnodeAdjacent(nextnodeblock)) {
				if(!agentproche.contains(s)) {
					isblock=false;
					break;
				}
			}
			
			if(isblock) {
				((ExploreCoopAgent) this.myAgent).setEnd();
				this.myAgent.addBehaviour(new SendBlockBehaviour(myAgent, agentproche));
				this.myAgent.addBehaviour(new AddBlockBehaviour(myAgent, myMap, agentproche));
				System.out.println(this.myAgent.getLocalName()+"\t"+myPosition+" \n\n\n- block wumpus before finished explore.\n\n\n");
				finished=true;
			}
		}else {
			if (myPosition!=null){
				//List of observable from the agent's current position
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
	
				/**
				 * Just added here to let you see what the agent is doing, otherwise he will be too quick
				 */
				try {
					this.myAgent.doWait(250);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//1) remove the current node from openlist and add it to closedNodes.
				this.myMap.addNode(myPosition, MapAttribute.closed);
				Node nn= this.myMap.getG().getNode(myPosition);
				for(String s : this.myInfo.keySet()) {
					Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> c;
					SerializableSimpleGraph<String,MapAttribute> updateSG;
					updateSG=this.myInfo.get(s).getRight();
					updateSG.addNode(nn.getId(),MapAttribute.valueOf((String)nn.getAttribute("ui.class")));
					c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(this.myInfo.get(s).getLeft(), updateSG);
					this.myInfo.put(s, c);
					//chgt indice couple
					Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> newc;
					// 1 car la carte a partage est vide puisque l'information bien d etre partage
					newc=((ExploreCoopAgent) this.myAgent).setCouple(s,this.myInfo.get(s).getRight(),2);
					this.myInfo.put(s, newc );
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
						c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(this.myInfo.get(s).getLeft(), updateSG);
						this.myInfo.put(s, c);
					}
					
				}
				
				//3) while openNodes is not empty, continues.
				if (!this.myMap.hasOpenNode()){
					//Explo finished
					finished=true;
					System.out.println(this.myAgent.getLocalName()+"\t"+myPosition+" \n\n\n- Exploration successufully done, behaviour removed.\n\n\n");
				}else{
					
					//4) select next move.
					//4.1 If there exist one open node directly reachable, go for it,
					//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
					if (nextNode==null){
						nextNode=this.myMap.getNextNode(myPosition,this.agents_pos,this.myAgentToShareMap,lobs);
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
					
					posavant=myPosition;
					nextnodeblock=nextNode;
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
					
				}
			}
		}

	}


	@Override
	public boolean done() {
		if(finished) {
			
			//s'ajouter comme fini
			if(!this.myMap.hasOpenNode()) {
				((ExploreCoopAgent) this.myAgent).setFini(this.myAgent.getLocalName());
				this.myAgent.addBehaviour(new SendEndBehaviour(this.myAgent,this.myAgentToAsk));
				this.myAgent.addBehaviour(new AddEndBehaviour(this.myAgent,this.myMap,this.myAgentToAsk));
				System.out.println(this.myAgent.getLocalName()+" remove ExploCoopBehaviour");
				
				
				
				HashMap<String,List<String>> myStench=new HashMap<String,List<String>>();
				Date tps=new java.util.Date();
				List<String> agentproche=new ArrayList<String>();
				// State names
				String c = "chasse"; 
				String r = "receive"; 
				String s = "send"; 
				String t="termine";
				String posavant="-1";
				FSMBehaviour fsm = new FSMBehaviour(this.myAgent); // Define the different states and behaviours 
				fsm.registerFirstState (new ChasseBehaviour(myAgent, myMap, agents_pos, myStench, tps, agentproche,posavant), c); 
				fsm.registerState (new ReceiveBehaviour(myAgent, agentproche, agents_pos, myStench, tps), r);
				fsm.registerState (new SendBehaviour(myAgent, agentproche, myStench), s); // Register the transitions
				fsm.registerLastState(new IsTermineBehaviour(myAgent, myMap, agents_pos, myStench, tps, agentproche,posavant,posavant,0),t);
				fsm.registerDefaultTransition (c,s);//Default 
				fsm.registerDefaultTransition (s,r);//Default 
				fsm.registerDefaultTransition (r,t);
				fsm. registerTransition (t,s, 2);//Cond 1 
				fsm. registerTransition (t,c, 1);//Cond 1 
				this.myAgent.addBehaviour(fsm);
			}
		}
		return finished;
	}
	
	
	
	
}
