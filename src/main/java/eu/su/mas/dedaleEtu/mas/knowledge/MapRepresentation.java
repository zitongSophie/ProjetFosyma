package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import javafx.application.Platform;


//apres la methode getShortestPathToClosestOpenNode ligne 166,
//changement de cette methode et ajouter de methode (nodeadjacent(plus besoin,je le garde au cas ou),getnextnode juste apres


/**
 * This simple topology representation only deals with the graph, not its content.</br>
 * The knowledge representation is not well written (at all), it is just given as a minimal example.</br>
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * 
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {	
		agent,open,closed;

	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration


	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		Platform.runLater(() -> {
			openGui();
		});
		//this.viewer = this.g.display();
		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id
	 * @param mapAttribute
	 */
	public synchronized void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
	}

	/**
	 * Add a node to the graph. Do nothing if the node already exists.
	 * If new, it is labeled as open (non-visited)
	 * @param id id of the node
	 * @return true if added
	 */
	public synchronized boolean addNewNode(String id) {
		if (this.g.getNode(id)==null){
			addNode(id,MapAttribute.open);
			return true;
		}
		return false;
	}

	/**
	 * Add an undirect edge if not already existing.
	 * @param idNode1
	 * @param idNode2
	 */
	public synchronized void addEdge(String idNode1,String idNode2){
		this.nbEdges++;
		try {
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (IdAlreadyInUseException e1) {
			System.err.println("ID existing");
			System.exit(1);
		}catch (EdgeRejectedException e2) {
			this.nbEdges--;
		} catch(ElementNotFoundException e3){

		}
	}
	
	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow, null if the targeted node is not currently reachable
	 */
	public synchronized List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if (shortestPath.isEmpty()) {//The openNode is not currently reachable
			return null;
		}else {
			shortestPath.remove(0);//remove the current position
		}
		return shortestPath;
	}
//=================CHANGEMENT DE CETTE METHODE
	public List<String> getShortestPathToClosestOpenNode(String myPosition,List<String> nodes) {
		//1) Get all openNodes
		List<String> opennodes=getOpenNodes();
		if(nodes!=null) {
			for (String node:nodes) {
			opennodes.remove(node);
			}
		}
		if(opennodes.isEmpty()) {
			opennodes=getOpenNodes();
		}
		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
				.map(on -> (getShortestPath(myPosition,on)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
				.collect(Collectors.toList());

		Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
		//3) Compute shorterPath

		return getShortestPath(myPosition,closest.get().getLeft());
	}
	
	
//============AJOUTER DE METHODE===============
// 
	/**
	 * get all nodeAdjacents of myPosition ,except othersPosition(the position of other agent)
	 * 
	 * @param myPosition
	 * @param observe
	 * @param agents_pos
	 * @return list nodeAdjacent of myPosition, don't include othersPosition
	 */
	public List<String> nodeAdjacent(String myPosition,List<Couple<String,List<Couple<Observation,Integer>>>> observe,List<String> agents_pos){
		List<String>nodeadj=new ArrayList<String>();
		for (Couple<String,List<Couple<Observation,Integer>>> c:observe) {
			nodeadj.add(c.getLeft());
		}
		if(agents_pos==null) {
			nodeadj.remove(myPosition);
			if(nodeadj.isEmpty()) {
				nodeadj.add(myPosition);
			}		
			return nodeadj;
		}
		
		//System.out.println("list node adjecents before remove "+nodeadj);
		for (String pos:agents_pos) {
			nodeadj.remove(pos);
		}
		nodeadj.remove(myPosition);
		if(nodeadj.isEmpty()) {
			nodeadj.add(myPosition);
		}		
		
		//System.out.println("list node adjecents "+nodeadj);
		return nodeadj;
		
	}
	/**
	 * 
	 * @param myPosition 
	 * @param agents_pos
	 * @param observe 
	 * @return nextNode
	 */
	public String getNextNode(String myPosition,HashMap<String,String> agents_pos) {
		List<String> posAgent=new ArrayList<String>();
		for (String ag: agents_pos.values()) {
			posAgent.add(ag);
		}
		
		List<String> mypath=this.getShortestPathToClosestOpenNode(myPosition,null);
		if(posAgent.isEmpty()) {
			return mypath.get(0);
		}
		int s1 = mypath.size();
		int s2;
		List<String> nodes = new ArrayList<String>();//open nodes will be explored by other agents
//		System.out.println("mypath"+mypath+"size"+mypath.size());
		List<String>otherspath;
		for (String pos:posAgent){
			if(this.g.getNode(pos)==null) {
				continue;
			}
			otherspath=this.getShortestPathToClosestOpenNode(pos,null);
			s2=otherspath.size();
			//no need to compare with other agent path choosen when otherspath is empty or longer than mypath
			if(otherspath.isEmpty()|| s2>s1 ) {
				continue;
			}
			//if other agent path is shorter than mypath and have the same target(open node to explore in the end of the path)
			//add this open node to the nodes which are considered no need to explore by my agent,re-compute mypath
			if(mypath.get(s1-1)==otherspath.get(s2-1)) {
				nodes.add(otherspath.get(s2-1));
				mypath=this.getShortestPathToClosestOpenNode(myPosition,nodes);
			}
		
			
		}
		List<String> nodeAdj=this.getnodeAdjacent(myPosition);
		if(posAgent.contains(mypath.get(0))) {
	        
	        for (String pos:posAgent){
	            nodeAdj.remove(pos);
	        }
	        if(nodeAdj.isEmpty()) {
	            nodeAdj.add(myPosition);
	        }
			Integer r=(int)( Math.random() *  nodeAdj.size()  );
			return nodeAdj.get(r);
		}

		//System.out.println("maprepresentation get nextnode : "+mypath.get(0)+" \t nodeadj : "+nodeAdj+"\t pos agent: "+posAgent);
		return mypath.get(0);
		
		
		
	}
	
//
	//
	//
//fin de changement
//-----------------------------

	//fonction pour la partie chasse wumpus
	
	

	public List<String> getnodeAdjacent(String id){
		Node n=this.getG().getNode(id);
		List<String>nodeadj=new ArrayList<String>();
		Iterator<Edge> iterE=n.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			nodeadj.add(e.getOpposite(n).getId());
			
		}	
		return nodeadj;
	}
	
	

	public List<String> getOpenNodes(){
		return this.g.nodes()
				.filter(x ->x .getAttribute("ui.class")==MapAttribute.open.toString()) 
				.map(Node::getId)
				.collect(Collectors.toList());
	}


	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		serializeGraphTopology();

		closeGui();

		this.g=null;
	}

	/**
	 * Before sending the agent knowledge of the map it should be serialized.
	 */
	private void serializeGraphTopology() {
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}	
	}


	public synchronized SerializableSimpleGraph<String,MapAttribute> getSerializableGraph(){
		serializeGraphTopology();
		return this.sg;
	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public synchronized void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private synchronized void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			//Platform.runLater(() -> {
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			//});
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private synchronized void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);//GRAPH_IN_GUI_THREAD)
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);

		g.display();
	}

	public void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
		//System.out.println("You should decide what you want to save and how");
		//System.out.println("We currently blindy add the topology");

		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			//System.out.println(n);
			boolean alreadyIn =false;
			//1 Add the node
			Node newnode=null;
			try {
				newnode=this.g.addNode(n.getNodeId());
			}	catch(IdAlreadyInUseException e) {
				alreadyIn=true;
				//System.out.println("Already in"+n.getNodeId());
			}
			if (!alreadyIn) {
				newnode.setAttribute("ui.label", newnode.getId());
				newnode.setAttribute("ui.class", n.getNodeContent().toString());
			}else{
				newnode=this.g.getNode(n.getNodeId());
				//3 check its attribute. If it is below the one received, update it.
				if (((String) newnode.getAttribute("ui.class"))==MapAttribute.closed.toString() || n.getNodeContent().toString()==MapAttribute.closed.toString()) {
					newnode.setAttribute("ui.class",MapAttribute.closed.toString());
				}
			}
		}

		//4 now that all nodes are added, we can add edges
		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			for(String s:sgreceived.getEdges(n.getNodeId())){
				addEdge(n.getNodeId(),s);
			}
		}
		//System.out.println("Merge done");
	}

	/**
	 * 
	 * @return true if there exist at least one openNode on the graph 
	 */
	public boolean hasOpenNode() {
		return (this.g.nodes()
				.filter(n -> n.getAttribute("ui.class")==MapAttribute.open.toString())
				.findAny()).isPresent();
	}


	public SerializableSimpleGraph<String, MapAttribute> addNodeSG(SerializableSimpleGraph<String, MapAttribute>mysg,String id,MapAttribute mapAttribute){
		//for (SerializableNode<String, MapAttribute> n: mysg.getAllNodes()){
		//	System.out.println("avant "+n.getNodeId());
		//}
		mysg.addNode(id,mapAttribute);
		//System.out.println("ici "+id);
		//for (SerializableNode<String, MapAttribute> n: mysg.getAllNodes()){
		//	System.out.println("apres "+n.getNodeId());
		//}
		return mysg;
	}

	public synchronized boolean addNewNodeSG(SerializableSimpleGraph<String, MapAttribute>mysg,String id) {
		if (mysg.getNode(id)==null){
			addNode(id,MapAttribute.open);
			return true;
		}
		return false;
	}	
	public synchronized SerializableSimpleGraph<String, MapAttribute> addEdgeSG(SerializableSimpleGraph<String, MapAttribute>mysg,String source,String target,String name){
		mysg.addEdge(name,source, target);
		return mysg;
	}
	
	public Integer getNbEdges() {
		return nbEdges;
	}
	public Graph getG() {
		return g;
	}
	
    public HashMap<String,String> getAllNextNode(Set<String> agentsansposition,String posBut,HashMap<String,String> agents_pos) {

        HashMap<String,String> allpostomove=new HashMap<String,String>();
        List<String> noeudAdjBut=getnodeAdjacent(posBut); //noeud adj de posBut
        for(String name:agentsansposition) {
            if(noeudAdjBut.contains(agents_pos.get(name))) {
                allpostomove.put(name, agents_pos.get(name)); //pas besoin de bouger
                noeudAdjBut.remove(agents_pos.get(name));
            }
        }
        for (String nodeAdj:noeudAdjBut) {
            Integer min= null;
            HashMap<String,String> lpath=new HashMap<String,String>();
            for (String name  : agentsansposition) {
                List<String> mypath=this.getShortestPath(agents_pos.get(name),nodeAdj); //chemin pour l'agent
                
                if(min==null) {
                    lpath=new HashMap<String,String>();
                    min=mypath.size();;
                    lpath.put(name, mypath.get(0));

                }
                if(min.equals(mypath.size())) {
                    lpath.put(name, mypath.get(0));
                }
                    
                if(min>mypath.size()) {
                    lpath=new HashMap<String,String>();
                    min=mypath.size();
                    lpath.put(name, mypath.get(0));
                }
            }
            for (String s: lpath.keySet()) {
                allpostomove.put(s, lpath.get(s));
                agentsansposition.remove(s);
            }
                
        }
        return allpostomove;
    
    }  
	
	

}