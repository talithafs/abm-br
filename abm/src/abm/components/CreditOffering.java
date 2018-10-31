package abm.components;

import abm.agents.Bank;

public class CreditOffering {

	double offering ;
	double interest ;
	Bank bank ;
	
	public CreditOffering(double offering, double interest, Bank bank) {
		this.offering = offering;
		this.interest = interest;
		this.bank = bank ;
	}

	public double getOffering() {
		return offering;
	}

	public double getInterest() {
		return interest;
	}

	public Bank getBank() {
		return bank;
	}
	
	
}
