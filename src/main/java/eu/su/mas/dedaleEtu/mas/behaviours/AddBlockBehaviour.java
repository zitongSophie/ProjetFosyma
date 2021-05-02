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
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class AddBlockBehaviour extends OneShotBehaviour{
	
	private MapRepresentation myMap;
	private List<String> CgChasse;
	private int exitvalue=1;//0:j'ai pas fini: entre dans ce state seulement quand moi meme est fini,mais pas fini pour tout le monde;tous le monde est fini:2

	public AddBlockBehaviour(Agent a,List<String> CgChasse) {
		super(a);
		this.CgChasse=CgChasse;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;
	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		//System.out.println("entree ADD END "+this.myAgent.getLocalName());

		
		final MessageTemplate msgT = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("ADD_ME_AS_BLOCK"));
		ACLMessage msg = this.myAgent.receive(msgT);
		if(msg!=null) {
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
				if(!this.CgChasse.contains(msg.getSender().getLocalName())){// ajoute l agent dans ceux qui savent que tout le monde a fini
					this.CgChasse.add(msg.getSender().getLocalName());
				}
				if(!CgChasse.contains(this.myAgent.getLocalName())) {
					this.CgChasse.add(this.myAgent.getLocalName());
					((ExploreCoopAgent) this.myAgent).setfiniblock(sgreceived.getListFini());
					System.out.println(this.myAgent.getLocalName()+" knew block now "+((ExploreCoopAgent) this.myAgent).getFiniExpl().toString());
				}
				if(((ExploreCoopAgent) this.myAgent).isIdenticalList(((ExploreCoopAgent) this.myAgent).getAgentsListDF("coureur"),this.CgChasse)) {// tout le monde sait que tout le monde a fini
					System.out.println("------------------------------------\n"+this.myAgent.getLocalName()+" knew block now "+((ExploreCoopAgent) this.myAgent).getFiniExpl().toString());
					exitvalue=2;
				}
				
			}
			msg = this.myAgent.receive(msgT);
		}
	}
	public int onEnd() {return exitvalue ;}
	




}
