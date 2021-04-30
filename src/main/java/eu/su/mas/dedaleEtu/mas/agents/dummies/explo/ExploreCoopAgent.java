package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.IsFinishedExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MoveTogetherBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveDecisionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceivePosAndOdeursInExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendDecisionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosAndOdeursBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.TermineBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
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
	private int agent_exitvalue;//0->hunt alone,1->hunt together,2->finished
	private List<String>list_agents_finished;
	private Date myTemps;
	private List<String>finiblock;
	private boolean endblock=false;
	private List<String>lstench;
	private HashMap<String,String>agents_pos;
	private Couple<Date,List<String>> list_recent_odeurs;
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
		//1)set the agent attributes
		
		myTemps=new java.util.Date();
		list_agents_finished=new ArrayList<String>();
		agent_exitvalue=-1;
		lstench=new ArrayList<String>();
		agents_pos=new HashMap<String,String>();
		//List<String> list_agentNames=new ArrayList<String>();
		
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
		//Initialization 
		this.otherInfo=new HashMap<String,Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>>();
		List<String> agentsNames=this.getAgentsListDF("coureur");
		agentsNames.remove(this.getLocalName());
		for(String s: agentsNames) {
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
		
		//exploration fsm behaviour 	
		String E="ExploCoop";
		String SPO="SendPosAndOdeurs";//my current position, <date, the most recent information about stench that i have >, sending time
		String RPO="ReceivePosAndOdeursInExplo";//sending date should be after myTemps update information
		String SM="ShareMap";
		String RM="ReceiveMap";//sending date should be after myTemps,if message receive type match ReceiveInformation :go to RIE
		String IFE="IfFinishedExplo";//if finished give the exitvalue to finish
		String F="Finished";//exit with exitvalue given
		FSMBehaviour fsm_exploration = new FSMBehaviour(this); // Define the different states and behaviours 
		fsm_exploration.registerFirstState (new ExploCoopBehaviour(null, myMap, otherInfo, agents_pos), E); // Register the transitions
		fsm_exploration.registerState(new SendPosAndOdeursBehaviour(this, list_recent_odeurs),SPO);
		fsm_exploration.registerState (new ReceivePosAndOdeursInExploBehaviour(this, agents_pos, this.list_recent_odeurs), RPO); 
		fsm_exploration.registerState(new ShareMapBehaviour(this, myMap, agents_pos, otherInfo),SM);
		fsm_exploration.registerState(new ReceiveMapBehaviour(this, myMap, agents_pos),RM);
		fsm_exploration.registerState(new IsFinishedExploBehaviour(this, myMap),IFE);
		fsm_exploration.registerLastState(new TermineBehaviour(), F);
		fsm_exploration.registerDefaultTransition (E,SPO);//Default 
		fsm_exploration.registerDefaultTransition (SPO,RPO);
		fsm_exploration.registerDefaultTransition (RPO,SM);
		fsm_exploration.registerDefaultTransition (SM,RM);
		fsm_exploration.registerDefaultTransition (RM,IFE);
		fsm_exploration. registerTransition (IFE,E, 1);
		fsm_exploration. registerTransition (IFE,F, 2);
		
		//chasse alone fsm behaviour
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		lb.add(fsm_exploration);
		
		
		
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
		this.agentToAsk=whoAsk;
		return whoAsk;
	}	
	
	public boolean isIdenticalList (List<String> h1, List<String> h2) {
		if(h1.isEmpty()) {
			if(h2.isEmpty()) {
				return true;
			}
			return false;
		}else {
			if(h2.isEmpty()) {
				return false;
			}
		}
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
			System.out. println ( "------- supprime agent coureur"+this.getLocalName()+" \n--------" ) ;
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	
	
	//====pour la chasse
	public void setendblock() {
		this.endblock=true;
	}
	
	public boolean getendblock() {
		return this.endblock;
	}
	
	public List<String>getfiniblock(){
		return this.finiblock;
	}
	
	public Integer setfiniblock(String name) {
		boolean nouveau=this.finiblock.add(name);
		if(nouveau) {// set change
			System.out.println(this.getLocalName()+" add "+ name+ " as finished");
			if(!this.otherInfo.isEmpty()) {
				if(this.finiblock.size()==this.otherInfo.keySet().size()) {
					List<String> agentsNames=this.getAgentsListDF("coureur");
					agentsNames.remove(this.getLocalName());
					if(isIdenticalList (finiblock, agentsNames))
						return 1; //tout le monde a fini
				}
			}
		}
		return 0;
	}
	
	public void setfiniblock(List<String> lname) {
		this.finiblock=new ArrayList<String>(lname);
	}
	
	
	public List<String> lstench(){
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=this.observe();
		List<String> lstench=new ArrayList<String>();
		for (Couple<String,List<Couple<Observation,Integer>>> cobs : lobs) {
			String pos=cobs.getLeft();
			for(Couple<Observation,Integer> isStench: cobs.getRight()) {
				if(isStench.getLeft().getName().equals("Stench") ){
					lstench.add(pos);
					break;
				}
			}
			
		}
		return lstench;
	}
	
	public void setmyTemps() {
        this.myTemps=new java.util.Date();
    }
    public Date getmyTemps() {
        return this.myTemps;
    }
    
	public List<String> getNodeAdjacent(String node){
		return this.myMap.getnodeAdjacent(node);
	}
	public void setAgentPos(HashMap<String,String>ap) {
		this.agents_pos=ap;
	}
	public HashMap<String,String> getagentpos(){
		return this.agents_pos;
	}


}
