package abm.components;

public class JobOffering implements Comparable<JobOffering>{
	
	private double wage ;
	private UsedMachine machine ;
	private boolean taken ; 
	
	public JobOffering(double wage) {
		this.wage = wage ;
		this.taken = false ;
	}
	
	public JobOffering(double wage, UsedMachine machine) {
		this.wage = wage;
		this.machine = machine;
		this.taken = false ;
	}

	public double getWage() {
		return wage;
	}
	
	public UsedMachine getMachine() {
		return machine;
	}
	
	public void setTaken(boolean taken) {
		this.taken = taken ;
	}
	
	public boolean getTaken() {
		return taken ;
	}

	@Override
	public int compareTo(JobOffering other) {
		
		if(this.wage == other.wage) {
			return 0 ;
		} 
		else if(this.wage < other.wage) {
			return -1 ;
		} 
		else {
			return 1 ;
		}
	}
	
}
