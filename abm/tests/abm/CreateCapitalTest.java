package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.Government;
import abm.agents.UnemployedConsumer;
import abm.creators.BanksCreator;
import abm.creators.ConsumersCreator;
import abm.creators.FirmsCreator;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;
import abm.links.Account;
import abm.links.AccountCreator;
import abm.links.Job;
import abm.links.JobCreator;
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings("unchecked")
class CreateCapitalTest {

	private static HashMap<String,Double> params = null ;
	private static ConsumersCreator consumersCreator = null ;
	private static FirmsCreator firmsCreator = null ;
	
	private static ArrayList<Consumer> consumers = null ;
	private static ArrayList<Firm> firms = null ;
	
	private static Context<Agent> context = null ;
	
	 @BeforeAll
	 static void initAll() {
		 
		 MetaParameters.initParameters(); 
		 params = Utils.readParameters();
		 Government.getInstance().setParams(params,null);
		 consumersCreator = new ConsumersCreator(params);
		 firmsCreator = new FirmsCreator(params);
	 }
	 
	@BeforeEach
	 void init() {
		 
		 ArrayList<Integer[]> distFir = Utils.readDistribution("fir") ;
		 ArrayList<Integer[]> distInc = Utils.readDistribution("inc") ;
		
		 consumers = consumersCreator.create(distInc);
		 firms = firmsCreator.create(distFir);
		
		 context = new DefaultContext<Agent>();
		 context.setId("abm");
		 context.addAll(consumers);
		 context.addAll(firms);
		 
		 NetworkBuilder<Agent> jobsBuilder = new NetworkBuilder<Agent>("jobs",context,false);
		 jobsBuilder.setEdgeCreator(new JobCreator());
		 jobsBuilder.buildNetwork() ;
		 LaborMarket.getInstance().init(context, (Network<Agent>) context.getProjection("jobs")) ;
		 
		 firmsCreator.createCapital(firms, consumers);
	 }
	 
	 @Nested
	 @DisplayName("Machines creation")
	 @TestInstance(Lifecycle.PER_CLASS)
	 class MachinesCreation {
		
		 @Test
		 @DisplayName("Number of machines")
		 void testNumber() {
			 
			for(Firm firm : firms) {
				if(firm instanceof ConsumptionGoodsFirm) {
					assertEquals(firm.getNEmployees(), ((ConsumptionGoodsFirm) firm).getMachines().size());
				}	
			}
		 }
		 
		 @Test
		 @DisplayName("Ratio used capacity/installed capacity")
		 void testCapacity() {
			 
			 double sum = 0 ;
			 
			 for(Firm firm : firms) {
				if(firm instanceof ConsumptionGoodsFirm) {
					
					double installed = ((ConsumptionGoodsFirm) firm).getInstalledCapacity() ;
					double used = ((ConsumptionGoodsFirm) firm).getUsedCapacity() ;
					
					sum += used/installed ;
				}	
			}
			 
			 double ratio1 = sum/Government.getInstance().getParam(Keys.N_CON_FIRMS) ;
			 double ratio2 = Government.getInstance().getParam(Keys.USED_CAP);

			 assertTrue(Math.abs(ratio1 - ratio2) <= 0.03);
		}
		 
		 @Test
		 @DisplayName("Used capacity and and production equivalence")
		 void testDemand() {
			 
			 for(Firm firm : firms) {

				 if(firm instanceof ConsumptionGoodsFirm) {
					 
					 double used = ((ConsumptionGoodsFirm) firm).getUsedCapacity();
					 double prod = ((ConsumptionGoodsFirm) firm).getLastProduction();
					 
					 assertEquals(used, prod);
				 }
			 }
		 }
		 
		 @Test
		 @DisplayName("Consistency between used capital, production, supply and demand")
		 void test() {
			 
			 for(Firm firm : firms) {

				 if(firm instanceof ConsumptionGoodsFirm) {
					 
					 double supply = ((ConsumptionGoodsFirm) firm).getLastSupply() ;
					 double usedCap = ((ConsumptionGoodsFirm) firm).getUsedCapacity();
					 double production = ((ConsumptionGoodsFirm) firm).getLastProduction();
					 double demand = ((ConsumptionGoodsFirm) firm).getCurrentSales();
					 
					 //assertEquals(inventory + production, supply);
					 assertEquals(production, usedCap);
					 assertTrue(Math.abs(demand - supply) <= 1);
				 }
			 }
		 }
				 
		 
	 }
	 
	 @Nested
	 @DisplayName("Jobs creation")
	 @TestInstance(Lifecycle.PER_CLASS)
	 class JobsCreation {
		 
		 @Test
		 @DisplayName("Consistency number of jobs/employees")
		 void testNumber() {
			 
			 for(Firm firm : firms) {
				 ArrayList<Job> jobs = LaborMarket.getInstance().getEdges(firm);
				 assertEquals(jobs.size(),firm.getNEmployees());
			 }
		 }
		 
		 @Test
		 @DisplayName("Equivalency employees wage/job wage")
		 void testWage() {
			 
			 for(Firm firm : firms) {

				 ArrayList<EmployedConsumer> emps = LaborMarket.getInstance().getAdjacent(firm);
				 
				 for(EmployedConsumer emp : emps) {
					 ArrayList<Job> jobs = LaborMarket.getInstance().getEdges(emp);
					 assertEquals(jobs.size(), 1);
					 assertEquals(jobs.get(0).getWage(), emp.getWage());
				 }
			 }
		 }
	 }

}
