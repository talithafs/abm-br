package abm.components;

import abm.helpers.Utils;

public class Machine implements Comparable<Machine>{
	
	private static int idCounter = 0 ;

	protected long capacity ;
	protected double maxWages ;
	private double efficiency ; 
	
	private int id  ;
	
	
	public Machine(long capacity, double maxWages) {
		this.capacity = capacity;
		this.maxWages = maxWages;
		this.efficiency = capacity / maxWages ; 
		this.id = idCounter++ ;
	}

	public int getId() {
		return id;
	}

	public long getCapacity() {
		return capacity;
	}


	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	public double getMaxWages() {
		return maxWages;
	}


	public void setMaxWages(double maxWages) {
		this.maxWages = maxWages;
	}


	public double getEfficiency() {
		return efficiency;
	}
	
	@Override
	public boolean equals(Object other) {
		
		if(other instanceof Machine) {
			
			boolean cond1 = ((Machine) other).getCapacity() == this.capacity ;
			boolean cond2 = ((Machine) other).getMaxWages() == this.maxWages ;
			
			if(cond1 && cond2) {
				return true ;
			}
			
			return false ;
		}
		
		return false ;
	}
	
	@Override
	public String toString() {
		
		Integer id = this.id ;
		Long capacity = this.capacity ;
		Double maxWages = this.maxWages ;
		Double efficiency = this.efficiency ;
		

		String[][] fields = {{"Id", id.toString() }, 
							 {"Capacity", capacity.toString()}, 
							 {"MaxWages", maxWages.toString()},
							 {"Efficiency", efficiency.toString()} } ;
		
	    return Utils.getComponentDescriptor(fields, true) ;
	}

	@Override
	public int compareTo(Machine other) {
		
		if(this.efficiency == other.efficiency) {
			return 0 ;
		} 
		else if(this.efficiency < other.efficiency) {
			return -1 ;
		} 
		else {
			return 1 ;
		}
	}
	
}
