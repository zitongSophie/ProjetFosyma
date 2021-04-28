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
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = 8802075205635695208L;
	private HashMap<String,String> agents_pos=new HashMap<String,String>();
	private HashMap<String,List<String>> myStench;
	private List<String>agentproche;
	private List<String> agentsNames;
	
	public SendBehaviour(final Agent myagent,List<String>agentproche,HashMap<String,List<String>>stench) {
		super(myagent);
		this.agentproche=agentproche;
		this.agentsNames=new ArrayList<String>();
		this.agentsNames.add("1stAgent");
		this.agentsNames.add("2ndAgent");
		this.agentsNames.remove(this.myAgent.getLocalName());
		this.myStench=stench;
	}
	
	@Override
	public void action() {
		List<String>lstench=((ExploreCoopAgent) this.myAgent).lstench();
		ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
		msg2.setSender(this.myAgent.getAID());
		msg2.setProtocol("SEND_ODEUR");
		for (String agentName : this.agentsNames) {
			msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
		SMPosition contents=new SMPosition(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),lstench,new java.util.Date());
		try {
			msg2.setContentObject(contents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
		//System.out. println ( "-------"+contents.getDate()+"stench "+lstench+this.myStench+this.myAgent.getLocalName()+" sendbehaviour\n--------" ) ;
	}




}