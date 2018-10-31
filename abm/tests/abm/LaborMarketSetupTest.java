package abm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.NonFinancialAgent;
import abm.components.JobOffering;
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class LaborMarketSetupTest {

	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> firms ;
	protected IndexedIterable<Agent> banks ;
	protected IndexedIterable<Agent> nonFins ;
	protected IndexedIterable<Agent> consumers ;

	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		firms = context.getObjects(ConsumptionGoodsFirm.class);
		nonFins = context.getObjects(NonFinancialAgent.class) ;
		banks = context.getObjects(Bank.class);
		consumers = context.getObjects(Consumer.class);
		
	}
		
	@Nested
	@DisplayName("Post job offerings")
	class PostOfferings {
		
		void testBudget() {
			
			Iterator<Agent> firmsItr = firms.iterator() ;
			
			while(firmsItr.hasNext()) {
				
				Firm firm = (Firm) firmsItr.next() ;
				
				double granted = CreditMarket.getInstance().getGrantedCredit(firm);
				double grantedForLabor = 0 ;
				
				if(firm.getNeededCredit() != 0) {
					if(granted > firm.getNeededForLabor()) {
						grantedForLabor = firm.getNeededForLabor() ;
					}
					else {
						grantedForLabor = granted ;
					}
				}

				double resources = firm.getFromAssets() + grantedForLabor ;
				double additional = firm.getAdditionalPayroll() ;
				
				assertTrue(resources >= additional);
				
				ArrayList<JobOffering> offerings = firm.getJobOfferings() ;
				
				if(offerings != null) {
					double wages = 0 ;
					if(offerings != null) {
						for(JobOffering offering : offerings) {
							wages += offering.getWage() ;
						}
					}
					assertTrue(Math.abs(wages - additional) < 1e-5);
				}
				
				firm.postJobOfferings(); 
				
				ArrayList<JobOffering> posted = LaborMarket.getInstance().getJobOfferings(firm);
				
				if(posted != null) {
					double wages = 0 ;
					if(offerings != null) {
						for(JobOffering offering : posted) {
							wages += offering.getWage() ;
							assertTrue(offerings.contains(offering));
						}
					}
					assertTrue(Math.abs(wages - additional) < 1e-5);
				}
			}
		}
		
		@Test
		@DisplayName("Demand rises")
		void rise() {
			
			TestsHelper.planProduction(firms, false, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			testBudget();
		}
		
		@Test
		@DisplayName("Demand drops")
		void drop() {
			
			TestsHelper.planProduction(firms, false, true);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			testBudget();
		}
		
		@Test
		@DisplayName("Random demand")
		void random() {
			
			TestsHelper.planProduction(firms, true, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			testBudget();
		}
		
	}
	
	@Nested
	@DisplayName("Pay employees")
	class PayEmployees {
		
		void testAssets() {
			
			Iterator<Agent> firmsItr = firms.iterator() ;
			
			while(firmsItr.hasNext()) {
				
				Firm firm = (Firm) firmsItr.next() ;
				firm.postJobOfferings();
				
				double oldAssets = firm.getAssets() ;
				
				firm.payEmployees(); 
				
				double newAssets = firm.getAssets() ;
				double diff = oldAssets - newAssets ;
				
				ArrayList<EmployedConsumer> emps = LaborMarket.getInstance().getAdjacent(firm);
				
				double payments = 0 ;
				for(EmployedConsumer emp : emps) {
					payments += emp.getWage() ;
				}
				
				assertTrue(Math.abs(payments - diff) < 1e-5);
				
				if(emps.size() > 0) {
					assertTrue(oldAssets > newAssets);
				}
			}
		}

		@Test
		@DisplayName("Demand rises")
		void rise() {
			
			TestsHelper.planProduction(firms, false, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			testAssets();
		}
		
		@Test
		@DisplayName("Demand drops")
		void drop() {
			
			TestsHelper.planProduction(firms, false, true);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			testAssets();
		}
		
		@Test
		@DisplayName("Random demand")
		void random() {
			
			TestsHelper.planProduction(firms, true, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			testAssets();
		}
	}
	
}
