package abm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.CapitalGoodsFirm;
import abm.components.JobOffering;
import abm.links.Job;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class CapFirmPlanningTest {

	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> firms ;

	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		firms = context.getObjects(CapitalGoodsFirm.class);
	}
	
	
	@Nested
	@DisplayName("Job offerings")
	class JobOfferings {
		
		void init(CapitalGoodsFirm firm) {
			double oldIncome = firm.getIncome() ;
			firm.setIncome(Math.abs(RandomHelper.nextDouble()) + oldIncome + 1);
			firm.planProduction(); 
		}
		
		@Test
		@DisplayName("RD goes up")
		void rd() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				double oldRD = firm.getRd();	
				init(firm);
				double newRD = firm.getRd();
				assertTrue(newRD > oldRD);
			}
		}
		
		@Test
		@DisplayName("Offerings are created")
		void offerings() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				init(firm);
				firm.planProduction(); 
				assertTrue(firm.getJobOfferings() != null);
				assertTrue(firm.getJobOfferings().size() > 0) ;
			}
		}
		
		@Test
		@DisplayName("Wages copy")
		void copy() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				init(firm);
				ArrayList<JobOffering> offerings = firm.getJobOfferings() ;
				ArrayList<Job> jobs = LaborMarket.getInstance().getEdges(firm);
				ArrayList<Double> wages = new ArrayList<Double>();
				
				for(Job job : jobs) {
					wages.add(job.getWage());
				}
				
				for(JobOffering offering : offerings) {
					assertTrue(wages.contains(offering.getWage()));
				}
			}
		}
		
		@Test
		@DisplayName("Wages sum")
		void wages() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				double oldRd = firm.getRd() ;
				init(firm);
				ArrayList<JobOffering> offerings = firm.getJobOfferings() ;
				double newRd = firm.getRd();
				
				double sum = 0 ;
				for(JobOffering offering : offerings) {
					sum += offering.getWage() ;
				}
				
				assertTrue(sum >= newRd - oldRd);
			}
		}
	}
	
	@Nested
	@DisplayName("Firing")
	class Firing {
		
		void init(CapitalGoodsFirm firm) {
			double oldIncome = firm.getIncome() ;
			firm.setIncome(RandomHelper.nextDoubleFromTo(0, oldIncome - 1));
			firm.planProduction(); 
		}
		
		@Test
		@DisplayName("RD goes down")
		void rd() {
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				double oldRD = firm.getRd();	
				init(firm);
				double newRD = firm.getRd();
				assertTrue(oldRD > newRD);
			}
		}
		
		@Test
		@DisplayName("Offerings are not created")
		void offerings() {
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				init(firm);
				assertTrue(firm.getJobOfferings() == null);
			}
		}
		
		@Test
		@DisplayName("Number of workers goes down")
		void number() {
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				int oldN = LaborMarket.getInstance().getAdjacent(firm).size();
				init(firm);
				int newN =  LaborMarket.getInstance().getAdjacent(firm).size();
				assertTrue(oldN > newN);
			}
		}
		
		@Test
		@DisplayName("Additional payroll")
		void costs() {
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				CapitalGoodsFirm firm = (CapitalGoodsFirm) firmItr.next() ;
				double oldRd = firm.getRd() ;
				init(firm);
				double newRd = firm.getRd() ;
				double addt = firm.getAdditionalPayroll() ;
				
				assertTrue(addt < 0);
				
				int nEmps = LaborMarket.getInstance().getAdjacent(firm).size() ;
				
				if(nEmps != 0) {
					assertTrue(Math.abs(addt) >= oldRd - newRd);
				}
				
			}
		}
		
		
	}

}
