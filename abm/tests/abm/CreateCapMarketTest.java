package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.Government;
import abm.components.Machine;
import abm.components.NewMachine;
import abm.components.Statistics;
import abm.components.UsedMachine;
import abm.creators.ConsumersCreator;
import abm.creators.FirmsCreator;
import abm.helpers.Utils;
import abm.markets.CapitalGoodsMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;

@SuppressWarnings("unchecked")
class CreateCapMarketTest {

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
		 
		 NetworkBuilder<Agent> capMarketBuilder = new NetworkBuilder<Agent>("capMarket", context, false);
		 capMarketBuilder.buildNetwork();
		 CapitalGoodsMarket.getInstance().init(context, (Network<Agent>) context.getProjection("capMarket"));
		 
		 firmsCreator.createCapital(firms, consumers);
		 
		 Controller ctrl = new Controller();
		 ctrl.createCapitalGoodsMarket(firms);
	 }

	@Test
	@DisplayName("Used machine to new machine correspondence")
	void testUsed() {
		
		ArrayList<UsedMachine> used = new ArrayList<UsedMachine>();
		ArrayList<NewMachine> newm = new ArrayList<NewMachine>();
		
		for(Firm firm : firms) {
			
			if(firm instanceof ConsumptionGoodsFirm) {
				used.addAll(((ConsumptionGoodsFirm) firm).getMachines());
			}
			else {
				newm.addAll(((CapitalGoodsFirm) firm).getCatalog());
			}
		}
		
		for(Machine u : used) {
			
			int sum = 0 ;
			
			for(Machine n : newm) {
				if(n.equals(u)) {
					sum++ ;
				}
			}
			assertEquals(sum,1);
		}
		

		for(Machine n : newm) {
			
			int sum = 0 ;
			
			for(Machine u : used) {
				if(n.equals(u)) {
					sum++ ;
				}
			}
			
			assertTrue(sum >= 1);
		}
	}
	
	@Test
	@DisplayName("Network checking")
	void testNetwork() {
		
		for(Firm firm : firms) {
			
			if(firm instanceof ConsumptionGoodsFirm) {
				
				ArrayList<CapitalGoodsFirm> caps = CapitalGoodsMarket.getInstance().getAdjacent((ConsumptionGoodsFirm) firm);
				ArrayList<UsedMachine> macs = ((ConsumptionGoodsFirm) firm).getMachines() ;
				
				assertTrue(caps.size() >= 1);
				
				for(Machine m : macs) {
					
					boolean contain = false ;
					
					for(CapitalGoodsFirm cap : caps) {
						if(cap.getCatalog().contains(m)) {
							contain = true ;
							break ;
						}
					}
					
					assertTrue(contain);
				}	
			}
		}
	}
}
