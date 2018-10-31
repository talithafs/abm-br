package abm;

// -Xmx32g -Xms32g -Xss32g 

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.Government;
import abm.agents.NonFinancialAgent;
import abm.components.Statistics;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.IndexedIterable;

public final class Flow {

	private static Context<Agent> context = null ;
	private static int run = 1 ;
	
	public static void initFlow(Context<Agent> cont) {
		context = cont ;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public static void run() throws NoSuchMethodException, SecurityException {
		
		IndexedIterable<Agent> conFirms = context.getObjects(ConsumptionGoodsFirm.class);
		IndexedIterable<Agent> capFirms = context.getObjects(CapitalGoodsFirm.class);
		IndexedIterable<Agent> firms = context.getObjects(Firm.class);
		IndexedIterable<Agent> consumers = context.getObjects(Consumer.class);
		IndexedIterable<Agent> banks = context.getObjects(Bank.class);
		IndexedIterable<Agent> nonFins = context.getObjects(NonFinancialAgent.class);
	
		Government.getInstance().calculateStatistics(); 
		
		iterate(firms, Firm.class.getMethod("planProduction"));
		ConsumptionGoodsMarket.getInstance().initSelectionFunction(); 
		iterate(consumers, Consumer.class.getMethod("calculateDemand"));
		
		iterate(banks, Bank.class.getMethod("calculateTotalCredit"));
		iterate(nonFins, NonFinancialAgent.class.getMethod("calculateNeededCredit"));
		iterate(banks, Bank.class.getMethod("calculateAvailableCredit"));
		CreditMarket.getInstance().match(banks, nonFins);
			
		iterate(firms, Firm.class.getMethod("postJobOfferings"));
		iterate(firms, Firm.class.getMethod("payEmployees"));
		LaborMarket.getInstance().match();
		
		iterate(conFirms, ConsumptionGoodsFirm.class.getMethod("buyMachines"));
		iterate(conFirms, ConsumptionGoodsFirm.class.getMethod("produceGoods"));
		iterate(capFirms, CapitalGoodsFirm.class.getMethod("innovate"));
		
		ConsumptionGoodsMarket.getInstance().match(conFirms, consumers);
		
		iterate(nonFins, ConsumptionGoodsFirm.class.getMethod("payDebts"));
		iterate(banks, Bank.class.getMethod("calculateMargin"));
		iterate(conFirms, ConsumptionGoodsFirm.class.getMethod("receiveMachines"));
		
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount() == 1) {
			System.out.println(">> New run: " + run++ );
		}
	}
	
	private static void iterate(IndexedIterable<Agent> agents, Method method, Object ... args)  {
		
		Iterator<Agent> itr = agents.iterator() ;
		
		while(itr.hasNext()) {
			try {
				if(args.length == 0) {
					method.invoke(itr.next());
				}
				else {
					method.invoke(itr.next(), args);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
