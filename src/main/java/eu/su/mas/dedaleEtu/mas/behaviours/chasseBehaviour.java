package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Node;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class chasseBehaviour extends SimpleBehaviour {
	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>  myInfo;
	private List<String> myAgentToShareMap; //agents to share the map
	private List<String> myAgentToAsk;
	private HashMap<String,String>agents_pos;
	private List<String>PosOdeurs=new ArrayList<String>();

	/**
	 * 
	 * @param myagent
	 * @param myMap known map of the world the agent is living in
	 * @param agentNames name of the agents to share the map with
	 */																												//add attribute
		public chasseBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> info,List<String> ata,List<String> atsm,HashMap<String,String> pos) {
			super(myagent);
			this.myMap=myMap;
			this.myInfo=info;	
			this.myAgentToAsk=ata;
			this.myAgentToAsk=((ExploreCoopAgent) this.myAgent).setAgentToAsk();
			this.myAgent=myagent;
			this.myAgentToShareMap=atsm;
			this.myAgentToShareMap=((ExploreCoopAgent) this.myAgent).setAgentToAsk();
			this.agents_pos=pos;
		}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//cas normale :chercher aleatoire dans le map
			List<String>caseproche=this.myMap.getnodeAdjacent(myPosition) ;
			Integer r=(int) Math.random() * ( caseproche.size()  );
			String nextNode=caseproche.get(r);
			
			
			//mettre a jour l'odeurs obseve
			List<String>currentPosOdeurs=new ArrayList<String>();
			for (Couple<String,List<Couple<Observation,Integer>>> c:lobs) {
				if(c.getRight().size()!=0) {
					currentPosOdeurs.add(c.getLeft());
				}
			}
			if(currentPosOdeurs.size()!=0) {
				this.PosOdeurs.clear();
				for(String s :currentPosOdeurs) {
					this.PosOdeurs.add(s);
				}
			}
			if(this.PosOdeurs.size()!=0) {
				r=(int) Math.random() * ( this.PosOdeurs.size()  );
				this.myMap.getShortestPath(myPosition, this.PosOdeurs.get(r)).get(0);
			}
			else {
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("WHO_IS_HERE_PROTOCOL");
				msg.setContent(myPosition);
				for (String agentName : this.myAgentToAsk) {
					msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
			
			
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
		}
	}
		


	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	

}
