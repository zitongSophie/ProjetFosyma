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
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class IsFinishedHuntAloneBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	private MapRepresentation myMap;
	private HashMap<String,String>agents_pos;
	private int exitvalue=1;					//1:continue;
												//2:fini chasse solo fsm(fini block ou passer chasse together fsm)
	private List<String>finiexpl;
	private List<String>pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
	public IsFinishedHuntAloneBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String> pos,List<String>pos_avant_next,List<String>finiexpl) {
		super(myagent);
		this.myMap=myMap;
		this.myAgent=myagent;
		this.agents_pos=pos;
		this.pos_avant_next=pos_avant_next;
		this.finiexpl=finiexpl;
		
	}

	@Override
	public void action() {
		if(this.myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		final MessageTemplate TemplatePos=MessageTemplate.MatchProtocol("POS_AND_ODEURS");
		final MessageTemplate TemplateMap=MessageTemplate.MatchProtocol("SHARE-TOPO");
		final MessageTemplate msgTemplate=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		HashMap<String,Date>time=new HashMap<String,Date>();
		if(msg!=null) {
			((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(2);
			exitvalue=2;
			while(msg!=null){
				if(!finiexpl.contains(msg.getSender().getLocalName())) {
					exitvalue=0;//sharemap
					//finiexpl.add(msg.getSender().getLocalName());
				}
				ACLMessage msgPos= this.myAgent.receive(TemplatePos);
				ACLMessage msgMap=this.myAgent.receive(TemplateMap);
				SMPosition smg=null;
				SerializableMessage smsg=null;
				if(msgPos!=null) {
					try {
						smg = ((SMPosition) msg.getContentObject());
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String agentname=msg.getSender().getLocalName();
					if(!time.containsKey(agentname)) {
						time.put(agentname,smg.getDate());
						this.agents_pos.put(agentname,smg.getpos());
						System.out.println(this.myAgent.getLocalName()+"=====isfinishedhunt agents_pos"+this.agents_pos+"======");
					}else {
						if(time.get(agentname).before(smg.getDate())) {
							time.put(agentname, smg.getDate());
							this.agents_pos.put(agentname,smg.getpos());
						}
					}
					if(time.keySet().size()==((ExploreCoopAgent) this.myAgent).getAgentName().size() ){
						break;
					}
					if((smg.getDate().getTime()+50>( (ExploreCoopAgent) this.myAgent).getmyTemps().getTime())){
						//System.out.println("message time "+smg.getDate().getTime()+"before mytime"+( (ExploreCoopAgent) this.myAgent).getmyTemps().getTime()+(smg.getDate().getTime()<=( (ExploreCoopAgent) this.myAgent).getmyTemps().getTime()));
						break;
					}
				}
				else {
					if(msgMap!=null) {
						try {
							smsg = ((SerializableMessage) msg.getContentObject());
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}

				msg=this.myAgent.receive(msgTemplate);
			}
			//System.out.println("HuntAloneBehaviour termine car communication");			
		}
		String posavant=this.pos_avant_next.get(0);
		String nextNode=this.pos_avant_next.get(1);
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(posavant.equals(myPosition)&& !this.agents_pos.containsValue(nextNode)) {
			Boolean isblock=false;
			if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
				isblock=true;
				List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
				for (String node:nodeAdj) {
					if(!node.equals(myPosition) && !this.agents_pos.containsValue(node)) {
						isblock=false;
					}
				}
			}
			if(isblock==true) {
				exitvalue=2;
				((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(2);
				System.out.println("HuntAloneBehaviour finished because block wumpus");
				
			}else {
				
				//il y a wumpus
				ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
				msg2.setSender(this.myAgent.getAID());
				msg2.setProtocol("WUMPUS_IS_HERE_PROTOCOL");
				Date time1=new java.util.Date();
				SMPosition contents=new SMPosition	(nextNode, time1, new Couple<Date,List<String>>(time1,((ExploreCoopAgent) this.myAgent).lstench()));
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
				
			}
		}

		
		
	}
	public int onEnd() {return exitvalue ;}


}
