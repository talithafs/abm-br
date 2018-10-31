package abm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.Government;
import abm.creators.ConsumersCreator;
import abm.creators.FirmsCreator;
import abm.helpers.Utils;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;

@SuppressWarnings("unchecked")
class CreateConMarketTest {

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
		 jobsBuilder.buildNetwork() ;
		 LaborMarket.getInstance().init(context, (Network<Agent>) context.getProjection("jobs")) ;
		 
		 NetworkBuilder<Agent> conMarketBuilder = new NetworkBuilder<Agent>("conMarket", context, false);
		 conMarketBuilder.buildNetwork();
		 ConsumptionGoodsMarket.getInstance().init(context, (Network<Agent>) context.getProjection("conMarket"));
		 
		 firmsCreator.createCapital(firms, consumers);
		 
		 Controller ctrl = new Controller();
		 ctrl.createConsumptionGoodsMarket(firms, consumers);
	 }
	
	 @Test
	 @DisplayName("Initial Demand")
	 void initial() {
		 
		 double firDemand = 0 ;
		 
		 for(Firm firm : firms) {
			 if(firm instanceof ConsumptionGoodsFirm) {
				 ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) firm ;
				 firDemand += con.getCurrentSales()*con.getPrice() ;
			 }
		 }
		 
		 double conDemand = 0 ;
		 
		 for(Consumer consumer : consumers) {
			 conDemand += consumer.getValueSpent() ;
		 }
		 
		 assertTrue(Math.abs(1 - firDemand/conDemand) < 1e-2);
	 }
	
	 @Test
	 @DisplayName("Sum") 
	 void testSum() {
		 
		 double totalCon = 0 ;
		 double totalFir = 0 ;
		 
		 ArrayList<Consumer> evaluated = new ArrayList<Consumer>();
		 
		 for(Firm firm : firms) {
			
			if(firm instanceof ConsumptionGoodsFirm) {
				
				long qty = ((ConsumptionGoodsFirm) firm).getLastSupply() ;
				double price = ((ConsumptionGoodsFirm) firm).getPrice() ;
				double valueFir = qty * price ;
				totalFir += valueFir ;
				
				double valueCons = 0 ;
				ArrayList<Consumer> cons = ConsumptionGoodsMarket.getInstance().getAdjacent((ConsumptionGoodsFirm) firm);
				
				for(Consumer con : cons) {
					
					if(!evaluated.contains(con)) {
						totalCon += con.getValueSpent() ; 
						evaluated.add(con);
					}
					
					valueCons += con.getValueSpent() ;
				}
		
				//assertTrue(valueCons >= valueFir);
			}
		}
		
		 assertTrue(Math.abs(1 - totalFir/totalCon) < 1e-2);
	 }

}
