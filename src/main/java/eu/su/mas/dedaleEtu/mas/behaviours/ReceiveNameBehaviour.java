package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveNameBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = -8577400242202965285L;
	private boolean finished= false;
	private List<String> agentsToContact;
	//private MapRepresentation myMap;
	//private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  myInfo;
	
	public ReceiveNameBehaviour(final Agent myagent,List<String> toContact) {
		super(myagent);
		this.agentsToContact=toContact;
		this.agentsToContact=new ArrayList<String>();
		//this.myMap=map;
		//this.myInfo=info;

	}

	@Override
	public void action() {
		//System.out.println("ReceiveNameBehaviour");
		//1) receive a message
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("ME_PROTOCOL"));
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		while(msg!=null) {
			System.out.println(this.myAgent.getLocalName()+"<----Value received from "+msg.getSender().getLocalName());
			//String sender=msg.getContent();
			this.agentsToContact.add(msg.getSender().getLocalName());	//get all the agents who is here
			msg = this.myAgent.receive(msgTemplate);
		}
		
		final ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
		msg2.setProtocol("SHARE");
				//2) Set the sender and the receiver(s)
		msg2.setSender(this.myAgent.getAID());
		msg2.addReceiver(this.myAgent.getAID()); 
		//4) send the message
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
		//System.out.println("-----------------"+this.agentsToContact);
		//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,this.agentsToContact,this.myMap,this.myInfo));
		//this.finished=true;
	}



}
