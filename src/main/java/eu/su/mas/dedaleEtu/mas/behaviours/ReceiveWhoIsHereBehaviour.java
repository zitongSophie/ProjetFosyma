package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveWhoIsHereBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	
	private MapRepresentation myMap;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> agentsInfo;
	private int exitvalue=1;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
	public ReceiveWhoIsHereBehaviour(final Agent myagent, MapRepresentation myMap, HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> agentsInfo) {
		super(myagent);
		this.myMap=myMap;
		this.myAgent=myagent;
		this.agentsInfo=agentsInfo;
		
	}

	@Override
	public void action() {
		
		HashMap<String,Date>time=new HashMap<String,Date>();
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("WHO_IS_HERE_PROTOCOL"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		List<String>receivers=new ArrayList<String>();
		if(msg!=null) {
			System.out.println("MoveAloneBehaviour termine car communication");
			exitvalue=2;
			this.myAgent.addBehaviour(new MoveTogetherBehaviour(myAgent, myMap, null, receivers));
		}
		while(msg!=null) {
			if(!receivers.contains(msg.getSender().getLocalName())) {
				receivers.add(msg.getSender().getLocalName());
			}
			msg = this.myAgent.receive(msgTemplate);
		}
		for(String receiver : receivers) {
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
			
			
			ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
			msg2.setProtocol("go_move_together");
			msg2.setSender(this.myAgent.getAID());
			msg2.addReceiver(new AID(receiver,AID.ISLOCALNAME));
			
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			SerializableMessage smsg=new SerializableMessage(this.myAgent.getLocalName(),myPosition,sg);
		}
		System.out. println ( "----mytime "+((ExploreCoopAgent) this.myAgent).getmyTemps()+this.myAgent.getLocalName()+"current pos"+((AbstractDedaleAgent)this.myAgent).getCurrentPosition()+"Receivewhoisherebehaviour\n--------" ) ;
		
		
	}
	public int onEnd() {return exitvalue ;}


}
