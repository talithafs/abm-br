package abm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.NonFinancialAgent;
import abm.components.MachineOrder;
import abm.components.NewMachine;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CapitalMarketTest {

	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> firms ;
	protected IndexedIterable<Agent> banks ;
	protected IndexedIterable<Agent> nonFins ;
	protected IndexedIterable<Agent> conFirms ;
	protected IndexedIterable<Agent> capFirms ;
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
		capFirms = context.getObjects(CapitalGoodsFirm.class);
		consumers = context.getObjects(Consumer.class);
	}
	
	void testMarket() {
		
		Iterator<Agent> conItr = conFirms.iterator() ;
		Iterator<Agent> capItr = capFirms.iterator() ;
		HashMap<ConsumptionGoodsFirm, Double> conAssets = new HashMap<ConsumptionGoodsFirm, Double>();
		HashMap<CapitalGoodsFirm, Double> capAssets = new HashMap<CapitalGoodsFirm, Double>();
		
		while(conItr.hasNext()) {
			ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) conItr.next();
			conAssets.put(con,con.getAssets());
		}
		
		while(capItr.hasNext()) {
			CapitalGoodsFirm cap = (CapitalGoodsFirm) capItr.next();
			capAssets.put(cap,cap.getAssets());
		}
		
		conItr = conFirms.iterator() ;
		
		while(conItr.hasNext()) {
			
			ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) conItr.next();
			con.buyMachines();
			
			ArrayList<MachineOrder> orders = con.getInvestments() ;
			double value = 0 ;
			for(MachineOrder order : orders) {
				
				ArrayList<NewMachine> catalog = order.getCapFirm().getCatalog() ;
				assertTrue(catalog.contains(order.getMachine()), "Catalog contains sold machine");
				int index = catalog.indexOf(order.getMachine()) ;
				assertTrue(catalog.get(index).getUnits() > 1, "Units of sold machines were increased");
				
				value += order.getMachine().getPrice();
			}
			
			double oldAssets = conAssets.get(con) ;
			double newAssets = con.getAssets() ;
			double diff = oldAssets - newAssets ;
			assertTrue(diff >= 0, "ConFirm assets decreased or remained the same");
			assertTrue(Math.abs(diff - value) < 1e-5, "ConFirm assets varied according to value spent");
		}
		
		capItr = capFirms.iterator() ;
				
		while(capItr.hasNext()) {
			CapitalGoodsFirm cap = (CapitalGoodsFirm) capItr.next();
			double oldAssets = capAssets.get(cap) ;
			double newAssets = cap.getAssets() ;
			double diff = newAssets - oldAssets ;
			assertTrue(diff >= 0, "CapFirm assets increased or remained the same");
			
			ArrayList<NewMachine> machines = cap.getCatalog() ;
			double value = 0 ;
			for(NewMachine mac : machines) {
				int sold = mac.getUnits() - 1 ;
				value += sold*mac.getPrice() ;
			}
			assertTrue(Math.abs(diff - value) < 1e-5, "CapFirm assets varied according to value sold");
		}		
	}
	
	@Test
	@DisplayName("Demand rises")
	void rise() {
		
		TestsHelper.planProduction(firms, false, false);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		testMarket();
	}
	
	@Test
	@DisplayName("Random demand")
	void random() {
		
		TestsHelper.planProduction(firms, true, false);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		testMarket();
	}
	
	@Test
	@DisplayName("Demand drops")
	void drop() {
		
		TestsHelper.planProduction(firms, false, true);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		testMarket();
	}
	
}

