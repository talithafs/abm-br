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
import abm.agents.ConsumptionGoodsFirm ;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.Government;
import abm.agents.UnemployedConsumer;
import abm.components.JobOffering;
import abm.components.MachineOrder;
import abm.components.NewMachine;
import abm.helpers.Constants.Keys;
import abm.markets.CapitalGoodsMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class ConFirmPlanningTest {
	
	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> firms ;

	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		firms = context.getObjects(ConsumptionGoodsFirm.class);
	}
	
	@Nested
	@DisplayName("Price movements")
	class Price {
		
		@Test
		@DisplayName("Up")
		void up() {

			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				double oldPrice = firm.getPrice() ;
				
				long newDemand = 2*firm.getLastSupply() ;
				firm.setCurrentSales(newDemand);
				firm.setInventory((long)(firm.getBeta()*newDemand));
				firm.planProduction();
				
				assertTrue(oldPrice < firm.getPrice());
			}
		}
		
		@Test
		@DisplayName("Down")
		void down() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				double oldPrice = firm.getPrice() ;
				
				long newDemand = firm.getLastSupply()/2 ;
				firm.setCurrentSales(newDemand);
				firm.setInventory((long)(firm.getBeta()*newDemand));
				firm.planProduction();

				assertTrue(oldPrice > firm.getPrice());
			}
		}
	}
	
	@Nested
	@DisplayName("Firing employees")
	class Firing {
		
		void init(ConsumptionGoodsFirm firm) {
			long newDemand = (long) (RandomHelper.nextDoubleFromTo(0.1, 0.85)*firm.getUsedCapacity());
			firm.setInventory((long) (firm.getBeta()*newDemand));
			firm.setCurrentSales(newDemand);
			firm.planProduction();
		}
		
		@Test 
		@DisplayName("Number of employees and global employed consumers")
		void number() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				
				int oldEmpsSize = firm.getNEmployees();
				ArrayList<EmployedConsumer> oldEmps = LaborMarket.getInstance().getAdjacent(firm);
				assertEquals(oldEmpsSize, oldEmps.size());

				init(firm);
				
				int newEmpsSize = firm.getNEmployees();
				ArrayList<EmployedConsumer> newEmps = LaborMarket.getInstance().getAdjacent(firm);
				
				assertEquals(newEmpsSize, newEmps.size());
				assertTrue(newEmpsSize < oldEmpsSize);
			}
		}
		
		@Test
		@DisplayName("Used capacity and additional payroll") 
		void usedCap() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				
				ArrayList<EmployedConsumer> oldEmps = LaborMarket.getInstance().getAdjacent(firm);
				long oldUC = firm.getUsedCapacity() ;
			
				init(firm);
				
				ArrayList<EmployedConsumer> newEmps = LaborMarket.getInstance().getAdjacent(firm);
				long newUC = firm.getUsedCapacity() ;

				assertTrue(oldUC > newUC);
				
				double diff = firm.getAdditionalPayroll() ;
				assertTrue(diff < 0);
				
				double ratio1 = Math.abs(diff)/firm.getPayroll();
				double ratio2 =  (double)(oldUC - newUC)/oldUC ;

				assertTrue(Math.abs(ratio1 - ratio2) < 1e-1);
				
				double oldPayroll = 0 ;
				for(EmployedConsumer emp : oldEmps) {
					oldPayroll += emp.getWage() ;
				}
				
				double newPayroll = 0 ;
				for(EmployedConsumer emp : newEmps) {
					newPayroll += emp.getWage() ;
				}
				
				assertEquals(diff, newPayroll - oldPayroll);
			}
		}
		

		@Test
		@DisplayName("Unemployed payment and change of status") 
		void status() {
	
			Iterator<Agent> firmItr = firms.iterator() ;
			double sumPayrolls = 0 ;
			double sumAdditionals = 0 ;
			double oldUnempsWages = 0 ;
			
			IndexedIterable<Agent> unemps = context.getObjects(UnemployedConsumer.class);
			
			for(Agent unemp : unemps) {
				oldUnempsWages += ((UnemployedConsumer) unemp).getResWage() ;
			}
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				int oldEmpsSize = context.getObjects(EmployedConsumer.class).size();
				int oldUnempsSize = context.getObjects(UnemployedConsumer.class).size();

				init(firm);
				
				int newEmpsSize = context.getObjects(EmployedConsumer.class).size();
				int newUnempsSize = context.getObjects(UnemployedConsumer.class).size();
				
				assertTrue(newEmpsSize < oldEmpsSize);
				assertTrue(newUnempsSize > oldUnempsSize);
				
				sumPayrolls += firm.getPayroll() ;
				sumAdditionals += firm.getAdditionalPayroll() ;
			}
			
			IndexedIterable<Agent> emps = context.getObjects(EmployedConsumer.class);
			unemps =  context.getObjects(UnemployedConsumer.class);
			
			double empsPayroll = 0 ;
			for(Agent emp : emps) {
				
				Firm employer = LaborMarket.getInstance().getEmployer((EmployedConsumer)emp);
				
				if(employer instanceof ConsumptionGoodsFirm) {
					empsPayroll += ((EmployedConsumer) emp).getWage() ;
				}
			}
			
			double newUnempsWages = 0 ;
			double unempsPayments = 0 ;
			for(Agent unemp : unemps) {
				newUnempsWages += ((UnemployedConsumer) unemp).getResWage() ;
				unempsPayments +=  ((UnemployedConsumer) unemp).getPayment() ;
			}
			
			assertEquals(sumPayrolls + sumAdditionals, empsPayroll);
			assertEquals(unempsPayments, newUnempsWages - oldUnempsWages);
		}
		
		@Test
		@DisplayName("Assets") 
		void assets() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				double oldAssets = firm.getAssets() ;

				init(firm);
				
				double newAssets = firm.getAssets() ;
				double diff = newAssets - oldAssets ;
				
				if(oldAssets >= Math.abs(firm.getAdditionalPayroll())) {
					assertEquals(diff, firm.getAdditionalPayroll());
				}
			}
		
		}
		
		@Test
		@DisplayName("Number of job offerings") 
		void offerings() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;

				init(firm);
				
				assertEquals(firm.getJobOfferings(), null);
			}
		}
	}

	@Nested
	@DisplayName("Creating job offerings")
	class JobOfferings {
		
		boolean init(ConsumptionGoodsFirm firm) {
			
			double prob = RandomHelper.createUniform(0, 1).nextDouble() ;
			boolean isProp = false ;
			long newDemand ;
			
			if(prob > 0.5) {
				newDemand = (long) (RandomHelper.nextDoubleFromTo(firm.getUsedCapacity()+10, firm.getInstalledCapacity()));
				firm.setInventory((long) (firm.getBeta()*newDemand));
				isProp = true ;
			} 
			else {
				newDemand = (long) (RandomHelper.nextDoubleFromTo(1.05, 2)*firm.getInstalledCapacity());
				firm.setInventory((long) (firm.getBeta()*newDemand));
			}
			
			firm.setCurrentSales(newDemand);
			firm.planProduction();
			
			return isProp ;
		}
		
		@Test
		@DisplayName("Number of job offerings")
		void number() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				init(firm);
				assertTrue(firm.getJobOfferings().size() > 0);
			}
		}
		
		@Test
		@DisplayName("Sum of offered wages") 
		void offerings() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				boolean proportional = init(firm);
				double extraCap ; 
				
				if(proportional) {
					extraCap = firm.getDesiredProduction() - firm.getUsedCapacity() ;
				} 
				else {	
					extraCap = firm.getInstalledCapacity() - firm.getUsedCapacity() ;
				}
				
				double offeringsCap = 0 ;
				double minWage = Government.getInstance().getParam(Keys.MIN_WAGE);
				
				ArrayList<JobOffering> offerings = firm.getJobOfferings() ;
				
				if(offerings != null && offerings.size() != 0) {
					for(JobOffering offering : offerings) {
						
						double wages = offering.getWage()/minWage ;
						double maxWages = offering.getMachine().getMaxWages() ;
						double capacity = offering.getMachine().getCapacity() ;
						
						offeringsCap += (wages/maxWages)*capacity ; 
					}
				}
				else {
					fail("Should have created job offerings");
				}
								
				assertTrue(1 - offeringsCap/extraCap <= 1.6e-1);	
			}
		}
		
		@Test
		@DisplayName("Additional payroll") 
		void addtPay() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				init(firm);
				
				double sum = 0 ;
				ArrayList<JobOffering> offerings = firm.getJobOfferings();
				
				if(offerings != null && offerings.size() > 0) {
					for(JobOffering offering : offerings) {
						sum += offering.getWage() ;
					}
					
					assertTrue(firm.getAdditionalPayroll() > 0);
					assertEquals(sum,firm.getAdditionalPayroll());
				}
				else {
					fail("Should have created job offerings.");
				}
				
			}
		}
		
		@Test
		@DisplayName("Assets") 
		void assets() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				
				double oldAssets = firm.getAssets() ;
				init(firm);
				double newAssets = firm.getAssets() ;
			
				assertEquals(oldAssets,newAssets);
			}
		}
	}
	
	@Nested
	@DisplayName("Expansion investment")
	class ExpansionInvestment {
		
		void init(ConsumptionGoodsFirm firm) {
		
			long newDemand = (long) (RandomHelper.nextDoubleFromTo(1.1,2)*firm.getInstalledCapacity());
			firm.setInventory((long) (firm.getBeta()*newDemand));
			firm.setCurrentSales(newDemand);
			firm.planProduction();	
		}
		
		@Test
		@DisplayName("Number of job offerings")
		void number() {
			
			Iterator<Agent> firmItr = firms.iterator() ;
			
			while(firmItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmItr.next() ;
				init(firm);
				
				ArrayList<MachineOrder> orders = firm.getInvestments() ;
				NewMachine repl = null ;
				
				if(firm.getReplacement() != null) {
					repl = firm.getReplacement().getSecond() ;
				} 
				else {
					repl = new NewMachine(0, 0, 0, 0);
				}
				
				double sum = 0 ;
				
				if(orders != null) {
					assertTrue(orders.size() > 0);
				}
				
				for(MachineOrder order : orders) {
					
					if(order.getMachine().equals(repl)) {
						order.decreaseQuantity(1);
					} 
	
					for(int i = 0; i < order.getQuantity(); i++) {
						sum += order.getMachine().getCapacity() ;
					}
				}
				
				double diff = firm.getDesiredProduction() - firm.getInstalledCapacity() ;
				assertTrue(diff > 0);				
				assertTrue(sum >= diff);
			}
		}
		
	}
}
