package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceivePosWumpusBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = -8577400242202965285L;
	private List<String>check;
	private int exitvalue=1;
	
	public ReceivePosWumpusBehaviour(final Agent myagent) {
		super(myagent);
		
	}

	@Override
	public void action() {		
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("WUMPUS_IS_HERE"));
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		if(msg!=null) {
			if(!msg.getContent().equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition())) {
				exitvalue=2;
				ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
				msg2.setSender(this.myAgent.getAID());
				msg2.setProtocol("OK_PROTOCOL");
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);	
				msg2.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
				msg2.addReceiver(new AID(msg.getSender().getLocalName(),AID.ISLOCALNAME));
				((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(2);
			}
		}
	}
	@Override
	public int onEnd() {
		return exitvalue;
	}


}
