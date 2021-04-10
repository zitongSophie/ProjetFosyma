package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
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
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class chasseBehaviour extends SimpleBehaviour {
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
	private List<String>PosOdeurs=new ArrayList<String>();

	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
		public chasseBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> info,List<String> ata,List<String> atsm,HashMap<String,String> pos) {
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
		// TODO Auto-generated method stub
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
					nextNode=this.myMap.getNextmove(myPosition,this.agents_pos,this.myAgentToShareMap,lobs,this.PosOdeurs);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					
//					System.out.println("\n"+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode+"\n");
					
					//System.out.println("normal iteration"+this.myMap.getNextNode(myPosition, this.agents_pos));
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
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
		// TODO Auto-generated method stub
		return false;
	}

}
