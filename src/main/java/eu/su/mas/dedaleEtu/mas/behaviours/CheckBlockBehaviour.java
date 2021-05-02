package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;

public class CheckBlockBehaviour extends OneShotBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282088595445153555L;
	private HashMap<String,String>check_pos;
	private MapRepresentation myMap;
	private boolean finished=false;
	private List<String>pos_avant_next;
	private int exitvalue=1;
	public CheckBlockBehaviour(Agent myagent,MapRepresentation myMap,List<String>pos_avant_next,HashMap<String,String>check) {
		super(myagent);
		this.pos_avant_next=pos_avant_next;
		this.check_pos=check;
		this.myMap=myMap;
	}
	@Override
	public void action() {
		if(myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		String nextNode=this.pos_avant_next.get(1);
		//I can move to this position or i l'owrong
		if(((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)||((ExploreCoopAgent) this.myAgent).lstench().isEmpty()) {
			exitvalue=1;//hunt together
			finished=true;
		}else {
			final MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchProtocol("OK"));
			ACLMessage msg = this.myAgent.receive(msgTemplate);
			if(msg==null) {
				exitvalue=1;
				finished=true;
			}else {
				String s=msg.getContent();
				
				if(!check_pos.containsKey(msg.getSender().getLocalName())) {
					check_pos.put(msg.getSender().getLocalName(), s);
					ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
					msg2.setSender(this.myAgent.getAID());
					msg2.setProtocol("OK");
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);	
					msg2.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
					msg2.addReceiver(new AID(msg.getSender().getLocalName(),AID.ISLOCALNAME));
				}
				List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
				nodeAdj.remove(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
				if(this.check_pos.values().containsAll(nodeAdj)) {
					exitvalue=2;
					finished=true;
				}
			}
		}

	}
	@Override
	public int onEnd() {
		return exitvalue;
	}
}
