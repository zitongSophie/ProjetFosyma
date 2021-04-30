package eu.su.mas.dedaleEtu.mas.behaviours;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class isBlockBeforeEndExploBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private boolean explofinished = false;
	private HashMap<String,String> agents_pos;
	private String mynnblock;
	private MapRepresentation mymap;
	
	public isBlockBeforeEndExploBehaviour(final Agent myagent,HashMap<String,String> ap,boolean fini,String nextnodeblock,MapRepresentation map) {
		super(myagent);
		this.agents_pos=ap;
		this.explofinished=fini;
		this.mynnblock=nextnodeblock;
		this.mymap=map;
	}
	
	@Override
	public void action() {
		Set<String> names=new HashSet<String>();
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("NOT_GOLEM"));	
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		while(msg != null) {
			if(!msg.getSender().getLocalName().equals(this.myAgent.getLocalName())) {
				this.agents_pos.put(msg.getSender().getLocalName(), msg.getContent());
				//System.out.println(this.myAgent.getLocalName()+" received a message not golem from "+msg.getSender().getLocalName());
				
				names.add(msg.getSender().getLocalName());	
			}
			msg = this.myAgent.receive(msgTemplate);
		}
		List<String> lnodeAdj;
		if(names.isEmpty() && this.mymap!=null) {//aucun agent dans la position mynnblock
			lnodeAdj=((ExploreCoopAgent) this.myAgent).getNodeAdjacent(this.mynnblock);
			if(lnodeAdj == null ||lnodeAdj.isEmpty()) {
				System.out.println(this.myAgent.getLocalName()+" block the golem before finished exploring the map");
				explofinished=true;
				//this.myAgent.addBehaviour(new AddBlockBehaviour(this.myAgent,this.mymap));
			}
			else {
				List<String> agentproche=new ArrayList<String>();
				for(String s:names) {
					agentproche.add(this.agents_pos.get(s)); //position des agents proches
				}
				boolean isblock=true;
				for( String s:lnodeAdj) {
					if(!agentproche.contains(s)) {
						isblock=false;
						break;
					}
				}
				
				if(isblock) {
					((ExploreCoopAgent) this.myAgent).setEnd();
					this.myAgent.addBehaviour(new TestSendBlockBehaviour(myAgent, agentproche));
					this.myAgent.addBehaviour(new TestAddBlockBehaviour(myAgent, mymap));
					System.out.println(this.myAgent.getLocalName()+"\t"+" \n\n\n- block wumpus before finished explore.\n\n\n");
					explofinished=true;
				}
			}
		}
		//System.out.println(this.myAgent.getLocalName()+" isBlockBehaviour "+names.toString()+" "+agentsToContact.toString());
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		if(explofinished) {
			System.out.println(this.myAgent.getLocalName()+" remove isBlockBeforeEndExploBehaviour");
		}
		return explofinished;
	}
}
