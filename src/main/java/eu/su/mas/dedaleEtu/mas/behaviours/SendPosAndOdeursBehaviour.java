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

public class SendPosAndOdeursBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = 8802075205635695208L;
	private Couple<Date,List<String>> list_recent_odeurs;
	
	public SendPosAndOdeursBehaviour(Agent myAgent,Couple<Date,List<String>> list_recent_odeurs) {
		super(myAgent);
		this.list_recent_odeurs=list_recent_odeurs;
	}
	@Override
	public void action() {
		List<String>lstench=((ExploreCoopAgent) this.myAgent).lstench();
		ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
		msg2.setSender(this.myAgent.getAID());
		msg2.setProtocol("POS_AND_ODEURS");
		for (String agentName : ((ExploreCoopAgent) this.myAgent).getAgentName()) {
			msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
		if(!lstench.isEmpty()) {
			list_recent_odeurs=new Couple<Date,List<String>>(((ExploreCoopAgent) this.myAgent).getmyTemps(),lstench );
		}
		SMPosition contents=new SMPosition(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), new java.util.Date(),this.list_recent_odeurs);
		
		try {
			msg2.setContentObject(contents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
		//System.out. println ( "======================\n"+this.myAgent.getLocalName()+"send POS_AND_ODEURS time:"+contents.getDate()+"stench "+lstench+""+"list odeurs "+list_recent_odeurs+" \nsendbehaviour======================\n" ) ;
	}




}
