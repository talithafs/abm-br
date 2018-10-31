package abm.creators;

import abm.agents.Account;
import abm.agents.Agent;
import repast.simphony.space.graph.EdgeCreator;

public class AccountsCreator implements EdgeCreator<Account, Agent> {

	public AccountsCreator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class getEdgeType() {
		return Account.class;
	}

	@Override
	public Account createEdge(Agent source, Agent target, boolean isDirected, double weight) {
		// TODO Auto-generated method stub
		return null;
	}

}
