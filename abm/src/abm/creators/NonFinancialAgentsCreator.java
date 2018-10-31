package abm.creators;

import java.util.ArrayList;

import abm.agents.Agent;
import abm.agents.Bank;
import repast.simphony.space.graph.Network;


public interface NonFinancialAgentsCreator<T> extends AgentsCreator<T> {

	public void createSavings(ArrayList<T> agents, ArrayList<Bank> banks);
	
	public void createLoans(ArrayList<T> agents, ArrayList<Bank> banks);
}
