package abm.agents;

import java.util.ArrayList;
import java.util.LinkedList;

import abm.components.CreditOffering;
import abm.components.Loan;
import abm.links.Account;
import abm.markets.LaborMarket;

public class NonFinancialAgent extends Agent {
	
	// State variables
	protected double neededCredit = 0 ;
	protected double grantedCredit = 0 ;
	
	// Markets
	protected static final LaborMarket laborMarket = LaborMarket.getInstance() ;
	
	// Parameters
	protected double amortizationRate =  0.01 ;
	
	public double getNeededCredit() {
		return neededCredit;
	}
	
	public void setNeededCredit(double neededCredit) {
		this.neededCredit = neededCredit;
	}

	public NonFinancialAgent(double assets, double debt) {
		super(assets, debt);
	}
	
	public NonFinancialAgent(double assets, double debt, int id) {
		super(assets, debt, id);
	}

	public void payDebts() {
		
		if(this.getNetWorth() > 0) {
			
			LinkedList<Loan> loans = creditMarket.getLoans(this);
			double maxAmount = this.amortizationRate*this.getNetWorth();
			
			for(Loan loan : loans) {
				double amount = loan.getAmount();
				
				if(amount >= maxAmount) {
					loan.getBank().amortize(this, loan, maxAmount);
					break ;
				}
				else {
					loan.getBank().amortize(this, loan, amount);
					maxAmount -= amount ;
				}
			}
		}
	}
	
	public void receiveGrantedCredit() {
		
		Bank bank = creditMarket.getBankWithDeposits(this);
		
		if(this.neededCredit > 0) {
			this.grantedCredit = creditMarket.getGrantedCredit(this); 
			bank.deposit(this, this.grantedCredit);
		}
		else {
			this.grantedCredit = 0 ;
		}
	}
	
	public void calculateNeededCredit() {
		
	}
	
	public Bank chooseBank() {
         
        ArrayList<Account> accounts = creditMarket.getEdges(this);
         
        double maxCredit = Double.MIN_VALUE ;
        double minInterest = Double.MAX_VALUE ;
         
        for(Account account : accounts) {
             
            double credit = account.getAvailableCredit() ;
            if(maxCredit < credit) {
                maxCredit = credit ;
            }
        }
         
        ArrayList<CreditOffering> offerings = creditMarket.getCreditOfferings(this);
         
        if(offerings != null) {
        	for(CreditOffering offering : offerings) {
                
                double credit = offering.getOffering() ;
                if(maxCredit < credit) {
                    maxCredit = credit ;
                }
            }
        }
                 
        Bank bank = null ;
         
        for(Account account : accounts) {
             
            double credit = account.getAvailableCredit() ;
             
            if(credit == maxCredit) {
                 
                double interest = account.getInterestOnNewLoans();
                 
                if(interest < minInterest) {
                    minInterest = interest ;
                    bank = (Bank) account.getSource() ;
                }
            }
        }
         
        if(offerings != null) {
        	for(CreditOffering offering : offerings) {
                
                double credit = offering.getOffering() ;
                 
                if(credit == maxCredit) {
                     
                    double interest = offering.getInterest();
                     
                    if(interest < minInterest) {
                        minInterest = interest ;
                        bank = offering.getBank() ;
                    }
                }
            }
        }
 
        return bank ;
    }

}
