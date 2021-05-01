package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
public class FinishedBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -130929600282534778L;
	private String nameFSM;

	public FinishedBehaviour(final Agent myagent,String nameFSM) {
		super(myagent);
		this.nameFSM=nameFSM;
	}
	@Override
	public void action() {
		// TODO Auto-generated method stub
		System.out.println(this.myAgent.getLocalName()+"~~~~~ "+": "+this.nameFSM+" finished~~~~~~~~~~~");
	}
	
	public int onEnd() {return ((ExploreCoopAgent) this.myAgent).get_fsm_exitvalue();}
}
