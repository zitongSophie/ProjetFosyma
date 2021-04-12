package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class CheckWinBehaviour extends SimpleBehaviour{
	

	private boolean finished=false;
	private boolean isBlock;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public CheckWinBehaviour(Agent a,boolean f) {
		super(a);
		this.isBlock=f;

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	public void action() {
		final MessageTemplate msgT = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("SHARE"));
		ACLMessage msgYes = this.myAgent.receive(msgT);
		
		

		/*
		List<String> finiExpl=((ExploreCoopAgent) this.myAgent).getAgentsListDF("finiExplo") ;
		if(!finiExpl.isEmpty()) {
			if(finiExpl.contains(this.getAgent().getLocalName())) {
				finiExpl.remove(this.myAgent.getLocalName());
				if(!finiExpl.isEmpty()) {
					if(((ExploreCoopAgent) this.myAgent).isIdenticalList (this.agentsInfo.keySet(),finiExpl )){
						this.finished=true;
					}	
				}
			}
		}*/
	}

	@Override
	public boolean done() {
		if(finished) {
			System.out.println("CheckWinBehaviour is finished of "+this.myAgent.getLocalName());
		}
		return finished;
	}



}
