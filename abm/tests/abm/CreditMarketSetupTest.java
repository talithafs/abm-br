package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.NonFinancialAgent;
import abm.agents.UnemployedConsumer;
import abm.components.CreditOffering;
import abm.components.MachineOrder;
import abm.links.Account;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.CreditMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class CreditMarketSetupTest {

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
	
	@Nested
	@DisplayName("Calculate total credit")
	class TotalCredit {
		
		@Test
		@DisplayName("No bad debt")
		void noBadDebt() {
			
			Iterator<Agent> iBanks = banks.iterator() ;
			
			while(iBanks.hasNext()) {
				
				Bank bank = (Bank) iBanks.next() ;
				ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				double badDebt = 0 ;

				for(Account account : accounts) {
					if(account.getTarget().getNetWorth() < 0) {
						badDebt += account.getLoansAmount() ;
					}
				}
				
				bank.calculateTotalCredit(); 
				
				if(badDebt == 0) {
					assertEquals(bank.getTotalCredit(), bank.getDebt());
				}
				
			}
		}
		
		@Test
		@DisplayName("Discount bad debt")
		void badDebt() {
			
			Iterator<Agent> iBanks = banks.iterator() ;
			
			while(iBanks.hasNext()) {
				
				Bank bank = (Bank) iBanks.next() ;
				ArrayList<NonFinancialAgent> clients = CreditMarket.getInstance().getAdjacent(bank);
				ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				NonFinancialAgent client = null ;
				double badDebt = 0 ;

				for(Account account : accounts) {
					if(account.getTarget().getNetWorth() < 0) {
						badDebt += account.getLoansAmount() ;
					}
				}
				
				if(badDebt == 0) {
					
					while(client == null) {
						NonFinancialAgent random = clients.get(RandomHelper.nextIntFromTo(0, clients.size()-1));
						double rnw = random.getNetWorth() ;
						double bnw = bank.getDebt() ;
						if(rnw > 0 && bnw > rnw) {
							client = random ;
						}
					}
					
					double amount = RandomHelper.nextDoubleFromTo(client.getNetWorth(), bank.getDebt());
					bank.lend(client, amount, 0);
					assertTrue(client.getNetWorth() < 0);
					
					Account acc = CreditMarket.getInstance().getEdge(bank, client);
					badDebt = acc.getLoansAmount() ;
				}
				
				bank.calculateTotalCredit();
				
				double totalCredit = (1 - badDebt/bank.getAssets())*bank.getDebt();
				assertEquals(bank.getTotalCredit(), totalCredit);
			}
			
		}
		
		@Test
		@DisplayName("Total loans")
		void loans() {
			
			Iterator<Agent> iBanks = banks.iterator() ;
			
			while(iBanks.hasNext()) {
				
				Bank bank = (Bank) iBanks.next();
				ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				bank.calculateTotalCredit();
				
				double sumLoans = 0 ;
				
				for(Account account : accounts) {
					sumLoans += account.getLoansAmount() ;
				}
				
				assertTrue(sumLoans <= bank.getTotalCredit());
				assertTrue(Math.abs(sumLoans - bank.getAssets()) < 1e-5);
			}
		}
		
	}
	
	@Nested 
	@DisplayName("Firms needed credit")
	class FirmsNeededCredit {
		
		protected IndexedIterable<Agent> firms ;
		protected IndexedIterable<Agent> conFirms ;
		protected IndexedIterable<Agent> capFirms ;
		
		@BeforeEach
		void init() {
			firms = context.getObjects(Firm.class);
			capFirms = context.getObjects(CapitalGoodsFirm.class);
			conFirms = context.getObjects(ConsumptionGoodsFirm.class);	
		}
		
		void init(ConsumptionGoodsFirm firm) {
			
			long newDemand = (long) (RandomHelper.nextDoubleFromTo(1.05,2)*firm.getInstalledCapacity());
			firm.setInventory((long) (firm.getBeta()*newDemand));
			firm.setCurrentSales(newDemand);
			firm.planProduction();	
		}
		
		@Test
		@DisplayName("Positive profits")
		void posProfit() {
			
			Iterator<Agent> iFirms = firms.iterator() ;
			
			while(iFirms.hasNext()) {
				
				Firm firm = (Firm) iFirms.next() ;
				double payroll = firm.getPayroll() ;
				double income = RandomHelper.nextDouble() + payroll ;
				firm.setIncome(income);
				firm.calculateNeededCredit();
				assertEquals(firm.getNeededCredit(), 0);
				assertEquals(firm.getNeededForLabor(), 0);
			}
		}
		
		@Test
		@DisplayName("Negative profits")
		void negProfit() {
			
			Iterator<Agent> iFirms = firms.iterator() ;
			
			while(iFirms.hasNext()) {
				
				Firm firm = (Firm) iFirms.next() ;
				double payroll = firm.getPayroll() ;
				double income = RandomHelper.nextDoubleFromTo(0, payroll - 10);
				firm.setIncome(income);
				firm.calculateNeededCredit();
				
				if(firm.getProfit() + firm.getNetWorth() <= 0) {
					assertEquals(firm.getNeededCredit(), 0);
					assertEquals(firm.getNeededForLabor(), 0);
				}
				else {
					double needed = (1-firm.getAlpha())*firm.getProfit() ;
					assertEquals(firm.getNeededCredit(), needed);
					assertEquals(firm.getNeededForLabor(), needed);
				}
				
			}
		}
		
		@Test
		@DisplayName("Expansion investments")
		void investments() {
			
			Iterator<Agent> iFirms = conFirms.iterator() ;
			
			while(iFirms.hasNext()) {
				
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) iFirms.next() ;
				init(firm);
				
				firm.calculateNeededCredit();
				
				double amount = 0 ;
				
				for(MachineOrder order : firm.getInvestments()) {
					amount += order.getMachine().getPrice() * order.getQuantity() ;
				}
				
				double needed = (1-firm.getAlpha())*amount ;
				
				if(firm.getNetWorth() + firm.getProfit() > needed){
					assertEquals(needed, firm.getNeededForInvestment());
					assertEquals(needed + firm.getNeededForLabor(), firm.getNeededCredit());
				}
				else {
					assertEquals(0, firm.getNeededForInvestment());
					assertEquals(firm.getNeededForLabor(), firm.getNeededCredit());
				}
			}
			
		}
	}
	
	@Nested 
	@DisplayName("Consumers needed credit")
	class ConsumersNeededCredit {
		
		@Test
		@DisplayName("Total value to spend and net worth")
		void total() {

			Iterator<Agent> iCons = context.getObjects(Consumer.class).iterator() ;
			
			while(iCons.hasNext()) {
				
				Consumer con = (Consumer) iCons.next() ;
				ConsumptionGoodsMarket.getInstance().initSelectionFunction();
				con.calculateDemand();
				con.calculateNeededCredit();
				
				if(con.getNeededCredit() > 0) {
					assertTrue(con instanceof UnemployedConsumer);
					assertEquals(con.getNeededCredit(), con.getTotalValue());
					assertTrue(con.getNetWorth() < con.getTotalValue());
				}
			}
			
		}
	}
	
	void setProbabilities(double prob) {
		
		Iterator<Agent> itr = agents.iterator() ;
		
		while(itr.hasNext()) {
			NonFinancialAgent agent = (NonFinancialAgent) itr.next() ;
			
			if(agent instanceof ConsumptionGoodsFirm) {
				ConsumptionGoodsFirm conFirm = (ConsumptionGoodsFirm) agent ;
				double payroll = conFirm.getNetWorth() + (conFirm.getInstalledCapacity()*conFirm.getPrice())*prob ;
				((Firm) agent).setPayroll(payroll);
			}
			else if(agent instanceof CapitalGoodsFirm) {
				CapitalGoodsFirm capFirm = (CapitalGoodsFirm) agent ;
				double payroll = capFirm.getNetWorth() + capFirm.getIncome()*prob ;
				((Firm) agent).setPayroll(payroll);
			}
		}
	}
	
	void testAccounts(Bank bank) {
		
		ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
		double total = bank.getTotalCredit() ;
		double limit = MetaParameters.getPercTotalCredit()*total ;
		
		ArrayList<Account> firmsAcc = new ArrayList<Account>();
		ArrayList<Account> consAcc = new ArrayList<Account>();
		
		for(Account account : accounts) {
			
			if(account.getAvailableCredit() > limit) {
				fail("Available credit greater than limit per agent");
			}
			
			total -= (account.getLoansAmount() + account.getAvailableCredit()) ;
			
			if(account.getTarget() instanceof Firm) {
				firmsAcc.add(account);
			}
			else if(account.getTarget() instanceof Consumer) {
				consAcc.add(account);
			} 
			else {
				fail("Owner of account is not a non financial agent");
			}
		}
		
		if(total < -1e-5) {
			fail("Bank tried to lend more than total available credit");
		}

		testCreditOfferings(bank, total, limit);
		testInterest(bank, firmsAcc);
		testInterest(bank, consAcc);
	}
	
	void testCreditOfferings(Bank bank, double partialTotal, double limit) {
		
		ArrayList<CreditOffering> offerings = CreditMarket.getInstance().getCreditOfferings(bank);
		
		if(offerings != null && offerings.size() > 0) {
			
			for(CreditOffering offering : offerings) {
				
				if(offering.getOffering() > limit) {
					fail("Available credit greater than limit per agent");
				}
				
				partialTotal -= offering.getOffering() ;
			}
		}
		
		if(partialTotal < -1e-4) {
			fail("Bank tried to lend more than total available credit");
		}	
	}
	
	void testInterest(Bank bank, ArrayList<Account> accounts) {
		
		for(Account one : accounts) {
			for(Account other : accounts) {
				
				double riskOne = bank.calculateRisk(one.getTarget());
				double riskOther = bank.calculateRisk(other.getTarget());
				double intrOne = one.getInterestOnNewLoans() ;
				double intrOther = other.getInterestOnNewLoans() ;
				boolean eval = false ;
				
				if(one.getAvailableCredit() > 0 && other.getAvailableCredit() > 0) {
					eval = true ;
				}
				
				if(eval) {					
					assertTrue(riskOne > 0);
					assertTrue(riskOther > 0);
					assertTrue(intrOne > 0);
					assertTrue(intrOther > 0);
					
					if(!one.equals(other) && intrOne != 0 && intrOther != 0) {
						if(one.getInterestOnNewLoans() > other.getInterestOnNewLoans()) {
							assertTrue(riskOne > riskOther);
						}
						else if(one.getInterestOnNewLoans() < other.getInterestOnNewLoans()) {
							assertTrue(riskOne < riskOther) ;
						}
						else {
							assertEquals(riskOne, riskOther) ;
						}
					}
				}
			}
				
		}
	}
	
	void runAvailableCreditTests(boolean eqLimit, double factor) {
		
		Iterator<Agent> banksItr = banks.iterator() ;
		
		while(banksItr.hasNext()) {
			Bank bank = (Bank) banksItr.next() ;
			bank.calculateTotalCredit();
			
			double total = bank.getTotalCredit() ;
			double needed = MetaParameters.getPercTotalCredit()*total;
			total -= bank.getAssets() ;
			
			ArrayList<NonFinancialAgent> clients = CreditMarket.getInstance().getAdjacent(bank);
			
			if(!eqLimit) {
				if(factor >= 0) {
					needed = (total / clients.size())*factor ;
				}
				else {
					needed = RandomHelper.nextDoubleFromTo(0, 2*total);
				}
			}
			
			for(NonFinancialAgent client : clients) {
				client.setNeededCredit(needed);
			}
			
			bank.calculateAvailableCredit(); 
			testAccounts(bank);
			
			if(factor < 1) {
				ArrayList<CreditOffering> offerings = CreditMarket.getInstance().getCreditOfferings(bank);
				assertTrue(offerings.size() > 0);
			}
		}
		
	}

	@Nested 
	@DisplayName("Calculate available credit")
	class AvailableCredit {
		
		@Nested
		@DisplayName("Limits and totals")
		class Limits {
			
			@Test
			@DisplayName("Credit is equal to total")
			void equal() {
				runAvailableCreditTests(false, 1);
			}
		
			@Test
			@DisplayName("Credit is less than total")
			void less() {
				runAvailableCreditTests(false, 0.5);
			}
			
			@Test
			@DisplayName("Credit is greater than total")
			void greater() {
				runAvailableCreditTests(false, 2);
			}
			
			@Test
			@DisplayName("Random credit")
			void random() {
				runAvailableCreditTests(false, -1);
			}
			
			@Test
			@DisplayName("Equal to limit in every case")
			void limit() {
				runAvailableCreditTests(true, 0);
			}
		
		}
		
		@Nested
		@DisplayName("Probabilities")
		class Probabilities {
			
			@Test
			@DisplayName("Greater than threshold")
			void conFirms() {
				setProbabilities(RandomHelper.nextDoubleFromTo(0.5, 1));
				runAvailableCreditTests(false, 0.5);
				
				Iterator<Agent> itr = banks.iterator() ;
				
				while(itr.hasNext()) {
					Bank bank = (Bank) itr.next() ;
					ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
					for(Account acc : accounts) {
						if(acc.getTarget() instanceof Firm) {
							assertEquals(acc.getAvailableCredit(), 0);
						}
					}
				}
			}
			
			@Test
			@DisplayName("Smaller than threshold")
			void capFirms() {
				setProbabilities(RandomHelper.nextDoubleFromTo(0, 0.49));
				runAvailableCreditTests(false, 1);
				
				Iterator<Agent> itr = banks.iterator() ;
				
				while(itr.hasNext()) {
					Bank bank = (Bank) itr.next() ;
					ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
					double total = bank.getTotalCredit() - bank.getAssets() ;
					double sum = 0 ;
					for(Account acc : accounts) {
						sum += acc.getAvailableCredit() ;
					}
					assertTrue(Math.abs(total - sum) < 1e-4);
				}
			}
			
		}

		@Test
		@DisplayName("Number of non financial agents")
		void nonFin() {
			
			int total = context.getObjects(NonFinancialAgent.class).size();
			int cons = context.getObjects(Consumer.class).size() ;
			int capFirms = context.getObjects(CapitalGoodsFirm.class).size() ;
			int conFirms = context.getObjects(ConsumptionGoodsFirm.class).size() ;
			
			assertEquals(cons + capFirms + conFirms, total);
		}
	}
	
	
	
}
