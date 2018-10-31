package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProductionTest {

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
	
	void testProd() {
		
		Iterator<Agent> conItr = conFirms.iterator() ;
		
		while(conItr.hasNext()) {
			
			ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) conItr.next();		
			con.produceGoods();
			
			double remaining = con.getRemainingWages() ;
			double lastProd = con.getLastProduction() ;
				
			if(remaining == 0) {
				assertEquals(lastProd, con.getUsedCapacity());
			}
			else {
				assertTrue(lastProd < con.getUsedCapacity());
			}
			
		}
	}
	
	@Test
	@DisplayName("Demand rises")
	void rise() {
		
		TestsHelper.planProduction(firms, false, false);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		TestsHelper.runCapitalGoodsMarket(conFirms);
		testProd();
	}
	
	@Test
	@DisplayName("Random demand")
	void random() {

		TestsHelper.planProduction(firms, true, false);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		TestsHelper.runCapitalGoodsMarket(conFirms);
		testProd();
		
	}
	
	@Test
	@DisplayName("Demand drops")
	void drop() {
		
		TestsHelper.planProduction(firms, false, true);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		TestsHelper.runLaborMarket(firms);
		TestsHelper.runCapitalGoodsMarket(conFirms);
		testProd();
	}
	
	@Test
	@DisplayName("Unpaid workers")
	void remaining() {
		
		TestsHelper.planProduction(firms, false, false);
		TestsHelper.planConsumption(consumers);
		TestsHelper.calculateCredit(banks, nonFins);
		
		Iterator<Agent> conItr = conFirms.iterator() ;
		
		while(conItr.hasNext()) {
			
			ConsumptionGoodsFirm con = (ConsumptionGoodsFirm) conItr.next();
			con.postJobOfferings(); 
			
			Bank bank = CreditMarket.getInstance().getBankWithDeposits(con);
			double assets = con.getAssets() ;
			double payroll = con.getPayroll() ;
			double wd = RandomHelper.nextDoubleFromTo(0, payroll-10);
			bank.withdraw(con, assets - wd);
		
			con.payEmployees();
		}
		
		TestsHelper.setupLaborMarket(context.getObjects(CapitalGoodsFirm.class));
		LaborMarket.getInstance().match();
		TestsHelper.runCapitalGoodsMarket(conFirms);
	
		testProd();
	}

}