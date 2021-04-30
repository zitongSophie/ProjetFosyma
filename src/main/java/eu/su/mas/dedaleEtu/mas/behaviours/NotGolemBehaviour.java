package eu.su.mas.dedaleEtu.mas.behaviours;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NotGolemBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private boolean finished = false;
	private List<String> names;
	private HashMap<String,String> agents_pos;
	
	
	public NotGolemBehaviour(final Agent myagent,HashMap<String,String> ap,boolean fini,List<String> ata) {
		super(myagent);
		this.names=ata;
		names=new ArrayList<String>();
		this.agents_pos=ap;
		this.finished=fini;
	}
	
	@Override
	public void action() {
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.names=new ArrayList<String>();
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("IS_GOLEM_PROTOCOL"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		while(msg != null) {
			if(!msg.getSender().getLocalName().equals(this.myAgent.getLocalName())) {
				//System.out.println(this.myAgent.getLocalName()+" received a message is Golem from "+msg.getSender().getLocalName());
				this.agents_pos.put(msg.getSender().getLocalName(), msg.getContent());
				names.add(msg.getSender().getLocalName());	
			
			}
			msg = this.myAgent.receive(msgTemplate);
		}
		//send


		final ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setProtocol("NOT_GOLEM");
		//2) Set the sender and the receiver(s)
		msg2.setSender(this.myAgent.getAID());
		if(!names.isEmpty()) {
			for(String receiver : names) {
				msg2.addReceiver(new AID(receiver,AID.ISLOCALNAME)); 
			}
		}
		msg2.addReceiver(this.myAgent.getAID());
		msg2.setContent(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
		//4) send the message
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
		block();
		
	}

	@Override
	public boolean done() {
		if(finished) {
			System.out.println(this.myAgent.getLocalName()+" remove NotGolemBehaviour");
		}
		return finished;
	}


}
