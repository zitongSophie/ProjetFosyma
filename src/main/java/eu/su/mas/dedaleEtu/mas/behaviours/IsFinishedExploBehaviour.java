package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class IsFinishedExploBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158228049276029011L;
	
	
	private MapRepresentation myMap;
	private HashMap<String,String>agents_pos;
	private int exitvalue=1;					//1:continue;
												//2:fini chasse solo fsm(fini block ou passer chasse together fsm)
	private List<String>lstench;
	private List<String>pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
	public IsFinishedExploBehaviour(final Agent myagent, MapRepresentation myMap,HashMap<String,String>agents_pos,List<String>pos_avant_next,List<String>lstench) {
		super(myagent);
		this.myMap=myMap;
		this.agents_pos=agents_pos;
		this.pos_avant_next=pos_avant_next;
		this.lstench=lstench;
	}

	@Override
	public void action() {
		if(myMap==null) {
			this.myMap=((ExploreCoopAgent) this.myAgent).getMap();
		}
		if(!myMap.hasOpenNode()) {
			if(!this.agents_pos.isEmpty()) {
				((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(1);
				
			}
			exitvalue=2;
			System.out.println(this.myAgent.getLocalName()+"\t"+" \n\n\n- Exploration successufully done, behaviour removed.\n\n\n");
			
			
		}
		String posavant=this.pos_avant_next.get(0);
		String nextNode=this.pos_avant_next.get(1);
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(myPosition!=null) {
			List<String>opennode=this.myMap.getOpenNodes();
			if(posavant.equals(myPosition)&& !this.agents_pos.containsValue(nextNode)) {
				if(opennode.size()==1 && opennode.contains(nextNode)) {
					exitvalue=2;
					((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(2);//go to allfinished
					System.out.println(this.myAgent.getLocalName()+"Exploration finished and  to  block wumpus : only single opennode"+" pos_wumpus "+nextNode+" agents_pos "+agents_pos);
				}else {
					//if it is a node we explored,see if all the nodes adjacent are occupied by agents
					//other can be move ,not sure to block,need to check
					if(opennode.size()>1 && !opennode.contains(nextNode)) {
						List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
						Boolean isblock=true;
						for (String node:nodeAdj) {
							if(!node.equals(myPosition) && !this.agents_pos.containsValue(node)) {
								isblock=false;
								break;
							}
						}
						if(isblock=true) {
							exitvalue=2;
							((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(2);
							ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
							msg2.setSender(this.myAgent.getAID());
							msg2.setProtocol("WUMPUS_IS_HERE");
							((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);	
							msg2.setContent(nextNode);
							for (String agentName : ((ExploreCoopAgent) this.myAgent).getAgentName()) {
								msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
							}
							System.out.println(this.myAgent.getLocalName()+"Exploration interrupt: may block a wumpus with other agents "+" pos_wumpus "+nextNode+" agents_pos "+agents_pos);
						
						}
					}
				}
			}
		}
		if(((ExploreCoopAgent) this.myAgent).lstench().isEmpty()) {
			if(exitvalue==2) {
				exitvalue=1;
			}
		}


		
		
		
		
	}
	public int onEnd() {return exitvalue;}


}
