package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;

public class ReceiveMapBehaviour extends OneShotBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282088595445153555L;

	private MapRepresentation myMap;
	private HashMap<String,String> agents_pos;
	public ReceiveMapBehaviour(Agent myagent, MapRepresentation myMap,HashMap<String,String> agents_pos) {
		super(myagent);
		this.myMap=myMap;
		this.agents_pos=agents_pos;
	}
	@Override
	public void action() {
		if(this.myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		List<String>WishReceiveList=new ArrayList<String>();
		//1) receive the message
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		while(msgReceived!=null) {

			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived =  ((SerializableMessage) msgReceived.getContentObject()).getsg();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(sgreceived);
			
			if(!WishReceiveList.contains(msgReceived.getSender().getLocalName())) {
				WishReceiveList.add(msgReceived.getSender().getLocalName());
				//System.out.println(this.myAgent.getLocalName()+"receive map from----> "+msgReceived.getSender().getLocalName()+" receivemap behaviour ");
				
			}
			
			if(this.agents_pos.isEmpty() || WishReceiveList.size()==this.agents_pos.size()) {
				break;
			}
			if(WishReceiveList.size()==((ExploreCoopAgent) this.myAgent).getAgentName().size()) {//already receive map to merge from the agent who maybe want to send
				break;
			}
			msgReceived=this.myAgent.receive(msgTemplate);
		}
		
	}

}
