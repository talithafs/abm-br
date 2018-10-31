package abm.components;

import abm.agents.Bank;
import abm.helpers.Utils;

public class Loan implements Comparable<Loan>{
	
	private double amount ;
	private double interest ;
	private Bank bank ;
	
	private static int counter = 0 ;
	private int id ;
	
	public Loan(double amount, double interest, Bank bank) {
		this.amount = amount;
		this.interest = interest;
		this.id = counter++ ;
		this.bank = bank ;
	}
	
	public Bank getBank() {
		return bank ;
	}
	
	public double getInterest() {
		return interest ;
	}
	
	public double getAmount() {
		return amount ;
	}
	
	public boolean amortize(double amount) {
		this.amount -= amount ;
		
		if(this.amount > 0){
			return true ;
		}
		
		return false ;
	}
	
	public double update() {
		double increase = this.amount * interest ;
		amount += increase ;
		return increase ;
	}

	@Override
	public String toString() {
		
		Double amount = this.amount ;
		Double interest = this.interest ;

		String[][] fields = { { "Amount", amount.toString()}, 
							{"Interest", interest.toString()} } ;
		
	    return Utils.getComponentDescriptor(fields, false);
	}

	@Override
	public int compareTo(Loan other) {
		
		if(this.id == other.id) {
			return 0;
		}
		else if(this.id < other.id) {
			return -1;
		}
		else {
			return 1 ;
		}
	}

}
