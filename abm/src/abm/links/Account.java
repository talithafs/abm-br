package abm.links;

import java.util.LinkedList;

import abm.agents.Agent;
import abm.agents.Government;
import abm.components.Loan;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.collections.Pair;

public class Account extends RepastEdge<Agent> {
	
	private double deposits = 0 ;
	private LinkedList<Loan> loans = null ;
	private double availableCredit = 0 ;
	private double interestOnNewLoans = 0 ;
	
	public Account(Agent source, Agent target, boolean directed, double deposits, LinkedList<Loan> loans) {
		super(source, target, directed);
		this.deposits = deposits;
		this.loans = loans;
	}
	
	public void updateTarget(Agent target) {
		this.target = target ;
	}
	
	public double getLoansAmount() {
		
		double sum = 0 ;
		
		if(loans != null && loans.size() != 0) {
			for(Loan loan : loans) {
				sum += loan.getAmount();
			}	
		}
		
		return sum ;
	}
	
	public double getDeposits() {
		return deposits;
	}
	
	public LinkedList<Loan> getLoans() {
		return loans;
	}
	
	public void addDeposits(double amount) {
		this.deposits += amount ;
	}
	
	private double updateDeposits() {
		double increase = deposits * Government.getInstance().getParam(Keys.MEAN_SAVS_INT);
		deposits += increase ;
		return increase ;
	}

	public Pair<Double,Double> update(){
		
		double depsInc = updateDeposits();
		double loansInc = 0 ;
		
		for(Loan loan : loans) {
			loansInc += loan.update() ;
		}
		
		return new Pair<Double,Double>(depsInc, loansInc);
	}
	
	public double getAvailableCredit() {
		return availableCredit;
	}

	public void setAvailableCredit(double availableCredit) {
		this.availableCredit = availableCredit;
	}
	

	public double getInterestOnNewLoans() {
		return interestOnNewLoans;
	}

	public void setInterestOnNewLoans(double interestOnNewLoans) {
		this.interestOnNewLoans = interestOnNewLoans;
	}

	@Override
	public String toString() {
		
		Double deposits = this.deposits ;
		Integer sizeLoans = this.loans.size() ;

		String[][] fields = { { "Deposits", deposits.toString()},
							  { "Number of Loans", sizeLoans.toString()} } ;
		
	    return Utils.getLinkDescriptor(fields, "Acount", this.getSource(), this.getTarget()) ;
	}


}
