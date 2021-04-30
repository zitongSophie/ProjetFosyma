package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveNameBehaviour extends SimpleBehaviour {
	private static final long serialVersionUID = -8577400242202965285L;
	private boolean finished= false;
	private List<String> agentsToContact;
	//private MapRepresentation myMap;
	//private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  myInfo;
	//------------------------------------------------
	private List<String> posAgentReceived=new ArrayList<String>();
	//------------------------------------------------
	
	
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
		System.out.println(this.myAgent.getLocalName()+" ReceiveNameBehaviour");
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.agentsToContact=new ArrayList<String>();
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("ME_PROTOCOL"));
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		if(msg!=null) {
			System.out.println(this.myAgent.getLocalName()+"<----Value received from "+msg.getSender().getLocalName());
			while(msg!=null) {
				
				//String sender=msg.getContent();
				this.agentsToContact.add(msg.getSender().getLocalName());	//get all the agents who is here

				this.posAgentReceived.add(msg.getContent());
				msg = this.myAgent.receive(msgTemplate);

			}
			final ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
			msg2.setProtocol("SHARE");
					//2) Set the sender and the receiver(s)
			msg2.setSender(this.myAgent.getAID());
			msg2.addReceiver(this.myAgent.getAID());
			// UTILE POUR LA CHASSE, obtenir les positions des autres qui sont a cote de l agent
			SMPosition smsg=new SMPosition(((AbstractDedaleAgent) this.myAgent).getCurrentPosition(),this.posAgentReceived,null);
			try {					
				msg2.setContentObject(smsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//4) send the message
			((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
			
			if(((ExploreCoopAgent) this.myAgent).getFini()==1) {
				finished=true;
			}
		}else {
			block();
		}
		
		
	}

	@Override
	public boolean done() {
		if(finished) {
			System.out.println(this.myAgent.getLocalName()+" remove ReceiveNameBehaviour");
		}
		return finished;
	}



}
