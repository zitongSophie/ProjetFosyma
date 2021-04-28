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

public class ReceiveBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = -8577400242202965285L;
	private boolean finished= false;
	private List<String> agentproche;
	private Date myTemps;
	private HashMap<String,String> agents_pos;
	private HashMap<String,List<String>> myStench;
	
	
	public ReceiveBehaviour(final Agent myagent,List<String> agentproche,HashMap<String,String> a_pos,HashMap<String,List<String>> mystench,Date tps) {
		super(myagent);
		this.agentproche=agentproche;
		this.myStench=mystench;
		this.myStench=new HashMap<String,List<String>>();
		this.agents_pos=a_pos;
		this.agents_pos=new HashMap<String,String>();
		this.myTemps=tps;
		
	}

	@Override
	public void action() {

		try {
			this.myAgent.doWait(50);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<String>ata=new ArrayList<String>();
		HashMap<String,Date>time=new HashMap<String,Date>();
		
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("SEND-ODEUR"));
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		if(msg!=null) {
			System.out. println ( "mytime"+this.myTemps+"-------stench "+this.myStench+this.myAgent.getLocalName()+" receivebehaviour\n--------" ) ;
			while(msg!=null) {
				SMPosition smg=null;
				try {
					smg = ((SMPosition) msg.getContentObject());
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//message ne pas a prendre en compte
				if(smg.getDate().before(myTemps)) {
					continue;
				}
				String agentname=msg.getSender().getLocalName();
				if(!ata.contains(agentname)) {
					ata.add(agentname);
					time.put(agentname,smg.getDate());
				}else {
					if(time.get(agentname).before(smg.getDate())) {
						time.put(agentname, smg.getDate());
						this.agents_pos.put(agentname,smg.getpos());
						this.myStench.put(agentname,smg.getPredicPosGolem());
					}
				}
				msg = this.myAgent.receive(msgTemplate);
			}
			this.agentproche=ata;
			System.out. println ( "mytime"+this.myTemps+" agentproche "+this.agentproche+"-------stench "+this.myStench+this.myAgent.getLocalName()+" receivebehaviour\n--------" ) ;
		}
		
	}

}
