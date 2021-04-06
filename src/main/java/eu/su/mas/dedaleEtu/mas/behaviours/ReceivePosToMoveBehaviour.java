package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ReceivePosToMoveBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;


	public ReceivePosToMoveBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
	}

	@Override
	public void action() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE-PREDICT-POS-GOLEM"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msg=this.myAgent.receive(msgTemplate);
		if (msg!=null){
			String nextNode=msg.getSender().getLocalName();
			if(nextNode!="-1") {
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			else {
				//1°Create the message
				ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
				msg.setProtocol("FIND_SOLUTION_PROTOCOL");
				msg.setSender(this.myAgent.getAID());
				try {
					for (String s : ((SMPosition) msg2.getContentObject()).getAgentsAgue() ){
						msg.addReceiver(new AID(s, AID.ISLOCALNAME));  
					}
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg.setContent("ENCHERE");
				//msg.setContent(this.myAgent.getLocalName());
		        System.out.println(this.myAgent.getLocalName()+" send a message");
				block();
			}
		}
		else {
			block();
		}

	}

	@Override
	public boolean done() {
		return finished;
	}

}
