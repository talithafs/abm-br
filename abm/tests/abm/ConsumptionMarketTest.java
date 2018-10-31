package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.NonFinancialAgent;
import abm.markets.ConsumptionGoodsMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class ConsumptionMarketTest {

	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> firms ;
	protected IndexedIterable<Agent> banks ;
	protected IndexedIterable<Agent> nonFins ;
	protected IndexedIterable<Agent> conFirms ;
	protected IndexedIterable<Agent> consumers ;
	
	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		firms = context.getObjects(Firm.class);
		nonFins = context.getObjects(NonFinancialAgent.class) ;
		banks = context.getObjects(Bank.class);
		conFirms = context.getObjects(ConsumptionGoodsFirm.class);
		consumers = context.getObjects(Consumer.class);		
		
		TestsHelper.planProduction(firms);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		TestsHelper.runCapitalGoodsMarket(conFirms);
		TestsHelper.runProduction(firms);	
	}
	
	@Test
	@DisplayName("Sold quantity is equal to bought quantity")
	void quantities() {
		
		ConsumptionGoodsMarket.getInstance().match(conFirms, consumers);
		
		Iterator<Agent> itr = conFirms.iterator() ;
		double sold = 0 ;
		while(itr.hasNext()) {
			ConsumptionGoodsFirm conFirm = (ConsumptionGoodsFirm) itr.next() ;
			sold += conFirm.getCurrentSales() ;
		}
		
		itr = consumers.iterator() ;
		double bought = 0 ; 
		while(itr.hasNext()) {
			Consumer con = (Consumer) itr.next() ;
			bought += con.getBoughtQty() ;
		}
		
		assertEquals(bought, sold);
	}
	
	@Test
	@DisplayName("Spent value less or equal to total value available to spend")
	void match() {
		
		ConsumptionGoodsMarket.getInstance().match(conFirms, consumers);
		
		Iterator<Agent> itr = consumers.iterator() ;
		while(itr.hasNext()) {
			Consumer con = (Consumer) itr.next() ;
			assertTrue(con.getValueSpent() <= con.getTotalValue());
		}
	}
	
	@Test
	@DisplayName("Sales is not greater than production plus inventory")
	void each() {
		
		ConsumptionGoodsMarket.getInstance().match(conFirms, consumers);
		
		Iterator<Agent> itr = conFirms.iterator() ; 
		
		while(itr.hasNext()) {
			ConsumptionGoodsFirm conFirm = (ConsumptionGoodsFirm) itr.next() ;
			assertEquals(conFirm.getLastSupply(), conFirm.getLastProduction() + conFirm.getInventory());
			assertTrue(conFirm.getCurrentSales() <= conFirm.getLastSupply());
		}
	}
	
	@Test
	@DisplayName("Consumers assets vary according to value spent")
	void conExpend() {
		
		Iterator<Agent> itr = consumers.iterator() ; 
		HashMap<Consumer, Double> mapAssets = new HashMap<Consumer, Double>();
		
		while(itr.hasNext()) {
			Consumer con = (Consumer) itr.next();
			mapAssets.put(con, con.getAssets());
		}
		
		ConsumptionGoodsMarket.getInstance().match(conFirms, consumers);
		
		itr = consumers.iterator() ; 
		
		while(itr.hasNext()) {
			Consumer con = (Consumer) itr.next();
			double value = con.getValueSpent() ;
			double newAssets = con.getAssets() ;
			double oldAssets = mapAssets.get(con);

			double diff = oldAssets - newAssets ;
			assertTrue(diff >= 0);
			assertTrue(Math.abs(value - diff) < 1e-10);
		}
	}
	
	@Test
	@DisplayName("Firms assets vary according to value sold")
	void firmExpend() {
		
		Iterator<Agent> itr = conFirms.iterator() ; 
		HashMap<ConsumptionGoodsFirm, Double> mapAssets = new HashMap<ConsumptionGoodsFirm, Double>();
		
		while(itr.hasNext()) {
			ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) itr.next();
			mapAssets.put(con, con.getAssets());
		}
		
		ConsumptionGoodsMarket.getInstance().match(conFirms, consumers);
		
		itr = conFirms.iterator() ; 
		
		while(itr.hasNext()) {
			ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) itr.next();
			double value = con.getCurrentSales()*con.getPrice() ;
			double newAssets = con.getAssets() ;
			double oldAssets = mapAssets.get(con);
			
			double diff = newAssets - oldAssets ;
			assertTrue(diff >= 0);
			assertTrue(Math.abs(value - diff) < 1e-5);
		}
	}
	
	
}
