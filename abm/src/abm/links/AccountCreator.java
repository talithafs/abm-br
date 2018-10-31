package abm.links;

import abm.agents.Agent;
import repast.simphony.space.graph.EdgeCreator;

public class AccountCreator implements EdgeCreator<Account, Agent> {
	
	public AccountCreator() { }

     public Class getEdgeType() {
    	 return Account.class ;
     }
     
     public Account createEdge(Agent source, Agent target, boolean isDirected, double weight) {
    	 return new Account(source, target, isDirected, 0, null);
     }
}
