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

public class CheckPosBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private boolean finished = false;
	private List<AID> names;
	//private HashMap<String,String> agents_pos;
	private String myAgentNextNode;
	private boolean isBlock;
	
	public CheckPosBehaviour(final Agent myagent,boolean f,String nextN) {
		super(myagent);
		names=new ArrayList<AID>();
		this.myAgentNextNode=nextN;	
		isBlock=f;
	}
	
	@Override
	public void action() {
		boolean isMe=false;
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("CHECK_NOBODY_PROTOCOL"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);	
		
		ACLMessage msg2 ;
		while(msg!=null) {
			//si le sender n est pas moi meme -> repondre
			if(msg.getSender().getLocalName()!=this.myAgent.getLocalName()) {//les autres agents demandes si ils sont bloqués par moi
				msg2 = new ACLMessage(ACLMessage.INFORM);
				msg2.setProtocol("ME_PROTOCOL"); //reponse au check
				msg2.setSender(this.myAgent.getAID());
				msg2.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
				msg2.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME)); 
				((AbstractDedaleAgent)  this.myAgent).sendMessage(msg2);
				msg=this.myAgent.receive(msgTemplate);
			}
			else {
				isMe=true; //je demande a verifier les positions
				this.myAgent.addBehaviour(new CheckWinBehaviour(this.myAgent,isBlock));
			}
		}
		
		block();
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}


}
