package abm.components;

import java.util.ArrayList;

import abm.agents.EmployedConsumer;
import abm.agents.Government;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;

public class UsedMachine extends Machine {

	private long usedCapacity = 0;
	private double usedWages = 0 ;
	private ArrayList<EmployedConsumer> operators ;
	
	public UsedMachine(long capacity, double maxWages) {
		super(capacity, maxWages);
		this.operators = new ArrayList<EmployedConsumer>();
	}

	public ArrayList<EmployedConsumer> getOperators() {
		return operators;
	}

	public void addOperator(EmployedConsumer operator) {
		updateOperation(operator.getWage()); 
		this.operators.add(operator);
	}
	
	public void addOperators(ArrayList<EmployedConsumer> operators) {
		
		for(EmployedConsumer operator : operators) {
			addOperator(operator);
		}
	}
	
	public void removeOperator(EmployedConsumer operator) {
		updateOperation(-operator.getWage());
		this.operators.remove(operator);
	}
	
	private void updateOperation(double wage) {
		
		double minWage = Government.getInstance().getParam(Keys.MIN_WAGE);
		double minWages = wage/minWage ;
		this.usedWages += minWages ;
		this.usedCapacity = (long) ((this.usedWages/this.maxWages)*this.capacity) ; 
	}
	

	public long getUsedCapacity() {
		return usedCapacity;
	}

	public void setUsedCapacity(int usedCapacity) {
		this.usedCapacity = usedCapacity;
	}

	public double getUsedWages() {
		return usedWages;
	}

	public void setUsedWages(int usedWages) {
		this.usedWages = usedWages;
	}

	
	@Override
	public String toString() {
		
		String strAgent = super.toString() ;
		
		Long usedCapacity = this.usedCapacity ;
		Double usedWages = this.usedWages ;

		String[][] fields = { { "Used Capacity", usedCapacity.toString()}, 
							{"Used Wages", usedWages.toString()} } ;
		
	    return strAgent + Utils.getComponentDescriptor(fields, false) ;
	}
	
}
