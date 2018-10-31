package abm.agents;


import abm.helpers.Utils;

public class EmployedConsumer extends Consumer {

	// State variables
	private double wage ;

	public EmployedConsumer(double savingPerc, double resWage, double assets, double debt, double wage) {
		super(savingPerc, resWage, assets, debt);
		this.wage = wage;
		this.payment = wage ;
	}
	
	public EmployedConsumer(double savingPerc, double resWage, double assets, double debt, double wage, double percWealth, int id) {
		super(savingPerc, resWage, assets, debt, percWealth, id);
		this.wage = wage;
		this.payment = wage ;
	}

	public void adjustReservationWage() {
		this.resWage = this.wage ;
	}

	public double getWage() {
		return wage;
	}

	public void setWage(double wage) {
		this.wage = wage;
	}
	
	public void receivePayment(double wage) {
		this.payment = wage ;
	}
	
	@Override 
	public String toString() {
		
		String strAgent = super.toString() ;
		Double wage = this.wage ;
		String[][] fields = { { "Wage", wage.toString()} } ;
		
	    return strAgent + Utils.getAgentDescriptor(fields, false) ;
	}

	
	
}
