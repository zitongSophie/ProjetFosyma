package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareMapBehaviour extends OneShotBehaviour{
	
	private MapRepresentation myMap;
	private HashMap<String,String> agents_pos;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  agentsInfo;
	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareMapBehaviour(Agent a,MapRepresentation mymap, HashMap<String,String> agents_pos, HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> agentsInfo) {
		super(a);
		this.myMap=mymap;	
		this.agentsInfo=agentsInfo;
		this.agents_pos=agents_pos;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	public void action() {
		//this.agents_pos=((ExploreCoopAgent) this.myAgent).get_agents_pos();
		if(this.myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		//System.out.println("======================\nShareMapBehaviour begin\n");
		for(String receiver : this.agents_pos.keySet()) {
			SerializableSimpleGraph<String, MapAttribute> sg=null;
			

			if(this.agentsInfo.get(receiver).getLeft()==0) {
				sg=this.myMap.getSerializableGraph();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SHARE-TOPO");
				msg.setSender(this.myAgent.getAID());
				msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
				
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				SerializableMessage smsg=new SerializableMessage(this.myAgent.getLocalName(),myPosition,sg);
				//SerializableMessage smsg=new SerializableMessage(this.myAgent.getLocalName(),myPosition,sg);
				try {					
					msg.setContentObject(smsg);
					//System.out.println("entree");
				} catch (IOException e) {
					e.printStackTrace();
				}
				Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> newc;
				// 1 car la carte a partage est vide puisque l'information bien d etre partage
				newc=((ExploreCoopAgent) this.myAgent).setCouple(receiver,new SerializableSimpleGraph<String,MapAttribute>(),1);
				this.agentsInfo.put(receiver, newc );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				//System.out.println(this.myAgent.getLocalName()+" type 0 sharemap to ---> "+receiver+" agents_pos===="+this.agents_pos);
			}
			else{
				if(this.agentsInfo.get(receiver).getLeft()==2) {
					sg=this.agentsInfo.get(receiver).getRight();
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("SHARE-TOPO");
					msg.setSender(this.myAgent.getAID());
					msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
					
					String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
					SerializableMessage smsg=new SerializableMessage(this.myAgent.getLocalName(),myPosition,sg);
					try {					
						msg.setContentObject(smsg);
						//System.out.println("entree");
					} catch (IOException e) {
						e.printStackTrace();
					}
					Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> newc;
					// 1 car la carte a partage est vide puisque l'information bien d etre partage
					newc=((ExploreCoopAgent) this.myAgent).setCouple(receiver,new SerializableSimpleGraph<String,MapAttribute>(),1);
					this.agentsInfo.put(receiver, newc );
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					//System.out.println(this.myAgent.getLocalName()+" type 2 sharemap to ---> "+receiver);
				}else {
					//System.out.println(this.myAgent.getLocalName()+" receiver "+receiver+" NO=====sharemap  agentinfo "+this.agentsInfo.get(receiver).getLeft());
				}
			}

			
		}

	}
		//System.out.println("======================\nShareMapBehaviour fini======================\n");
	


}
