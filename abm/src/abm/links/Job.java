package abm.links;

import abm.agents.Agent;
import abm.helpers.Utils;
import repast.simphony.space.graph.RepastEdge;

public class Job extends RepastEdge<Agent> {
	
	private double wage ;
	
	public Job(Agent source, Agent target, boolean directed, double wage) {
		super(source, target, directed);
		this.wage = wage;
	}

	public double getWage() {
		return wage;
	}

	public void setWage(double wage) {
		this.wage = wage;
	}
	
	@Override
	public String toString() {
		
		Double wage = this.wage ;

		String[][] fields = { { "Wage", wage.toString()} } ;
		
	    return Utils.getLinkDescriptor(fields, "Job", this.getSource(), this.getTarget()) ;
	}

}
