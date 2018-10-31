package abm.markets;

import java.util.ArrayList;
import java.util.Iterator;

import abm.MetaParameters;
import abm.Controller;
import abm.agents.Agent;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.helpers.Utils;
import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public final class ConsumptionGoodsMarket extends AbstractMarket<ConsumptionGoodsFirm, Consumer, RepastEdge<Agent>>{

	private static ConsumptionGoodsMarket instance = new ConsumptionGoodsMarket();
	
	private EmpiricalWalker selectionFunction = null ;
	private IndexedIterable<Agent> currentFirms = null ;	
	
	public static ConsumptionGoodsMarket getInstance() {
		return instance ;
	}
	
	public ConsumptionGoodsFirm selectFirm() {
		return (ConsumptionGoodsFirm) currentFirms.get(selectionFunction.nextInt());
	}
	
	public void initSelectionFunction() {
		
		IndexedIterable<Agent> firms = context.getObjects(ConsumptionGoodsFirm.class);
		Iterator<Agent> itr = firms.iterator() ;
		
		double sum = 0 ;
		double disp = MetaParameters.getInformationDispersion() ;
		
		while(itr.hasNext()) {
			
			ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) itr.next() ;
			
			double price = firm.getPrice() ;
			sum += Math.exp(-price/disp);
		}
		
		double[] probs = new double[firms.size()];
		
		for(int i = 0; i < firms.size(); i++) {
			
			double price = ((ConsumptionGoodsFirm) firms.get(i)).getPrice();
			probs[i] = Math.exp(-price/disp)/sum;
		}
		
		selectionFunction = RandomHelper.createEmpiricalWalker(probs, Empirical.NO_INTERPOLATION);
		currentFirms = firms ;
	}

	public void match(IndexedIterable<Agent> firms, IndexedIterable<Agent> consumers) {
				
		int trials = 3 ;

		while(trials > 0) {
			
			ArrayList<Agent> clients = Utils.shuffle(consumers);
			Iterator<Agent> consItr = clients.iterator() ;
			
			while(consItr.hasNext()) {
				
				Consumer consumer = (Consumer) consItr.next() ;
				
				if(consumer.getDemand() > 0) {
					ConsumptionGoodsFirm firm = consumer.getChosenFirm() ;
					enterQueue(firm, consumer);
				}	
			}
			
			Iterator<Agent> firmsItr = firms.iterator() ;
			
			while(firmsItr.hasNext()) {
				ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmsItr.next() ;
				firm.sellGoods();			
			}
			
			clearQueues();			
			trials-- ;
		}
	}
}
