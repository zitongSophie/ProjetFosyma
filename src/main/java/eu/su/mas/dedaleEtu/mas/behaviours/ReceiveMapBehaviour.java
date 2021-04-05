package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;

public class ReceiveMapBehaviour extends SimpleBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282088595445153555L;

	private MapRepresentation myMap;

	
	public ReceiveMapBehaviour(Agent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
		
		
	}
	@Override
	public void action() {
		//1) receive the message
		//System.out.println("ReceiveMapBehaviour");
		/*try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		while (msgReceived!=null) {
			//System.out.println(this.myAgent.getLocalName()+ "received a message from "+msgReceived.getSender().getLocalName());
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			//String mgSender=null;
			//String senderPos=null;
			try {
				sgreceived = ((SerializableMessage) msgReceived.getContentObject()).getsg();
				//mgSender=((SerializableMessage) msgReceived.getContentObject()).getname();
				//senderPos=((SerializableMessage) msgReceived.getContentObject()).getpos();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(sgreceived);
			msgReceived=this.myAgent.receive(msgTemplate);
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
