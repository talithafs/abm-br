package abm.markets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import abm.Controller;
import abm.agents.Agent;
import abm.agents.CapitalGoodsFirm;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.NonFinancialAgent;
import abm.components.MachineOrder;
import repast.simphony.space.graph.RepastEdge;

public final class CapitalGoodsMarket extends AbstractMarket<CapitalGoodsFirm, ConsumptionGoodsFirm, RepastEdge<Agent>> {

	private static CapitalGoodsMarket instance = new CapitalGoodsMarket();
	
	private CapitalGoodsMarket() { } 
	
	public static CapitalGoodsMarket getInstance() {
		return instance ;
	}
	
	public ArrayList<CapitalGoodsFirm> getRandomFirms(ConsumptionGoodsFirm firm, int n){

		int i = 0 ;
		int trials = 5 ;
		
		ArrayList<CapitalGoodsFirm> randomCaps = new ArrayList<CapitalGoodsFirm>() ;
		ArrayList<CapitalGoodsFirm> caps = getAdjacent(firm);
		
		while(i < n && trials > 0) {
			
			Iterator<Agent> itr = context.getRandomObjects(CapitalGoodsFirm.class, 1).iterator() ;
			CapitalGoodsFirm cap = (CapitalGoodsFirm) itr.next() ;
			
			if(!caps.contains(cap)) {
				randomCaps.add(cap);
				i++ ;
			}
			else {
				trials-- ;
			}
		}
	
		return randomCaps ;
	}
	
	
	public double executeSales(ConsumptionGoodsFirm conFirm, ArrayList<MachineOrder> orders) {
		
		double totalValue = 0 ;
		
		for(MachineOrder order : orders) {
			
			CapitalGoodsFirm capFirm = order.getCapFirm() ;
			capFirm.sellMachine(order.getMachine(), conFirm);
			totalValue += order.getMachine().getPrice() ;
			addEdge(capFirm, conFirm);
		}
		
		return totalValue ;
	}
	
}
