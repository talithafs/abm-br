package abm.markets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abm.Controller;
import abm.agents.Agent;
import abm.agents.NonFinancialAgent;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

@SuppressWarnings("unchecked")
public class AbstractMarket<S extends Agent,T extends NonFinancialAgent, L extends RepastEdge<Agent>>  implements Market<S,T,L> {

	private HashMap<S, LinkedList<T>> queues ;
	private Network<Agent> network ;
	protected Context<Agent> context ;
	
	@Override
	public void init(Context<Agent> context, Network<Agent> network) {
		this.network = network ;
		this.queues = new HashMap<S, LinkedList<T>>();
		this.context = context ;
	}

	@Override
	public L getEdge(S source, T target) {
		return (L) network.getEdge(source, target);
	}

	@Override
	public ArrayList<L> getEdges(Agent node) {
		
		ArrayList<L> list = new ArrayList<L>();
		Iterable<RepastEdge<Agent>> edges = network.getEdges(node);
		
		if(edges != null) {
			for(RepastEdge<Agent> edge : edges) {
				list.add((L) edge);
			}
		}

		return list;
	}

	@Override
	public void addEdge(S source, T target) {
		network.addEdge(source,target);
	}

	@Override
	public L addEdge(L edge) {
		return (L) network.addEdge(edge);
	}

	@Override
	public void removeEdge(L edge) {
		network.removeEdge(edge);
	}
	
	@Override
	public void removeEdge(S source, T target) {
		RepastEdge<Agent> edge = network.getEdge(source, target);
		network.removeEdge(edge);
	}

	@Override
	public void removeEdges(Agent node) {
		ArrayList<L> edges = getEdges(node);
		
		for(L edge : edges) {
			network.removeEdge(edge);
		}
	}

	@Override
	public ArrayList<T> getAdjacent(S node) {
		
		ArrayList<T> list = new ArrayList<T>();
		Iterable<Agent> agents = network.getAdjacent(node);
		
		for(Agent agent : agents) {
			list.add((T) agent);
		}
		
		return list;
	}
	
	@Override
	public ArrayList<S> getAdjacent(T node) {
		
		ArrayList<S> list = new ArrayList<S>();
		Iterable<Agent> agents = network.getAdjacent(node);
		
		if(agents != null) {
			for(Agent agent : agents) {
				list.add((S) agent);
			}
		}
		
		return list;
	}

	@Override
	public void enterQueue(S source, T target) {
		
		LinkedList<T> queue = queues.get(source) ;
		
		if(queue == null) {
			queues.put(source, new LinkedList<T>());
			queue = queues.get(source);
		}

		queue.push(target);
	}

	@Override
	public T peekQueue(S source) {
		
		LinkedList<T> queue = queues.get(source) ;
		
		if(queue != null) {
			return (T) queue.peek();
		}

		return null;
	}

	@Override
	public T pollQueue(S source) {

		LinkedList<T> queue = queues.get(source) ;
		
		if(queue != null) {
			return (T) queue.poll();
		}

		return null;
	}

	@Override
	public boolean inQueue(T target, S source) {
		
		LinkedList<T> queue = queues.get(source) ;
		
		if(queue != null) {
			return queue.contains(target);
		}

		return false ;
	}

	@Override
	public boolean isQueueEmpty(S source) {
		
		LinkedList<T> queue = queues.get(source) ;
		
		if(queue != null) {
			return queue.isEmpty();
		}

		return true ;
	}
	
	@Override
	public void clearQueues() {
		
		queues = new HashMap<S, LinkedList<T>>();
	}

	@Override
	public void addEdges(List<L> edges) {
		
		for(L edge : edges) {
			network.addEdge(edge);
		}
		
	}

	@Override
	public void addEdges(List<S> sources, T target) {
		
		for(S source : sources) {
			network.addEdge(source, target);
		}
		
	}

	@Override
	public void addEdges(S source, List<T> targets) {
		
		for(T target : targets) {
			network.addEdge(source, target);
		}
		
	}

	@Override
	public LinkedList<T> getQueue(S source) {
		return queues.get(source);
		
	}
	
}
