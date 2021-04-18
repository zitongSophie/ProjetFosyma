package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveOdeurBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = -8577400242202965285L;
	private boolean finished= false;
	private List<String> myAgentsToAsk;
	private Date myTemps=new java.util.Date();
	private HashMap<String,String> agents_pos=new HashMap<String,String>();
	private HashMap<String,List<String>> myStench=new HashMap<String,List<String>>();
	
	public ReceiveOdeurBehaviour(final Agent myagent,List<String> toContact,HashMap<String,String> a_pos,HashMap<String,List<String>> hmstench,Date tps) {
		super(myagent);
		this.myAgentsToAsk=toContact;
		this.myAgentsToAsk=new ArrayList<String>();
		this.myStench=hmstench;
		this.agents_pos=a_pos;
		this.myTemps=tps;
		
	}

	@Override
	public void action() {
		//1) Attente avant de recevoir les messages
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("SEND-ODEUR"));
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		while(msg!=null) {
			SMPosition smg=null;
			try {
				smg = ((SMPosition) msg.getContentObject());
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//message  a ne pas prendre en compte
			if(Integer.valueOf(smg.getDate()) < Integer.valueOf((int) this.myTemps.getTime())) {
				continue;
			}
			else{//message a prendre en compte
				this.myAgentsToAsk.add(msg.getSender().getLocalName());	//get all the agents who is here
				this.agents_pos.put(msg.getSender().getLocalName(),smg.getpos());
				List<String> lstench=this.myStench.get(msg.getSender().getLocalName());
				if(lstench.isEmpty()) {
					this.myStench.put(msg.getSender().getLocalName(),smg.getPredicPosGolem());
				}
			}
			msg = this.myAgent.receive(msgTemplate);
		}
		
		final ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
		msg2.setProtocol("CHOIX-MOUVEMENT");
				//2) envoie a soi meme un message pour choisir le move
		msg2.setSender(this.myAgent.getAID());
		msg2.addReceiver(this.myAgent.getAID());
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
	}
}
