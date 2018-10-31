package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.Government;
import abm.agents.NonFinancialAgent;
import abm.agents.UnemployedConsumer;
import abm.components.JobOffering;
import abm.components.UsedMachine;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;
import abm.links.Job;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.util.collections.Pair;

@SuppressWarnings({ "rawtypes", "unchecked" })
class LaborMarketFlowTest {

	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> firms ;
	protected IndexedIterable<Agent> banks ;
	protected IndexedIterable<Agent> nonFins ;
	protected IndexedIterable<Agent> unemps ;
	protected IndexedIterable<Agent> consumers ;
	
	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		firms = context.getObjects(Firm.class);
		nonFins = context.getObjects(NonFinancialAgent.class) ;
		banks = context.getObjects(Bank.class);
		unemps = context.getObjects(UnemployedConsumer.class);	
		consumers = context.getObjects(Consumer.class);
	}
	
	@Nested
	@DisplayName("Choose job offering")
	class ChooseOffering {
		
		void testChoices() {
			
			ArrayList<Agent> unempCons = Utils.shuffle(unemps);
			
			for(Agent agent : unempCons) {
				UnemployedConsumer unemp = (UnemployedConsumer) agent ;
				Pair<Firm, JobOffering> chosen = unemp.chooseJobOffering() ;
				
				if(chosen != null) {
					ArrayList<JobOffering> offerings = LaborMarket.getInstance().getJobOfferings(chosen.getFirst());
					assertTrue(offerings.contains(chosen.getSecond()));
					assertTrue(chosen.getSecond().getWage() >= unemp.getResWage());
				}
			}
		}
		
		@Test
		@DisplayName("Demand rises")
		void rise() {
			
			TestsHelper.planProduction(firms, false, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			TestsHelper.setupLaborMarket(firms);
			testChoices();
		}
		
		@Test
		@DisplayName("Demand drops")
		void drop() {
			
			TestsHelper.planProduction(firms, false, true);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			TestsHelper.setupLaborMarket(firms);	
			testChoices();
		}
		
		@Test
		@DisplayName("Random demand")
		void random() {
			
			TestsHelper.planProduction(firms, true, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			TestsHelper.setupLaborMarket(firms);
			testChoices();
		}
	}
	
	@Nested
	@DisplayName("Choose job offering")
	class Match {
		
		void testMatch() {
			
			Iterator<Agent> firmsItr = firms.iterator() ;
			HashMap<Firm, Integer> mapEmps = new HashMap<Firm,Integer>();
			HashMap<Firm, ArrayList<JobOffering>> mapOffs = new HashMap<Firm,ArrayList<JobOffering>>();
			
			while(firmsItr.hasNext()) {
				Firm firm = (Firm) firmsItr.next() ;
				int oldSize = LaborMarket.getInstance().getAdjacent(firm).size();
				mapEmps.put(firm, oldSize);
				mapOffs.put(firm, firm.copyOfferings());
			}
			
			LaborMarket.getInstance().match();
			
			firmsItr = firms.iterator() ;
			
			while(firmsItr.hasNext()) {
				
				Firm firm = (Firm) firmsItr.next() ;
				
				int newSize = LaborMarket.getInstance().getAdjacent(firm).size();
				ArrayList<JobOffering> offerings = mapOffs.get(firm);
				
				if(offerings != null && offerings.size() > 0) {
					int oldSize = mapEmps.get(firm);
					assertTrue(oldSize <= newSize) ;
					
					if(oldSize < newSize) {
						
						int nJobs = newSize - oldSize ;				
						int n = 0 ;
						
						ArrayList<Job> jobs = LaborMarket.getInstance().getEdges(firm);
						for(Job job : jobs) {
							for(JobOffering offering : offerings) {								
								if(offering.getWage() == job.getWage()) {
									n++ ;
								}
							}
						}
						assertTrue(n >= nJobs);
					}
				}
				
				ArrayList<EmployedConsumer> emps = LaborMarket.getInstance().getAdjacent(firm);
				double wages = 0 ;
				for(EmployedConsumer emp : emps) {
					wages += emp.getWage() ;
				}
				
				assertTrue(Math.abs(wages - (firm.getPayroll() + firm.getAdditionalPayroll())) < 1e-9);
			
				if(firm instanceof ConsumptionGoodsFirm) {
					testMachines((ConsumptionGoodsFirm) firm);
				}	
			}
			testWages();
		}
		
		void testWages() {
			
			Iterator<Agent> conItr = consumers.iterator();
			
			while(conItr.hasNext()) {
				Consumer con = (Consumer) conItr.next();
				
				if(con instanceof EmployedConsumer) {
					assertEquals(con.getResWage(), ((EmployedConsumer) con).getWage());
				}
				else {
					if(con.getPayment() > 0) {
						assertTrue(con.getResWage() < con.getPayment());
					}
				}
			}
		}
		
		void testMachines(ConsumptionGoodsFirm firm) {
			
			ArrayList<UsedMachine> macs = firm.getMachines() ;
			double minWage = Government.getInstance().getParam(Keys.MIN_WAGE);
			long instCap = firm.getInstalledCapacity() ;
			long usedCap = firm.getUsedCapacity() ;
			double minWages = (firm.getAdditionalPayroll() + firm.getPayroll())/minWage ;
			
			long sumInst = 0 ;
			long sumUsed = 0 ;
			double sumWages = 0 ;
			
			for(UsedMachine mac : macs) {
				sumInst += mac.getCapacity() ;
				sumUsed += mac.getUsedCapacity() ;
				sumWages += mac.getUsedWages() ; 
			}
			
			assertEquals(sumInst, instCap);
			assertEquals(sumUsed, usedCap);
			assertTrue(Math.abs(minWages - sumWages) <= 1e-10);
		}
		
		@Test
		@DisplayName("Demand rises")
		void rise() {
			
			TestsHelper.planProduction(firms, false, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			TestsHelper.setupLaborMarket(firms);
			testMatch();
		}
		
		@Test
		@DisplayName("Demand drops")
		void drop() {
			
			TestsHelper.planProduction(firms, false, true);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			TestsHelper.setupLaborMarket(firms);
			testMatch();
		}
		
		@Test
		@DisplayName("Random demand")
		void random() {
			
			TestsHelper.planProduction(firms, true, false);
			TestsHelper.planConsumption(consumers);
			TestsHelper.calculateCredit(banks, nonFins);
			TestsHelper.setupLaborMarket(firms);
			testMatch();
		}
		
	}
}


