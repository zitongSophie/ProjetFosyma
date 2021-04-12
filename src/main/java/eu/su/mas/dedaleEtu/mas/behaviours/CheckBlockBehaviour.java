package eu.su.mas.dedaleEtu.mas.behaviours;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckBlockBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private boolean finished = false;
	//private HashMap<String,String> agents_pos;
	private boolean isBlock;
	
	public CheckBlockBehaviour(final Agent myagent) {
		super(myagent);
	}
	
	@Override
	public void action() {
		boolean isMe=false;
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("CHECK_BLOCK_PROTOCOL"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);	
		
		ACLMessage msg2 ;
		msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setProtocol("ME_PROTOCOL");
		msg2.setSender(this.myAgent.getAID());
		msg2.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME)); 
			//si le sender n est pas moi meme -> repondre
		if(msg.getSender().getLocalName()==this.myAgent.getLocalName()) {//les autres agents demandes si ils sont bloqués par moi
			List<String> agentBlock=((ExploreCoopAgent) this.myAgent).getAgentsListDF("block");
			if(agentBlock.isEmpty()) {
			 //reponse au check
				msg2.setContent("no");//nobody block
			}
			else {
				if(agentBlock.contains(this.myAgent.getLocalName())) {
					agentBlock.remove(this.myAgent.getLocalName());
					if(agentBlock.isEmpty()) {
						msg2.setContent("no");
					}
					else {
						msg2.setContent("yes");
					}
				}
			}
			((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
		}
		else {
			block();
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}


}
