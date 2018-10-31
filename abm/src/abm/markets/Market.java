package abm.markets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import abm.agents.Agent;
import abm.agents.NonFinancialAgent;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public interface Market<S extends Agent,T extends NonFinancialAgent,L extends RepastEdge<Agent>> {

	public void init(Context<Agent> context, Network<Agent> network);
	
	public L getEdge(S source, T target);
	
	public ArrayList<L> getEdges(Agent node);
	
	public void addEdge(S source, T target);
	
	public L addEdge(L edge);
	
	public void addEdges(List<L> edges);
	
	public void addEdges(List<S> sources, T target);
	
	public void addEdges(S source, List<T> targets);
	
	public void removeEdge(L edge);
	
	public void removeEdge(S source, T target);
	
	public void removeEdges(Agent node);
	
	public ArrayList<T> getAdjacent(S node);
	
	public ArrayList<S> getAdjacent(T node);
	
	public void enterQueue(S source, T target) ;
	
	public T peekQueue(S source);
	
	public T pollQueue(S source);
	
	public boolean inQueue(T target, S source);
	
	public boolean isQueueEmpty(S source);
	
	public LinkedList<T> getQueue(S source);
	
	public void clearQueues() ;
}
