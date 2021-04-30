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

public class MeBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private boolean finished = false;
	private List<AID> names;
	private HashMap<String,String> agents_pos;
	
	public MeBehaviour(final Agent myagent,HashMap<String,String> ap) {
		super(myagent);
		names=new ArrayList<AID>();
		this.agents_pos=ap;
	}
	
	@Override
	public void action() {
		System.out.println(this.myAgent.getLocalName()+" MeBehaviour");
		//1) receive the SendWhoIsHere message
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("WHO_IS_HERE_PROTOCOL"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		if(msg!=null) {
			System.out.println("SendMessage behaviour from agent: "+ msg.getSender().getLocalName()+msg.getContent());
		}else {
			block();
		}
		while(msg != null) {
			this.agents_pos.put(msg.getSender().getLocalName(), msg.getContent());
				
			names.add(msg.getSender());	
			//msg = this.myAgent.receive(msgTemplate);
			//System.out.println("SendMessage behaviour from agent: "+ msg.getSender().getLocalName()+this.myAgent.getLocalName());
		}
		//send
		final ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setProtocol("ME_PROTOCOL");
				//2) Set the sender and the receiver(s)
		msg2.setSender(this.myAgent.getAID());
		for(AID receiver : names) {
			msg2.addReceiver(receiver); 
		}//4) send the message
		String pos=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		msg2.setContent(pos);
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
	}

	@Override
	public boolean done() {
		if(finished) {
			System.out.println(this.myAgent.getLocalName()+" remove MeBehaviour");
		}
		return finished;
	}


}
