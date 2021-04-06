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
			this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.myMap,this.myAgentToShareMap,this.myInfo));
			this.myAgent.addBehaviour(new ReceiveMapBehaviour(this.myAgent,this.myMap));
			//this.myAgent.addBehaviour(new ReceiveDeclareBehaviour(this.myAgent,this.agents_pos));
			this.myAgent.addBehaviour(new MeBehaviour(this.myAgent,this.agents_pos));
			this.myAgent.addBehaviour(new ReceiveNameBehaviour(this.myAgent,this.myAgentToShareMap));
		}

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

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
				//c=((ExploreCoopAgent) this.myAgent).setSgNode(s,myPosition,MapAttribute.open);
				SerializableSimpleGraph<String,MapAttribute> updateSG;
				
				//updateSG=this.myMap.addNodeSG(this.myInfo.get(s).getRight(), myPosition,MapAttribute.closed);
				updateSG=this.myInfo.get(s).getRight();
				//System.out.println("dedans jbjbjbjbjbjb "+nn.getId());
				updateSG.addNode(nn.getId(),MapAttribute.valueOf((String)nn.getAttribute("ui.class")));
				c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(this.myInfo.get(s).getLeft(), updateSG);
				this.myInfo.put(s, c);
				//chgt indice couple
				Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> newc;
				// 1 car la carte a partage est vide puisque l'information bien d etre partage
				newc=((ExploreCoopAgent) this.myAgent).setCouple(s,this.myInfo.get(s).getRight(),2);
				this.myInfo.put(s, newc );
				//System.out.println("apres moi "+this.myInfo.get(s).getRight().getNode(myPosition));
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
					//System.out.println("apres moi "+this.myInfo.get(s).getRight().getNode(myPosition));
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
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
//					System.out.println(this.myAgent.getLocalName()+" begin\t myposition "+myPosition+"\t agents_pos "+agents_pos);
/**###changement##	
 * #**/
					
					//changer ce ligne
					nextNode=this.myMap.getNextNode(myPosition,this.agents_pos,this.myAgentToShareMap,lobs);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					
//					System.out.println("\n"+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode+"\n");
					
					//System.out.println("normal iteration"+this.myMap.getNextNode(myPosition, this.agents_pos));
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}


				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.

/**
 * 
 * **/				
//--------------------------------------AJOUTER ET MODIFICATION A PARTIR DE CETTE LIGNE, COMMMUNICATION D'AGENT, POUR LE MERGE DE MAP ET DECIDER NEXTNODE-----------------------
//===============================================================================================================

				//declare position
/*send the message to declare my position to the agent nearby*/
				
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
		//System.out.println(this.myAgent.getLocalName()+ " enregistre comme finiExplo");
		if(finished) {
			//deregister
			DFAgentDescription dfd = new DFAgentDescription();
			this.myAgent.getDefaultDF().setName(((AbstractDedaleAgent)this.myAgent).getAID().getLocalName()); 
			// The agent AID
			ServiceDescription sd = new ServiceDescription () ;
			sd.setType( "courreur" ); // You have to give a
			sd.setName(((AbstractDedaleAgent)this.myAgent).getLocalName());//(local)name of
			dfd.addServices(sd);
			//Register the service

			try {
				DFService.deregister( ((AbstractDedaleAgent)this.myAgent), dfd );
				System.out. println ( "------- supprime agent courreur"+this.myAgent.getLocalName()+" \n--------" ) ;
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//register
			DFAgentDescription dfd2 = new DFAgentDescription();
			this.myAgent.getDefaultDF().setName(((AbstractDedaleAgent)this.myAgent).getAID().getLocalName()); 
			// The agent AID
			ServiceDescription sd2 = new ServiceDescription () ;
			sd2.setType( "finiExplo" ); // You have to give a
			sd2.setName(((AbstractDedaleAgent)this.myAgent).getLocalName());//(local)name of
			dfd2.addServices(sd2);
			//Register the service
			DFAgentDescription result;
			try {
				result = DFService.register( this.myAgent , dfd );
				System.out. println ( "-------\n"+result+ "results \n--------" ) ;
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return finished;
	}
	
	
	
	
}
