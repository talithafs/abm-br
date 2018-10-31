package abm;

import java.util.Iterator;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.IndexedIterable;

public class TestsHelper {

	public static void planProduction(IndexedIterable<Agent> firms, boolean random, boolean fire) {
		
		Iterator<Agent> firmsItr = firms.iterator() ;
		
		while(firmsItr.hasNext()) {
			Firm firm = (Firm) firmsItr.next() ;
			
			double prob = RandomHelper.createUniform(0, 1).nextDouble() ;
			
			if(firm instanceof ConsumptionGoodsFirm) {
				ConsumptionGoodsFirm conFirm = (ConsumptionGoodsFirm) firm ;
				long newDemand ;
				
				if(!random) {
					if(fire) {
						newDemand = (long) RandomHelper.nextDoubleFromTo(0, conFirm.getUsedCapacity()-10);
					}
					else {
						if(prob > 0.5) {	
							newDemand = (long) (RandomHelper.nextDoubleFromTo(conFirm.getUsedCapacity()+10, conFirm.getInstalledCapacity()));
						} 
						else {
							newDemand = (long) (RandomHelper.nextDoubleFromTo(1.01, 2)*conFirm.getInstalledCapacity());
						}
					}
				}
				else {
					newDemand = (long) (RandomHelper.nextDoubleFromTo(0, 2)*conFirm.getCurrentSales());
				}

				conFirm.setInventory((long) (conFirm.getBeta()*newDemand));
				conFirm.setCurrentSales(newDemand);
			}
			else if(firm instanceof CapitalGoodsFirm) {
				CapitalGoodsFirm capFirm = (CapitalGoodsFirm) firm ;
				
				if(!random) {
					if(fire) {
						firm.setIncome(RandomHelper.nextDoubleFromTo(0, capFirm.getIncome() - 10));
					}
					else {
						firm.setIncome(RandomHelper.nextDoubleFromTo(1.01, 2)*capFirm.getIncome());
					}
				}
				else {
					firm.setIncome(RandomHelper.nextDoubleFromTo(0, 2)*capFirm.getIncome());
				}
			}
			
			firm.planProduction(); 
			firm.calculateNeededCredit();
		}
	}
	
	public static void planProduction(IndexedIterable<Agent> firms) {
		
		Iterator<Agent> itr = firms.iterator() ;
		
		while(itr.hasNext()) {
			Firm firm = (Firm) itr.next() ;
			firm.planProduction(); 
			firm.calculateNeededCredit();
		}
	}
	
	public static void planConsumption(IndexedIterable<Agent> consumers) {
		
		ConsumptionGoodsMarket.getInstance().initSelectionFunction(); 
		
		Iterator<Agent> itr = consumers.iterator() ;
		
		while(itr.hasNext()) {
			Consumer con = (Consumer) itr.next() ;
			con.calculateDemand();
		}
	}
	
	public static void calculateCredit(IndexedIterable<Agent> banks, IndexedIterable<Agent> nonFins) {
		
		Iterator<Agent> banksItr = banks.iterator() ;
		
		while(banksItr.hasNext()) {
			Bank bank = (Bank) banksItr.next() ;
			bank.calculateTotalCredit(); 
			bank.calculateAvailableCredit(); 
			CreditMarket.getInstance().match(banks, nonFins);
		}
	}
	
	public static void setupLaborMarket(IndexedIterable<Agent> firms) {
		
		Iterator<Agent> firmsItr = firms.iterator() ;
		
		while(firmsItr.hasNext()) {
			Firm firm = (Firm) firmsItr.next() ;
			firm.postJobOfferings(); 		
			firm.payEmployees();
		}
	}
	
	public static void runLaborMarket(IndexedIterable<Agent> firms) {
		
		setupLaborMarket(firms);
		LaborMarket.getInstance().match(); 
	}
	
	public static void runCapitalGoodsMarket(IndexedIterable<Agent> firms) {
		
		Iterator<Agent> firmsItr = firms.iterator() ;
		
		while(firmsItr.hasNext()) {
			ConsumptionGoodsFirm firm = (ConsumptionGoodsFirm) firmsItr.next() ;
			firm.buyMachines();
		}
	}
	
	public static void runProduction(IndexedIterable<Agent> firms) {
		
		Iterator<Agent> firmsItr = firms.iterator() ;
		
		while(firmsItr.hasNext()) {
			
			Firm firm = (Firm) firmsItr.next() ;
			
			if(firm instanceof ConsumptionGoodsFirm) {
				((ConsumptionGoodsFirm) firm).produceGoods(); 
			}
			else {
				((CapitalGoodsFirm) firm).innovate();
			}
		}
	}

}
