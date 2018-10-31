package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.UnemployedConsumer;
import abm.markets.ConsumptionGoodsMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class ConsumerPlanningTest {
	
	protected Context<Agent> context ; 
	protected IndexedIterable<Agent> consumers ;
	protected Iterator<Agent> itrCons ;

	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
		
		consumers = context.getObjects(Consumer.class);
		itrCons = consumers.iterator() ;
		ConsumptionGoodsMarket.getInstance().initSelectionFunction();
	}

	@Test
	@DisplayName("Better price between neighboring firms")
	void price() {
		
		while(itrCons.hasNext()) {
			Consumer consumer = (Consumer) itrCons.next() ;
			consumer.calculateDemand();
			
			ArrayList<ConsumptionGoodsFirm> firms = ConsumptionGoodsMarket.getInstance().getAdjacent(consumer);
			ConsumptionGoodsFirm chosen = consumer.getChosenFirm() ;
			
			for(ConsumptionGoodsFirm firm : firms) {
				if(firm.getPrice() < chosen.getPrice()) {
					fail("Found a better price");
				}
			}

		}
	}
	
	@Test
	@DisplayName("Consistent value to spend")
	void value() {
		
		while(itrCons.hasNext()) {
			Consumer consumer = (Consumer) itrCons.next() ;
			consumer.calculateDemand();
			
			double totalValue = consumer.getTotalValue() ;
			double value ;
			
			if(consumer instanceof UnemployedConsumer) {
				value = consumer.getConsInertia() * (1 - consumer.getSavingPerc()) * consumer.getResWage() ;
			}
			else {
				value = (1 - consumer.getSavingPerc()) * ((EmployedConsumer) consumer).getWage();
			}
			
			assertTrue(totalValue >= value);
			
			double extra = 0 ;
			
			if(totalValue > value) {
				extra = consumer.getNetWorth() * consumer.getPercFromWealth() ;
			}
			
			assertEquals(totalValue, value + extra);

		}
	}

}
