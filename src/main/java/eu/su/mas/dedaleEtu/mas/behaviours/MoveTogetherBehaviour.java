package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SMAllPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import org.graphstream.graph.Node;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class MoveTogetherBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -3494766264924947341L;

	private boolean finished = false;

	private MapRepresentation myMap;
	private HashMap<String,List<String>> infoOdeurAgent; // de tous les agents de l'environnement
	private String myPosition; 
	private String myNexNode; // pour verifier quand un agent echange avec myAgent que ce n est pas la position d'un autre agent
	
	
	public MoveTogetherBehaviour(final Agent myAgent, MapRepresentation myMap) {
		super(myAgent);
		this.myMap=myMap;	
		this.infoOdeurAgent=new HashMap<String,List<String>>();
		this.myPosition=null;
	}

	@Override
	public void action() {
		//0) Retrieve the current position
		String Position=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if(this.myPosition==null) {
			this.myPosition=Position;
		}
		try {
			this.myAgent.doWait(250);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		final MessageTemplate msgT = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("POS-MOVE-TOGETHER"));
		ACLMessage msg = this.myAgent.receive(msgT);
		
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//obliger de recevoir un message

		boolean doitcontinuer=true;
		SMAllPosition msgContent=null;
		while(doitcontinuer ) {
			if (msg==null) {
				msg = this.myAgent.receive(msgT); //doit au moins recevoir un message d un agent
			}
			else{
				try {
					msgContent = (SMAllPosition) msg.getContentObject();
					if(((ExploreCoopAgent) this.myAgent).getmyTemps().after(msgContent.getdate())) {
						doitcontinuer=false;
					}
					
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		this.myPosition=Position;
		this.myNexNode=msgContent.getListFini().get(this.myAgent.getLocalName());
		((ExploreCoopAgent) this.myAgent).setmyTemps();
		((AbstractDedaleAgent)this.myAgent).moveTo(this.myNexNode);
		
	}	
		
	
}
