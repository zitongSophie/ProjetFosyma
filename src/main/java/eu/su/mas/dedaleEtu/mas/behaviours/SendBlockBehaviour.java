package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.SMEnd;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendBlockBehaviour extends SimpleBehaviour{
	private static final long serialVersionUID = 8802075205635695208L;
	private boolean finished= false;
	private List<String> agentsToContact;
	
	// Send to other agent that it finished
	public SendBlockBehaviour(final Agent myagent,List<String> toContact) {
		super(myagent);
		this.agentsToContact=toContact;
		this.agentsToContact=new ArrayList<String>();
	}
	
	@Override
	public void action() {
		//1) receive messages
		final ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setProtocol("ADD_ME_AS_BLOCK");
		//2) Set the sender and the receiver(s)
		msg2.setSender(this.myAgent.getAID());
		
		if(this.agentsToContact.isEmpty()) {
			for(String s:((ExploreCoopAgent) this.myAgent).getAgentsListDF("coureur")) {
				if(s==this.myAgent.getLocalName()) continue;
				msg2.addReceiver(new AID(s,AID.ISLOCALNAME));
			}
		}
		else {
			for(String s:this.agentsToContact) {
				if(s==this.myAgent.getLocalName()) continue;
				msg2.addReceiver(new AID(s,AID.ISLOCALNAME));
			}
		}
		try {
			msg2.setContentObject(new SMEnd(((ExploreCoopAgent) this.myAgent).getfiniblock()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//3) send the message
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
		if(((ExploreCoopAgent) this.myAgent).getendblock()) {
			finished=true;
		}
	}
	@Override
	public boolean done() {
		if(finished) {
			System.out.println(this.myAgent.getLocalName()+" remove SendEndBehaviour");
		}
		return finished;
	}
}
