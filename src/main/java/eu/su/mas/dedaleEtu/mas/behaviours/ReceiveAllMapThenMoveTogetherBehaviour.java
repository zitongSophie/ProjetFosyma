package eu.su.mas.dedaleEtu.mas.behaviours;

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

public class ReceiveAllMapThenMoveTogetherBehaviour extends OneShotBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282088595445153555L;

	private MapRepresentation myMap;
	
	public ReceiveAllMapThenMoveTogetherBehaviour(Agent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;	
	}
	@Override
	public void action() {
		//1) receive the message
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("go_move_together"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if(msgReceived!=null) {
			((ExploreCoopAgent) this.myAgent).setEnd();
		}else {
			block();
		}
		while (msgReceived!=null) {
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = ((SerializableMessage) msgReceived.getContentObject()).getsg();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(sgreceived);
			msgReceived=this.myAgent.receive(msgTemplate);
		}
		
	}


}
