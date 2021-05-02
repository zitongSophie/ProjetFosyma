package eu.su.mas.dedaleEtu.mas.behaviours;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graphstream.graph.Edge;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SMAllPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.SMPosition;
import jade.core.AID;
//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

//sert pour choisir la position a aller pour les agents en communication
public class ReceiveInfoTogetherBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 8802075205635695208L;
	private HashMap<String,String> agentpos;
	private Couple<Date,List<String>> list_recent_odeurs;
	private HashMap<String,List<String>> infoOdeurAgent;
	private MapRepresentation mymap;
	
	public ReceiveInfoTogetherBehaviour(final Agent myagent,MapRepresentation map,HashMap<String,String> a_pos, Couple<Date,List<String>>list_recent_odeurs) {
		super(myagent);
		this.agentpos=new HashMap<String,String>();//contient lui meme
		this.mymap=map;
		this.list_recent_odeurs=list_recent_odeurs;
		this.infoOdeurAgent=new HashMap<String,List<String>>();//contient this.myAgent
	}
	
	@Override
	public void action() {
		
		//1) receive the SendWhoIsHere message
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("POS_AND_ODEURS"));	
		
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		HashMap<String,Date>time=new HashMap<String,Date>();
		try {
			this.myAgent.doWait(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//obliger de recevoir un message
		Integer nb=0;
		while((msg==null || msg.getSender().getLocalName().equals(this.myAgent.getLocalName())) && nb<10) {
			msg = this.myAgent.receive(msgTemplate); //doit au moins recevoir un message d un agent
			nb+=1;
		}
		while(msg!=null ) {
			SMPosition smg=null;
			try {
				smg = ((SMPosition) msg.getContentObject());
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String agentname=msg.getSender().getLocalName();
			if(((ExploreCoopAgent) this.myAgent).getmyTemps().after(smg.getDate())) {//message utile
				time.put(agentname, smg.getDate());
				this.agentpos.put(agentname,smg.getpos());
				if(!(smg.getPredicPosGolem().getRight().isEmpty())) {
					 //ajout des odeurs de chaque agent s'ils en ont
					this.infoOdeurAgent.put(agentname, smg.getPredicPosGolem().getRight());
				}
			}
			
			msg = this.myAgent.receive(msgTemplate);
		}
		if(!this.agentpos.keySet().isEmpty()) {
			this.agentpos.put(this.myAgent.getLocalName(), ((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
			this.infoOdeurAgent.put(this.myAgent.getLocalName(), this.list_recent_odeurs.getRight());
			HashMap<String,String> nextAllpos=this.getPositionForEachAgent(infoOdeurAgent, agentpos);
			SMAllPosition mycontent=new SMAllPosition(new java.util.Date(),nextAllpos);
			ACLMessage msg2=new ACLMessage(ACLMessage.INFORM);
			msg2.setProtocol("POS-MOVE-TOGETHER");
			msg2.setSender(this.myAgent.getAID());
			try {
				msg2.setContentObject(mycontent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (String agentName :this.infoOdeurAgent.keySet()) {
				msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
		}
		else {
			//il faut moveAlone
		}
	}
	//dans posAgent contient  la position de l'agent this.myAgent
	public HashMap<String,String> getPositionForEachAgent(HashMap<String,List<String>> info,HashMap<String,String> posAgent) {
		HashMap<String,Integer> posGolem=new HashMap<String,Integer>(); // positions ou il y a des odeurs
		HashMap<String,String> nextPosAgent=new HashMap<String,String>(); //positions a aller pour les agents en communications
		for (String name : info.keySet()) {// pour chaque posodeur de chaque agent
			for(String odeur : info.get(name)) {//ne pas ajouter les odeurs qui sont a leur position
				if(!this.agentpos.values().contains(odeur)) {
					if(posGolem.containsKey(odeur)) {
						posGolem.put(odeur, posGolem.get(name)+1);
					}
					else {
						posGolem.put(odeur, 1); //calcul de la proba de la position du golem
					}
				}
			}
		}
		List<Integer> valeur=(List<Integer>) posGolem.values();
		Set<String> posProb=null;
		Set<String> noms=new HashSet<String>(this.agentpos.keySet());
		if(!posGolem.isEmpty()){
			Collections.sort(valeur);
			// la valeur de la proba de la position qui apparait le plus de fois
			Integer maxv=valeur.get(valeur.size()-1);
			//obtenir les positions probables
			posProb=this.positionProbable(maxv, posGolem);
			if(posProb.isEmpty()) {
				System.out.println("ReceiveInfo : "+this.myAgent.getLocalName()+" didn't find a position");
			}
			else {

				HashMap<String, String> lpossible;
				Iterator<String> iter=posProb.iterator();
				while(!noms.isEmpty() && iter.hasNext()) {
					String posBut=iter.next();
					lpossible= this.mymap.getAllNextNode(noms,posBut,this.agentpos);
					for (String s:lpossible.keySet()) {
						nextPosAgent.put(s, lpossible.get(s));
					}
				}
			}
		}
		
		if(posGolem.isEmpty()|| posProb==null || posProb.isEmpty() ||!noms.isEmpty()) {
			for(String s: noms ) {
				String poss=this.agentpos.remove(s);
				nextPosAgent.put(s,this.mymap.getNextNode(this.agentpos.get(s),this.agentpos));
				this.agentpos.put(s,poss);
			}
		}
	
		return nextPosAgent;
	}
	
	//obtenir la liste des positions observees m fois
	public Set<String> positionProbable(Integer m, HashMap<String,Integer> posG){
		Set<String> res=new HashSet<String>();
		for(String s: posG.keySet()) {
			if(posG.get(s).equals(m)) {
				res.add(s);
			}
		}
		return res;
	}
}
