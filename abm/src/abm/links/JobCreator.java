package abm.links;

import abm.agents.Agent;
import repast.simphony.space.graph.EdgeCreator;

public class JobCreator implements EdgeCreator<Job, Agent> {
	
	public JobCreator() { }

     public Class getEdgeType() {
    	 return Job.class ;
     }
     
     public Job createEdge(Agent source, Agent target, boolean isDirected, double weight) {
    	 return new Job(source, target, isDirected, 0) ;
     }
}

