package eu.su.mas.dedaleEtu.mas.behaviours;



import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceivePosAndOdeursBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private HashMap<String,String> agents_pos;
	private Couple<Date,List<String>> list_recent_odeurs;
	
	public ReceivePosAndOdeursBehaviour(final Agent myagent,HashMap<String,String> a_pos, Couple<Date,List<String>>list_recent_odeurs) {
		super(myagent);
		//this.agentproche=agentproche;
		this.agents_pos=a_pos;
		this.list_recent_odeurs=list_recent_odeurs;
	}
	
	@Override
	public void action() {
		this.agents_pos.clear();
		//1) receive the SendWhoIsHere message
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("POS_AND_ODEURS"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		HashMap<String,Date>time=new HashMap<String,Date>();
		while(msg!=null) {
			SMPosition smg=null;
			try {
				smg = ((SMPosition) msg.getContentObject());
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//message ne pas a prendre en compte
			if((smg.getDate().getTime()+50>( (ExploreCoopAgent) this.myAgent).getmyTemps().getTime())){
				//System.out.println("message time "+smg.getDate().getTime()+"before mytime"+( (ExploreCoopAgent) this.myAgent).getmyTemps().getTime()+(smg.getDate().getTime()<=( (ExploreCoopAgent) this.myAgent).getmyTemps().getTime()));
				break;
			}
			if(time.keySet().size()==((ExploreCoopAgent) this.myAgent).getAgentName().size() ){
				break;
			}
			String agentname=msg.getSender().getLocalName();
			if(!time.containsKey(agentname)) {
				time.put(agentname,smg.getDate());
				this.agents_pos.put(agentname,smg.getpos());
				System.out.println(this.myAgent.getLocalName()+" ReceivePosAndOdeurs 1: "+this.agents_pos);
				if(!(smg.getPredicPosGolem()==null)) {
					if(this.list_recent_odeurs.getRight().isEmpty()) {
						this.list_recent_odeurs=smg.getPredicPosGolem();
					}else {
						if(this.list_recent_odeurs.getLeft().before(smg.getPredicPosGolem().getLeft())) {
							this.list_recent_odeurs=smg.getPredicPosGolem();
						}
					}
				}
			}else {
				if(time.get(agentname).before(smg.getDate())) {
					time.put(agentname, smg.getDate());
					this.agents_pos.put(agentname,smg.getpos());
					System.out.println(this.myAgent+" ReceivePosAndOdeurs 2: "+this.agents_pos);
					if(!(smg.getPredicPosGolem()==null)) {
						if(this.list_recent_odeurs==null) {
							this.list_recent_odeurs=smg.getPredicPosGolem();
						}else {
							if(this.list_recent_odeurs.getLeft().before(smg.getPredicPosGolem().getLeft())) {
								this.list_recent_odeurs=smg.getPredicPosGolem();
							}
						}
					}
				}
			}
			msg = this.myAgent.receive(msgTemplate);
		}
	}
		//((ExploreCoopAgent) this.myAgent).set_agents_pos(agents_pos);
		//System.out. println ( "======================\nmytime"+((ExploreCoopAgent) this.myAgent).getmyTemps()+" agentproche "+this.agents_pos+"-------stench "+this.list_recent_odeurs+this.myAgent.getLocalName()+" \n\nreceiveoposdeurbehaviour exploration\n======================\n" ) 
}