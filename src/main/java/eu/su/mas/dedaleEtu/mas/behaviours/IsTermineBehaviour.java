package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class IsTermineBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	private MapRepresentation myMap;
	private HashMap<String,String>agents_pos;
	private HashMap<String,List<String>> myStench;
	private int exitvalue=1;					//1:continue;
												//2:fini chasse solo fsm(fini block ou passer chasse together fsm)
	
	private List<String>pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
	public IsTermineBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String> pos,HashMap<String,List<String>> myStench,List<String>pos_avant_next) {
		super(myagent);
		this.myMap=myMap;
		this.myStench=myStench;	
		this.myAgent=myagent;
		this.agents_pos=pos;
		this.pos_avant_next=pos_avant_next;
		
	}

	@Override
	public void action() {
		try {
			this.myAgent.doWait(250);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HashMap<String,Date>time=new HashMap<String,Date>();
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("SEND_ODEUR"));
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		if(msg!=null) {
			exitvalue=2;
			while(msg!=null) {
				SMPosition smg=null;
				try {
					smg = ((SMPosition) msg.getContentObject());
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//message ne pas a prendre en compte
				if(smg.getDate().before(((ExploreCoopAgent) this.myAgent).getmyTemps())) {
					continue;
				}
				String agentname=msg.getSender().getLocalName();
				if(!time.containsKey(agentname)) {
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
			System.out.println("MoveAloneBehaviour termine car communication");
			//a completer lancer movetogather
			
			
		}else {
			String posavant=this.pos_avant_next.get(0);
			String nextNode=this.pos_avant_next.get(1);
			if(posavant.equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition())) {
				if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
					
					//il y a wumpus
					ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
					msg2.setSender(this.myAgent.getAID());
					msg2.setProtocol("WUMPUS_IS_HERE_PROTOCOL");
					
					SMPosition contents=new SMPosition(nextNode,null,new java.util.Date());
					try {
						msg2.setContentObject(contents);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					for (String agentName : ((ExploreCoopAgent) this.myAgent).getAgentName()) {
						msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
					if(this.myMap.getnodeAdjacent(nextNode).size()==1){
						exitvalue=2;
						System.out.println("MoveAloneBehaviour termine car block wumpus");
						
					}
					
				}
			}
		}
		
		
	}
	public int onEnd() {return exitvalue ;}


}
