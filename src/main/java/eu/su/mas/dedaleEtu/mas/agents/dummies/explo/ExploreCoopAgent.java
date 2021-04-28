package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveNameBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import dataStructures.tuple.Couple;
//import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
//add agents_pos:l54,l63
/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapRepresentation myMap;
	private HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>> otherInfo;
	private List<String> agentToShareMap; //agents to share the map
	private List<String> agentToAsk;
	private Date temps=new java.util.Date();
	
	private Integer end=0; // fin de share,ReceiveName
	private List<String> finiExpl;
	//

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		final Object[] args = getArguments();
		finiExpl=new ArrayList<String>();
		//List<String> list_agentNames=new ArrayList<String>();
/*add*/	HashMap<String,String> agents_pos=new HashMap<String,String>();
		
//Inscription	
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID()); // The agent AID
		ServiceDescription sd = new ServiceDescription () ;
		sd.setType( "coureur" ); // You have to give a
		sd.setName(this.getLocalName());//(local)name of
		dfd.addServices(sd);
		//Register the service

		DFAgentDescription result;
		try {
			result = DFService.register( this , dfd );
			System.out. println ( "-------\n"+result+ "results \n--------" ) ;
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//initialisation 
		this.otherInfo=new HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>();
		List<String> agentsNames=this.getAgentsListDF("coureur");
		agentsNames.remove(this.getLocalName());
		for(String s: agentsNames) {
			agents_pos.put(s,(String)"-1");
			
			System.out.println(this.getLocalName()+"nom des agents "+s);
			//SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
			Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>c;
			//0 : vus 0 fois,  1 vide et viens d echanger info   2: graphes non vide
			c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(0, new SerializableSimpleGraph<String,MapAttribute>());
			this.otherInfo.put(s,c);
		}
		// demander aux agents ou on a des choses a envoyer
		this.agentToAsk=new ArrayList<String>();
		for (String s :this.otherInfo.keySet()) {
			if(this.otherInfo.get(s).getLeft()==0) {
				this.agentToAsk.add(s);
			}
		}
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		
		lb.add(new ExploCoopBehaviour(this,this.myMap,this.otherInfo,this.agentToAsk,this.agentToShareMap,agents_pos));

		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	//AMS
	public List <String> getAgentsListAMS(){
		AMSAgentDescription [] agentsDescriptionCatalog = null;
		List <String> agentsNames= new ArrayList<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c .setMaxResults ( Long.valueOf(-1));
			agentsDescriptionCatalog = AMSService.search(this, new
					AMSAgentDescription (), c );
		}
		catch (Exception e) {
			System.out. println ( "Problem searching AMS: " + e );
			e . printStackTrace () ;
		}
		for ( int i=0; i<agentsDescriptionCatalog.length ; i++){
			AID agentID = agentsDescriptionCatalog[i ]. getName();
			agentsNames.add(agentID.getLocalName());
		}
		return agentsNames;
	}


	//yellow page
	public List<String> getAgentsListDF(String type){
		List <String> agentsNames= new ArrayList<String>();
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription () ;
		sd .setType(type); // name of the service
		dfd . addServices(sd) ;
		DFAgentDescription[] result;
		try {
			result = DFService.search( this , dfd);
			for (int i=0; i<result.length; i++) {
				agentsNames.add(result[i].getName().getLocalName());
			}
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			System.out. println ( "Problem searching Yellow Page: " + e );
			e.printStackTrace();
		} //You get the list of all the agents (AID)offering this service
		return agentsNames;
	}
	
	//Modifier les donnees du graphes
	public Couple<Integer,SerializableSimpleGraph<String, MapAttribute>> setCouple(String agentName,SerializableSimpleGraph<String, MapAttribute>sg,Integer vu){
		Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>c;
		Integer nbvus=this.otherInfo.get(agentName).getLeft();
		c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(nbvus+vu, sg);
		return c;
	}
	
	public List<String> setAgentToAsk() {
		List<String> whoAsk=new ArrayList<String>();
		for (String s :this.otherInfo.keySet()) {
			if(this.otherInfo.get(s).getLeft()==0) {
				whoAsk.add(s);
			}
			else {
				if(this.otherInfo.get(s).getLeft()==2) {
					whoAsk.add(s);
				}
			}
		}
		return whoAsk;
	}	
	
	public boolean isIdenticalList (List<String> h1, List<String> h2) {
	    if ( h1.size() != h2.size() ) {
	        return false;
	    }
	    List<String> clone = new ArrayList<String>(h2); 

	    Iterator<String> it = h1.iterator();
	    while (it.hasNext() ){
	        String A = it.next();
	        if (clone.contains(A)){ 
	            clone.remove(A);
	        } else {
	            return false;
	        }
	    }
	    return true; //will only return true if sets are equal
	}	

	public boolean isIdenticalList (Set<String> h1, List<String> h2) {
		System.out.println(h1.toString());
		System.out.println(h2.toString());
	    if ( h1.size() != h2.size() ) {
	        return false;
	    }
	    List<String> clone = new ArrayList<String>(h2); 

	    Iterator<String> it = h1.iterator();
	    System.out.println("iterateur "+it);
	    while (it.hasNext() ){
	        String A = it.next();
	        if (clone.contains(A)){ 
	            clone.remove(A);
	        } else {
	            return false;
	        }
	    }
	    return true; //will only return true if sets are equal
	}	
	
	public Set<String> getAgentName() {//sans le nom de l agent this
		return this.otherInfo.keySet();
	}
	
	
	
	public void inscription(String type) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID()); // The agent AID
		ServiceDescription sd = new ServiceDescription () ;
		sd.setType( type ); // You have to give a
		sd.setName(this.getLocalName());//(local)name of
		dfd.addServices(sd);
		//Register the service

		DFAgentDescription result;
		try {
			result = DFService.register( this , dfd );
			System.out. println ( "-------\n"+this.getLocalName()+ " est devenu "+type+"\n--------" ) ;
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void desincription(String type) {
		DFAgentDescription dfd = new DFAgentDescription();
		this.getDefaultDF().setName(this.getLocalName()); 
		// The agent AID
		ServiceDescription sd = new ServiceDescription () ;
		sd.setType(type ); // You have to give a
		sd.setName(this.getLocalName());//(local)name of
		dfd.addServices(sd);
		//Register the service

		try {
			DFService.deregister( this, dfd );
			System.out. println ( "------- supprime agent courreur"+this.getLocalName()+" \n--------" ) ;
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public Integer setFini(String name) {
		if(this.finiExpl.contains(name)) {
			return 0;
		}
		boolean nouveau=this.finiExpl.add(name);
		if(nouveau) {// set change
			System.out.println(this.getLocalName()+" add "+ name+ " as finished");
			if(!this.otherInfo.isEmpty()) {
				if(this.finiExpl.size()==this.otherInfo.keySet().size()) {
					List<String> agentsNames=this.getAgentsListDF("coureur");
					agentsNames.remove(this.getLocalName());
					if(isIdenticalList (finiExpl, agentsNames))
						return 1; //tout le monde a fini
				}
			}
		}
		return 0;
	}
	
	public void setFini(List<String> lname) {
		this.finiExpl=new ArrayList<String>(lname);
	}
	
	public void setEnd() {
		this.end=1;
	}

	public Integer getFini() {
		return this.end;
	}
	

	
	public List<String> getFiniExpl(){
		return this.finiExpl;
	}
		

}
