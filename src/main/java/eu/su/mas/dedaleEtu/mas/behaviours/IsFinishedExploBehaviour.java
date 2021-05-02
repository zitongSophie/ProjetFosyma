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
	private int exitvalue=1;//1:					
	private List<String>lstench;
	private List<String>pos_avant_next;
	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												
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
			exitvalue=3;//fini
			System.out.println(this.myAgent.getLocalName()+"\t"+" \n\n\n- Exploration successufully done, behaviour removed.\n\n\n");
			
			
		}
		String posavant=this.pos_avant_next.get(0);
		String nextNode=this.pos_avant_next.get(1);
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(posavant.equals(myPosition)&& !this.agents_pos.containsValue(nextNode)&& !((ExploreCoopAgent) this.myAgent).lstench().isEmpty()) {
			Boolean isblock=true;
			List<String>nodeAdj=this.myMap.getnodeAdjacent(nextNode);
			for (String node:nodeAdj) {
				if(!node.equals(myPosition) && !this.agents_pos.containsValue(node)) {
					isblock=false;
					break;
				}
			}
			
			if(isblock==true) {
				exitvalue=2;
				((ExploreCoopAgent) this.myAgent).set_fsm_exitvalue(3);
				System.out.println("EXPLORATIONBehaviour may block wumpus");
				ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
				msg2.setSender(this.myAgent.getAID());
				msg2.setProtocol("WUMPUS_IS_HERE");
				try {
					msg2.setContentObject(nextNode);
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
		
	public int onEnd() {return exitvalue;}


}
