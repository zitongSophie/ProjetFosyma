package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SMEnd;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class AddBlockBehaviour extends SimpleBehaviour{
	
	private MapRepresentation myMap;
	private List<String> listReceivers;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  agentsInfo;
	private boolean finished=false;
	private List<String> Cg;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public AddBlockBehaviour(Agent a,MapRepresentation mymap, List<String> receivers) {
		super(a);
		this.myMap=mymap;
		this.listReceivers=receivers;	
		this.Cg=new ArrayList<String>();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;
	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		//System.out.println("entree ADD END "+this.myAgent.getLocalName());
		
		try {
			this.myAgent.doWait(150);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(this.myMap==null) block();
		
		
		final MessageTemplate msgT = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("ADD_ME_AS_BLOCK"));
		ACLMessage msg = this.myAgent.receive(msgT);
		while(msg!=null) {
			SMEnd sgreceived=null;
			try {
				sgreceived =  ((SMEnd) msg.getContentObject());
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//ajout du send comme agent fini
			if(! msg.getSender().getLocalName().equals(this.myAgent.getLocalName())) {
				((ExploreCoopAgent) this.myAgent).setFini(msg.getSender().getLocalName());
			}
			if(((ExploreCoopAgent) this.myAgent).isIdenticalList(((ExploreCoopAgent) this.myAgent).getAgentsListDF("coureur"),sgreceived.getListFini())) {// this.myAgent sait que le sender sait que tout le monde a fini
				System.out.println(this.myAgent.getLocalName()+" find "+msg.getSender().getLocalName()+" knew everything "+sgreceived.getListFini());
				if(!this.Cg.contains(msg.getSender().getLocalName())){// ajoute l agent dans ceux qui savent que tout le monde a fini
					this.Cg.add(msg.getSender().getLocalName());
				}
				if(!Cg.contains(this.myAgent.getLocalName())) {
					this.Cg.add(this.myAgent.getLocalName());
					((ExploreCoopAgent) this.myAgent).setfiniblock(sgreceived.getListFini());
					System.out.println(this.myAgent.getLocalName()+" knew everything now "+((ExploreCoopAgent) this.myAgent).getFiniExpl().toString());
				}
				if(((ExploreCoopAgent) this.myAgent).isIdenticalList(((ExploreCoopAgent) this.myAgent).getAgentsListDF("coureur"),this.Cg)) {// tout le monde sait que tout le monde a fini
					System.out.println("------------------------------------\n"+this.myAgent.getLocalName()+" knew everything now "+((ExploreCoopAgent) this.myAgent).getFiniExpl().toString());
					finished=true;
				}
				
			}
			msg = this.myAgent.receive(msgT);
		}
	}
	
	@Override
	public boolean done() {
		if(finished) {
			((ExploreCoopAgent) this.myAgent).setendblock();
			System.out.println(this.myAgent.getLocalName()+" remove AddEndBehaviour");
		}
		return finished;
	}



}
