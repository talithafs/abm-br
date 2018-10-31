package abm.agents;

import java.util.ArrayList;
import java.util.LinkedList;

import abm.MetaParameters;
import abm.Controller;
import abm.components.CreditOffering;
import abm.components.Loan;
import abm.components.Statistics;
import abm.helpers.Constants.Keys;
import abm.links.Account;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.Pair;

public class Bank extends FinancialAgent {
	
	// State variables
	private double meanInterestOnLoans = 0 ;
	private double margin = -1 ;
	private double interestOnDeposits = 0 ;
	private double totalCredit ;
	private double lent ;
	private double toLend ; 
	private double badDebt = -1 ;

	// Parameters
	private double mu =	 0.1 ;
	private double phi =  0.5 ;
	
	// Auxiliary variables
	private int nLoans = 0 ;
 	
	public Bank() {  } 

	public Bank(double assets, double debt) {
		super(assets, debt);
	}
	
	public void initMargin() {
		if(this.margin == -1) {
			
			double diff = this.meanInterestOnLoans - CentralBank.getInstance().getSelic() ;
			this.margin =  diff*MetaParameters.getMarginPercSpread() ;
		}
	}
	
	public void calculateTotalCredit() {
		
		ArrayList<Account> accounts = creditMarket.getEdges(this);
		double badDebt = 0;
	
		for(Account account : accounts) {
			if(account.getTarget().getNetWorth() < 0) {
				badDebt += account.getLoansAmount() ;
			}
		}
	
		this.badDebt = badDebt ;

		// TODO Ver capital adequacy ratio
		this.totalCredit = (1 - this.badDebt/this.getAssets())*this.getDebt() ;
	}
	
	public void calculateAvailableCredit() {
		
		ArrayList<NonFinancialAgent> agents = creditMarket.getAdjacent(this);
		SimUtilities.shuffle(agents, RandomHelper.getUniform());
		
		int nAgents = (int) Government.getInstance().getCount(NonFinancialAgent.class) ;
		int nBanks = (int) Government.getInstance().getParam(Keys.N_BANKS);
		int n = nAgents / nBanks ;
		
		ArrayList<NonFinancialAgent> targets = creditMarket.getRandomAgents(this, n);
		
		if(targets.size() != 0) {
			agents.addAll(targets);
		} 
		
		double limitPerAgent = MetaParameters.getPercTotalCredit()*this.totalCredit ;
		double newLoans = this.totalCredit - this.getAssets() ;
		this.toLend = newLoans ;
		
		for(NonFinancialAgent agent : agents) {
				
			double prob = 0 ;
			double psi = 0 ;
				
			if(agent instanceof ConsumptionGoodsFirm) {
				
				ConsumptionGoodsFirm conFirm = (ConsumptionGoodsFirm) agent ;
				psi =  (conFirm.getPayroll() - conFirm.getNetWorth())	/
								(conFirm.getInstalledCapacity()*conFirm.getPrice());
	
			} 
			else if(agent instanceof CapitalGoodsFirm) {
				
				CapitalGoodsFirm capFirm = (CapitalGoodsFirm) agent ;
				
				psi =  (capFirm.getPayroll() - capFirm.getNetWorth()) / capFirm.getIncome() ;
			}
			
			if(psi > 0) {
				
				if(psi <= 1) {
					prob = psi ;
				} 
				else {
					prob = 1 ;
				}
			}
			
			double availableCredit = 0 ;
			double loansAmount = 0 ;
			Account account = getAccount(agent);
			
			if(account != null) {
				loansAmount = account.getLoansAmount() ;
			}
				
			if(loansAmount < limitPerAgent && newLoans > loansAmount) {
				
				boolean cond1 = (prob <= this.phi) ;
				boolean cond2 = agent.getNeededCredit() <= limitPerAgent - loansAmount ;
				boolean cond3 = agent.getNeededCredit() <= newLoans ;
				
				if(cond1) {
					
					if(cond2 && cond3) {
						availableCredit = agent.getNeededCredit() ;
					} 
					else if(!cond2 && cond3) { // maior q lim por agt, menor q total
						availableCredit = limitPerAgent - loansAmount ;
					}
					else if(cond2 && !cond3) { // menor que o lim por agr, maior q total
						availableCredit = newLoans ;
					} 
					else { //!cond2 && !cond3
						if(limitPerAgent > newLoans) {  // available = o que sobrar
							availableCredit = newLoans;
						}
						else {
							availableCredit = limitPerAgent - loansAmount ;
						}
					}
				}
				else {
					availableCredit = 0 ;
				}
			}
		

			newLoans -= availableCredit;

			// TODO Establish a minimum credit
			if(newLoans >= 0) {
				double interest = calculateInterest(agent);
				
				if(account != null) {
					account.setAvailableCredit(availableCredit);
					account.setInterestOnNewLoans(interest);
				} 
				else {
					CreditOffering offering = new CreditOffering(availableCredit, interest, this);
					creditMarket.postCreditOffering(agent, offering);
				}
			}
			else {
				newLoans += availableCredit ;
			}
		
		}
	}

	
	public void grantLoans() {
		
		double credit = 0 ; 
		
		while(!creditMarket.isQueueEmpty(this)) {
			
			if(this.getNetWorth() < 0) {
				NonFinancialAgent agent = creditMarket.pollQueue(this);
				Account account = creditMarket.getEdge(this, agent);
				
				double amount ;
				double interest ;
				
				if(account == null) {
					CreditOffering offering = creditMarket.getCreditOffering(agent, this);
					amount = offering.getOffering() ;
					interest = offering.getInterest() ;
				}
				else {
					amount = account.getAvailableCredit() ;
					interest = account.getInterestOnNewLoans() ;
				}
				
				lend(agent, amount, interest);
				credit += amount ;
			} else {
				break ;
			}
		}
		
		this.lent = credit ;
	}

	public void calculateMargin() {

		double unif =  RandomHelper.createUniform(0,1).nextDouble() ;
		
		if(this.lent < this.toLend) {
			this.margin  *= (1 - this.mu*unif) ;
		}
		else {
			this.margin *= (1 + this.mu*unif) ;
		}
	}
	
	public double calculateRisk(Agent agent) {
		
		if(agent instanceof Firm) {
			
			double perc =  (1-((Firm) agent).getNEmployees() / Statistics.getInstance().getLastNEmployees());
			double debtToNetWorth = agent.getDebt() / agent.getNetWorth() ;
			
			return  (perc*debtToNetWorth*MetaParameters.getFirmsRiskPremium()) ;
		}
		else if(agent instanceof Consumer) {
			
			return MetaParameters.getConsRiskPremium()*(agent.getDebt()/agent.getNetWorth());
		}
		
		return 0 ;
	}
	
	private double calculateInterest(Agent agent) {
		return CentralBank.getInstance().getSelic() + this.margin + calculateRisk(agent) ;
	}
	
	private Account createAccount(Agent agent, double deposits, Loan loan) {
		
		LinkedList<Loan> loans = new LinkedList<Loan>();
		
		if(loan != null) {
			loans.add(loan);
		}
		
		Account account = new Account(this,agent,false, deposits,loans);
		
		return creditMarket.addEdge(account); 
	}

	public void lend(NonFinancialAgent agent, double amount, double interest) {
		
		Account account = getAccount(agent);
		Loan loan =  new Loan(amount, interest,this);
		
		if(account == null) {
			account = createAccount(agent, 0, loan);
		}
		else {
			account.getLoans().add(loan);
			agent.updateDebt(amount);
		}
		
		updateAssets(amount);
		updateMeanInterestOnLoans(interest);
	}
	
	public void amortize(NonFinancialAgent agent, Loan loan, double amount) {
		
		Account account = getAccount(agent);
		LinkedList<Loan> loans = account.getLoans() ;
		boolean isPaid = false ;
		
		if(loans.contains(loan)) {
			isPaid = loan.amortize(amount);
		}
		
		if(isPaid) {
			double interest = loan.getInterest() ;
			updateMeanInterestOnLoans(-interest) ;
			loans.remove(loan);
		}
		
		updateAssets(-amount);
		agent.updateDebt(-amount);
	}
	
	
	public void withdraw(NonFinancialAgent agent, double amount) {
		
		Account account = getAccount(agent);
		
		account.addDeposits(-amount);
		updateDebt(-amount);
		agent.updateAssets(-amount);
	}
	
	private void updateMeanInterestOnLoans(double newInterest) {
		
		if(this.nLoans == 0) {
			
			if(newInterest > 0) {
				this.nLoans = 1 ;
				this.meanInterestOnLoans = newInterest ;
			}
			
			return ;
		}
		
		double sum = this.nLoans * this.meanInterestOnLoans ;
		
		if(newInterest > 0) {
			this.nLoans ++ ;
		}
		else {
			this.nLoans -- ;
		}
		
		this.meanInterestOnLoans = (sum + newInterest)/this.nLoans ;	
	}

	
	private Account getAccount(NonFinancialAgent agent) {
		return creditMarket.getEdge(this, agent);
	}
	
	public void deposit(NonFinancialAgent agent, double amount) {
		
		Account account = getAccount(agent);
		
		if(account == null) {
			account = createAccount(agent, amount, null);
		} 
		else {
			account.addDeposits(amount);
			agent.updateAssets(amount);
		}
		
		updateDebt(amount);
	}
	
	public void updateAccounts() {
		
		ArrayList<Account> accounts = creditMarket.getEdges(this);
		
		for(Account account : accounts) {
			
			Pair<Double,Double> incs = account.update() ;
			double depsInc = incs.getFirst() ;
			double loansInc = incs.getSecond() ;
			
			this.updateAssets(loansInc);
			account.getTarget().updateDebt(loansInc);
			this.updateDebt(depsInc);
			account.getTarget().updateAssets(depsInc);
		}
	}
	
	public double getInterestOnDeposits() {
		return interestOnDeposits;
	}

	public void initInterestOnDeposits(double interestOnDeposits) {
		
		if(this.interestOnDeposits == 0) {
			this.interestOnDeposits = interestOnDeposits;
		}
	}

	public double getBadDebt() {
		return badDebt;
	}

	public void setBadDebt(double badDebt) {
		this.badDebt = badDebt;
	}

	public double getLent() {
		return lent;
	}

	public void setLent(double lent) {
		this.lent = lent;
	}

	public double getTotalCredit() {
		return totalCredit;
	}

	public double getMeanInterestOnLoans() {
		return meanInterestOnLoans;
	}

	public double getMargin() {
		return margin;
	}

	public double getMu() {
		return mu;
	}

	public double getPhi() {
		return phi;
	}
	
	
}
