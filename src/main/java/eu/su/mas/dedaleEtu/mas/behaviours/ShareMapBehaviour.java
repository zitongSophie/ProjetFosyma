package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareMapBehaviour extends SimpleBehaviour{
	
	private MapRepresentation myMap;
	private List<String> listReceivers;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  agentsInfo;
	private boolean finished=false;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareMapBehaviour(Agent a,long period,MapRepresentation mymap, List<String> receivers, HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> agentsInfo) {
		super(a);
		this.myMap=mymap;
		this.listReceivers=receivers;	
		this.agentsInfo=agentsInfo;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		//System.out.println("ShareMapBehaviour");
		if(this.myMap==null) block();
		final MessageTemplate msgT = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("SHARE"));
		ACLMessage msgYes = this.myAgent.receive(msgT);
		for(String receiver : this.listReceivers) {
			SerializableSimpleGraph<String, MapAttribute> sg;
			

			if(this.agentsInfo.get(receiver).getLeft()==0) {
				sg=this.myMap.getSerializableGraph();
			}
			else{
				//System.out.println(this.myAgent.getLocalName()+" -----shareMysg");
				sg=this.agentsInfo.get(receiver).getRight();
				//sg=this.myMap.getSerializableGraph();
			}
				//sg=this.myMap.getSerializableGraph();
				//System.out.println(receiver+" shared map");
			
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-TOPO");
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
			
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			SerializableMessage smsg=new SerializableMessage(this.myAgent.getLocalName(),myPosition,sg);
		//msgReceived.getContentObject()).getsg()
		//msgReceived.getContentObject()).getname()
		//msgReceived.getContentObject()).getpos()
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
			//System.out.println(this.myAgent.getLocalName()+" shared map");
		
		}
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("Finished"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if(msgReceived!=null) {
			List<String> finiExpl=((ExploreCoopAgent) this.myAgent).getAgentsListDF("finiExplo") ;
			finiExpl.remove(this.myAgent.getLocalName());
			if(((ExploreCoopAgent) this.myAgent).isIdenticalList (this.agentsInfo.keySet(),finiExpl )){
				this.finished=true;
			}
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		if(finished) {
			System.out.println("ShareMapBehaviour is finished of "+this.myAgent.getLocalName());
		}
		return finished;
	}



}
