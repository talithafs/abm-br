package abm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.NonFinancialAgent;
import abm.components.CreditOffering;
import abm.links.Account;
import abm.markets.CreditMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class CreditMarketFlowTest {

	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> agents ;
	protected IndexedIterable<Agent> banks ;

	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		agents = context.getObjects(NonFinancialAgent.class);
		banks = context.getObjects(Bank.class);	
	}
	
	void initNeededCredit(double factor) {
		
		Iterator<Agent> banksItr = banks.iterator() ;
		
		while(banksItr.hasNext()) {
			Bank bank = (Bank) banksItr.next() ;
			bank.calculateTotalCredit();
			
			double total = bank.getTotalCredit() ;
			double needed = MetaParameters.getPercTotalCredit()*total;
			total -= bank.getAssets() ;
			
			ArrayList<NonFinancialAgent> clients = CreditMarket.getInstance().getAdjacent(bank);

			if(factor >= 0) {
				needed = (total / clients.size())*factor ;
			}
			else {
				needed = RandomHelper.nextDoubleFromTo(0, 2*total);
			}
			
			for(NonFinancialAgent client : clients) {
				client.setNeededCredit(needed);
			}
			
			bank.calculateAvailableCredit(); 
		}
	}
	
	@Test
	@DisplayName("Choose banks")
	void choose() {
		
		initNeededCredit(RandomHelper.nextDoubleFromTo(0, 2));
		
		Iterator itr = agents.iterator() ;
		
		while(itr.hasNext()) {
			
			NonFinancialAgent agent = (NonFinancialAgent) itr.next() ;
			Bank chosen = agent.chooseBank() ;
			
			double chosenInterest = 0 ;
			double chosenCredit = 0 ;
			Account origAcc = null ;
			CreditOffering origOff = null ;
			
			ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(agent);
			ArrayList<CreditOffering> offerings = CreditMarket.getInstance().getCreditOfferings(agent);
			
			for(Account account : accounts) {
				if(account.getSource().equals(chosen)) {
					chosenInterest = account.getInterestOnNewLoans() ;
					chosenCredit = account.getAvailableCredit() ;
					origAcc = account ;
				}
			}
			
			if(origAcc != null) {
				accounts.remove(origAcc);
			}
			
			if(offerings != null) {
				for(CreditOffering offering : offerings) {
					if(offering.getBank().equals(chosen)) {
						chosenInterest = offering.getInterest() ;
						chosenCredit = offering.getOffering() ;
						origOff = offering ;
					}
				}
				
				if(origOff != null) {
					offerings.remove(origOff);
				}
			}
			
			
			for(Account account : accounts) {
				if(chosenInterest > account.getInterestOnNewLoans()) {
					assertTrue(chosenCredit > account.getAvailableCredit());
				}
				if(chosenCredit < account.getAvailableCredit()) {
					assertTrue(chosenInterest < account.getInterestOnNewLoans());
				}
			}
			
			if(offerings != null) {
				for(CreditOffering offering : offerings) {
					if(chosenInterest > offering.getInterest()) {
						assertTrue(chosenCredit > offering.getOffering());
					}
					if(chosenCredit < offering.getOffering()) {
						assertTrue(chosenInterest <  offering.getInterest());
					}
				}
			}
		}
	}
	
	@Nested
	@DisplayName("Match")
	class Match {
		
		@Test
		@DisplayName("Agent debt")
		void agentDebt() {
			
			initNeededCredit(RandomHelper.nextDoubleFromTo(0, 2));
			
			HashMap<NonFinancialAgent, Double> mapDebt = new HashMap<NonFinancialAgent, Double>();
			HashMap<NonFinancialAgent, Integer> mapAccs = new HashMap<NonFinancialAgent, Integer>();
			
			for(Agent agent : agents) {
				mapDebt.put((NonFinancialAgent) agent, agent.getDebt());
				mapAccs.put((NonFinancialAgent) agent, CreditMarket.getInstance().getEdges(agent).size());
			}	
			
			CreditMarket.getInstance().match(banks, agents);
			
			for(Agent agent : agents) {
				
				double oldDebt = mapDebt.get(agent);
				double newDebt = agent.getDebt() ;
				
				assertTrue(oldDebt <= newDebt) ;
				
				if(oldDebt < newDebt) {
					
					double diff = newDebt - oldDebt ;
					ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(agent);

					if(accounts.size() > mapAccs.size()){
						ArrayList<CreditOffering> offerings = CreditMarket.getInstance().getCreditOfferings((NonFinancialAgent) agent);
						boolean contain = false ;
						
						for(CreditOffering offering : offerings) {
							if(offering.getOffering() == diff) {
								contain = true ;
							}
						}
						assertTrue(contain);
					}
					
					boolean contain = false ;
					boolean last = false ;
					for(Account account : accounts) {
						if(Math.abs(account.getAvailableCredit() - diff) < 1e-6) {
							contain = true ;
							int size = account.getLoans().size() ;
							double lastLoan = account.getLoans().get(size-1).getAmount();
							if(Math.abs(account.getAvailableCredit() - lastLoan) < 1e-6) {
								last = true ;
							}
						}
					}
					
					assertTrue(contain);
					assertTrue(last);
				}
			}
		}
		
		
		@Test
		@DisplayName("Banks assets")
		void bankAssets() {
			
			initNeededCredit(RandomHelper.nextDoubleFromTo(0, 2));
			
			HashMap<Bank, Double> mapAssets = new HashMap<Bank, Double>();
			
			for(Agent bank : banks) {
				mapAssets.put((Bank) bank, bank.getAssets());
				
				ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				double totalLoans = 0 ;
				
				for(Account account : accounts) {
					totalLoans += account.getLoansAmount() ;
				}
				assertTrue(Math.abs(totalLoans - bank.getAssets()) < 1e-5);
			}
			
			CreditMarket.getInstance().match(banks, agents);
			
			for(Agent bank : banks) {
				double newAssets = bank.getAssets() ;
				double oldAssets = mapAssets.get(bank);
				assertTrue(oldAssets <= newAssets);
				assertTrue(Math.round(newAssets) <= Math.round(((Bank) bank).getTotalCredit()));
				
				ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				double totalLoans = 0 ;
				
				for(Account account : accounts) {
					totalLoans += account.getLoansAmount() ;
				}

				assertTrue(Math.abs(totalLoans - newAssets) < 1e-5);
			}
		}
	}
	
	
}