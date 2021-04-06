package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;

public class ReceivePPGolemBehaviour extends SimpleBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282088595445153555L;

	private MapRepresentation myMap;
	private List<String> myPPGolem; //les positions possibles du golem : deja pris les intersections
	private HashMap<String,String> myAgentInfo;
	
	public ReceivePPGolemBehaviour(Agent myagent, MapRepresentation myMap,HashMap<String,String> agentsPos) {
		super(myagent);
		this.myMap=myMap;
		this.myPPGolem=null;
		this.myAgentInfo=agentsPos;
	}
	@Override
	public void action() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-PREDICT-POS-GOLEM"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		Integer nbAgentInContact=0;
		if(msgReceived!=null) {//si on a un agent central
			nbAgentInContact+=1;
			try {
				this.myPPGolem = ((SMPosition) msgReceived.getContentObject()).getPredicPosGolem();
				this.myAgentInfo.put(msgReceived.getSender().getLocalName(), ((SMPosition) msgReceived.getContentObject()).getpos());
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String nextNode=null;
			for (String myPosition : this.myAgentInfo.keySet()) {
				nextNode=this.myMap.getNextNode(myPosition,this.myAgentInfo);
			}
			msgReceived=this.myAgent.receive(msgTemplate);
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
