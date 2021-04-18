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
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendOdeurBehaviour extends SimpleBehaviour{
	private static final long serialVersionUID = 8802075205635695208L;
	private boolean finished = false;
	private HashMap<String,String> agents_pos=new HashMap<String,String>();
	private HashMap<String,List<String>> myStench=new HashMap<String,List<String>>();
	private boolean isBlock;
	private Date myTemps=new java.util.Date(); //date de l'agent
	private List<String> myAgentToAsk;
	
	
	public SendOdeurBehaviour(final Agent myagent,Date d,List<String> ats) {
		super(myagent);
		myTemps=d;
		List<String> agentsNames=((ExploreCoopAgent) this.myAgent).getAgentsListDF("coureur");
		agentsNames.remove(this.myAgent.getLocalName());
		for (String s : agentsNames) {
			this.agents_pos.put(s,(String)"-1"); // ne contient pas sa propre position 
			this.myStench.put(s, new ArrayList<String>());
		}
		this.myStench.put(this.myAgent.getLocalName(),new ArrayList<String>()); // contient ses propres odeurs de la liste d observation
		this.myAgentToAsk=ats; 
	}
	
	@Override
	public void action() {
		boolean isMe=false;
		this.myTemps=new java.util.Date(); 
		List<String> lstench=null;
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		//this.agents_pos.put(this.myAgent.getLocalName(), ((AbstractDedaleAgent)this.myAgent).getCurrentPosition()); // mise a jour de la position de l agent
		for (Couple<String,List<Couple<Observation,Integer>>> cobs : lobs) {
			String pos=cobs.getLeft();
			for(Couple<Observation,Integer> isStench: cobs.getRight()) {
				if(isStench.getLeft().getName()=="Stench") {
					lstench=this.myStench.get(this.myAgent.getLocalName());
					lstench.add(pos);
					this.myStench.put(this.myAgent.getLocalName(), lstench);
					break;
				}
			}
			
		}
		if(!lstench.isEmpty()) {
			this.myAgent.addBehaviour(new ReceiveOdeurBehaviour(this.myAgent,this.myAgentToAsk,this.agents_pos,this.myStench,this.myTemps)); // creation de ce behaviour
			ACLMessage msg ;
			msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SEND-ODEUR");
			msg.setSender(this.myAgent.getAID());
			for (String s: this.agents_pos.keySet()) {
				msg.addReceiver(new AID(s, AID.ISLOCALNAME)); 
			}
			SMPosition contents=new SMPosition(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),lstench,String.valueOf(new java.util.Date().getTime()));
			try {
				msg.setContentObject(contents);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			((AbstractDedaleAgent)  this.myAgent).sendMessage(msg);
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}


}
