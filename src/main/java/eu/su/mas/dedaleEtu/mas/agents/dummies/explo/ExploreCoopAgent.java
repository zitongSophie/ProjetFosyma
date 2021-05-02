package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.AddBlockBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.AddEndBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FinishedBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.IsAllFinishedBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.IsFinishedExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.IsFinishedHuntAloneBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.IsFinishedHuntTogetherBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MoveAloneBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MoveTogetherBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveInfoTogetherBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceivePosAndOdeursBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendBlockBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendEndBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosAndOdeursBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
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
	private int fsm_exitvalue=0;//0->hunt alone,1->hunt together,2->finished
	private Date myTemps;
	private List<String>finiblock;
	private boolean endblock=false;
	private List<String>lstench;
	private HashMap<String,String>agents_pos;
	private Couple<Date,List<String>> list_recent_odeurs;
	private List<String>Cg;
	private List<String> finiExpl;
	private Integer end=0;
	private List<String> CgChasse;
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
		this.Cg=new ArrayList<String>();
		myTemps=new java.util.Date();
		fsm_exitvalue=0;
		lstench=new ArrayList<String>();
		agents_pos=new HashMap<String,String>();
		this.CgChasse=new ArrayList<String>();
		//List<String> list_agentNames=new ArrayList<String>();
		this.finiExpl=new ArrayList<String>();
		this.list_recent_odeurs=new Couple<Date,List<String>>(myTemps,new ArrayList<String>() );
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
		
		List<String>pos_avant_next=new ArrayList<String>();
		HashMap<String,String>check=new HashMap<String,String>();
		pos_avant_next.add("-1");
		pos_avant_next.add("-1");
		//main fsm behaviour
		String E=" ExploCoop ";
		String HA=" HuntAlone ";
		String HT=" HuntTogether ";
		String SB="SendBlock";
		String AB="AddBlock";
		String AE="AddEnd";
		String SE="SendEnd";
		String FA="FinishedALL";//exit with exitvalue given
		String FE="Finishedexpl";
		String FHA="FinishedAlone";
		String FHT="Finishedtogether";
		String IAF="IsAllFinished";
		FSMBehaviour fsm_main = new FSMBehaviour(this);
		fsm_main=new FSMBehaviour(this);
		FSMBehaviour fsm_exploration = new FSMBehaviour(this); 
		FSMBehaviour fsm_hunt_alone = new FSMBehaviour(this);
		FSMBehaviour fsm_hunt_together = new FSMBehaviour(this);
		fsm_main.registerFirstState(fsm_exploration, E);
		fsm_main.registerState(fsm_hunt_alone, HA);
		fsm_main.registerState(new AddBlockBehaviour(this, CgChasse), AB);
		fsm_main.registerState(new SendBlockBehaviour(this, CgChasse), SB);
		fsm_main.registerState(new IsAllFinishedBehaviour(this, this.CgChasse), IAF);
		fsm_main.registerLastState(new FinishedBehaviour(this," ALL "), FA);
		//String s="share";
		//fsm_main.registerLastState(new ShareMapBehaviour(this, myMap, agents_pos, otherInfo),s);
		//fsm_main.registerDefaultTransition(CB, AB);

		//0:->huntalone,1:->hunttogether,2:->checkblock(not sure to block),3:->addblock(sure to block)
		fsm_main.registerTransition(E, HA, 0);
		fsm_main.registerTransition(E, HT, 1);
		fsm_main.registerTransition(E, AB, 3);
		
		fsm_main.registerTransition(HA, HT, 1);
		fsm_main.registerTransition(HA, AB, 3);
		
		fsm_main.registerDefaultTransition(HT, AB);
		fsm_main.registerDefaultTransition(AB, SB);
		fsm_main.registerDefaultTransition(SB, IAF);
		fsm_main.registerTransition(IAF, AB,1);
		fsm_main.registerTransition(IAF, FA,2);
		
		
		//exploration fsm behaviour 	
		String ME="MoveExploCoop";
		String SPO="SendPosAndOdeurs";//my current position, <date, the most recent information about stench that i have >, sending time
		String RPOE="ReceivePosAndOdeurs";//sending date should be after myTemps update information
		String SM="ShareMap";
		String RM="ReceiveMap";//sending date should be after myTemps,if message receive type match ReceiveInformation :go to RIE
		String IFE="IfFinishedExplo";//if finished give the exitvalue to finish
		// Define the different states and behaviours 
		fsm_exploration.registerFirstState (new ExploCoopBehaviour(this, myMap, otherInfo,agents_pos, pos_avant_next), ME); // Register the transitions
		fsm_exploration.registerState(new SendPosAndOdeursBehaviour(this, list_recent_odeurs),SPO);
		fsm_exploration.registerState (new ReceivePosAndOdeursBehaviour(this, agents_pos, this.list_recent_odeurs), RPOE); 
		fsm_exploration.registerState(new ShareMapBehaviour(this, myMap, agents_pos, otherInfo),SM);
		fsm_exploration.registerState(new ReceiveMapBehaviour(this, myMap, agents_pos),RM);
		fsm_exploration.registerState(new IsFinishedExploBehaviour(this, myMap, agents_pos, pos_avant_next, pos_avant_next),IFE);
		fsm_exploration.registerState(new AddEndBehaviour(this, Cg),AE);
		fsm_exploration.registerLastState(new FinishedBehaviour(this,E), FE);
		fsm_exploration.registerDefaultTransition (ME,SPO);
		fsm_exploration.registerDefaultTransition (SPO,RPOE);
		fsm_exploration.registerDefaultTransition (RPOE,SM);
		fsm_exploration.registerDefaultTransition (SM,RM);
		fsm_exploration.registerDefaultTransition (RM,IFE);
		fsm_exploration.registerTransition (IFE,ME, 1);
		fsm_exploration.registerTransition (IFE,AE, 2);
		
		fsm_exploration.registerDefaultTransition (AE,FE);

		
		
		//chasse alone fsm behaviour
		String MA="MoveAlone";
		String IFHA="IsFinishedHuntAlone";
		
		// Define the different states and behaviours 
        fsm_hunt_alone.registerFirstState(new MoveAloneBehaviour(this, myMap,pos_avant_next),MA); 
        fsm_hunt_alone.registerState(new IsFinishedHuntAloneBehaviour(this, myMap, agents_pos, pos_avant_next),IFHA);
        fsm_hunt_alone.registerState(new SendEndBehaviour(this, Cg),SE);
        fsm_hunt_alone.registerState(new AddEndBehaviour(this, Cg),AE);
        fsm_hunt_alone.registerState(new AddBlockBehaviour(this, CgChasse),AE);
        fsm_hunt_alone.registerLastState(new FinishedBehaviour(this,HA), FHA);
        fsm_hunt_alone.registerDefaultTransition(MA, SE);
        fsm_hunt_alone.registerDefaultTransition(SE, AE);
        fsm_hunt_alone.registerDefaultTransition(AE, IFHA);
        fsm_hunt_alone.registerTransition (IFHA,MA, 1);//move alone
        fsm_hunt_alone.registerTransition (IFHA,AB, 2);//finished
        fsm_hunt_alone.registerDefaultTransition(AB, FHA);
        

        String MT="MoveTogether";
        String IFHT="IsFinishedHuntTogether";
        String RIT="ReceiveInformationTogeter";
        fsm_hunt_together.registerFirstState (new SendPosAndOdeursBehaviour(this, this.list_recent_odeurs), SPO);
        fsm_hunt_together.registerState (new SendEndBehaviour(this, pos_avant_next), MT);
        fsm_hunt_together.registerState(new AddEndBehaviour(this, pos_avant_next), ME);
        fsm_hunt_together.registerState (new ShareMapBehaviour(this, myMap, agents_pos, otherInfo), SM);
        fsm_hunt_together.registerState (new MoveTogetherBehaviour(this, myMap), MT); // Register the transitions
        fsm_hunt_together.registerState(new IsFinishedHuntTogetherBehaviour(this, myMap, agents_pos, pos_avant_next,finiExpl),IFHT);
        fsm_hunt_together.registerState(new ReceiveInfoTogetherBehaviour(this, myMap, agents_pos,this.list_recent_odeurs),RIT);
        fsm_hunt_together.registerLastState(new FinishedBehaviour(this, HT), FHT);
        fsm_hunt_together.registerDefaultTransition (SM,SE);//Default 
        fsm_hunt_together.registerDefaultTransition (SE,AE);
        fsm_hunt_together.registerDefaultTransition (AE,RIT);
        fsm_hunt_together.registerDefaultTransition (RIT,MT);
        //1:continue;
        //2:fini chasse solo fsm(fini block ou passer chasse together)
        fsm_hunt_together.registerTransition (SPO,SE, 4);
        fsm_hunt_together.registerTransition (SPO,SM, 3);
        fsm_hunt_together.registerTransition (IFHT,SPO, 1);
        fsm_hunt_together.registerTransition (IFHT,FHA, 2);

        

	
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		lb.add(fsm_main);
		
		
		
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
		c=new Couple<Integer,SerializableSimpleGraph<String, MapAttribute>>(vu, sg);
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
	
	//====        ====//
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
	public void set_fsm_exitvalue(int i) {
		this.fsm_exitvalue=i;
	}
	
	public int get_fsm_exitvalue() {
		return this.fsm_exitvalue;
	}
	public void setmyTemps() {
        this.myTemps=new java.util.Date();
    }
    public Date getmyTemps() {
        return this.myTemps;
    }
	public MapRepresentation getMap() {
		return this.myMap;
	}
	public void setMap(MapRepresentation m) {
		this.myMap=m;
	}
    //===      ====//
    
	public List<String> getNodeAdjacent(String node){
		return this.myMap.getnodeAdjacent(node);
	}
	
	

	
	public boolean setCg(String name){
		  if(!Cg.contains(name)){
		    this.Cg.add(name);
		    return true; 
		  }
		  return false;
	}

	public List<String> getCg(){
		 return this.Cg;
	}
	
    public boolean isAllFiniExplo() {
        if(this.isIdenticalList(this.getAgentsListDF("coureur"), Cg)) {
            return true;
        }
        return false;
    }

    public boolean isAllFiniChasse() {
        if(this.isIdenticalList(this.getAgentsListDF("coureur"), CgChasse)) {
            return true;
        }
        return false;
    }
    
    public boolean setCgChasse(String name){
        if(!CgChasse.contains(name)){
            this.CgChasse.add(name);
            return true; 
        }
          return false;
    }

    public List<String> getCgChasse(){
        return this.CgChasse;
    }


}
