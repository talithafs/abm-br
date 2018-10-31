package abm.components;

import java.util.ArrayList;
import java.util.Iterator;

import abm.Controller;
import abm.agents.Agent;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.UnemployedConsumer;
import repast.simphony.context.Context;
import repast.simphony.util.collections.IndexedIterable;

public final class Statistics {

	private static Statistics instance = new Statistics();
	
	private ArrayList<Double> unempRate = null;
	private ArrayList<Double> vacanRatio = null;
	private ArrayList<Double> inflation = null;
	private ArrayList<Double> priceLevel = null ;
	private ArrayList<Double> gdp = null ;
	private ArrayList<Double> gdpGrowth = null ;
	private ArrayList<Double> meanUnempRate = null;
	private ArrayList<Double> accumGdpGrowth = null ;
	private ArrayList<Double> meanGdpGrowth = null ;
	private ArrayList<Double> gdpDev = null ;
	private ArrayList<Double> accumInflation = null;
	
	private int ma = 3 ;
	
	private Context<Agent> context = null ;
	
	private Statistics() {} 
	
	public static Statistics getInstance() {
		return instance ;
	}
	
	public void init(Context<Agent> context) {
		this.context = context ;
	}
	
	public double calculateUnempRate() {
		
		double nUnemps = context.getObjects(UnemployedConsumer.class).size();
		double nCons = context.getObjects(Consumer.class).size();
		
		if(unempRate == null) {
			unempRate = new ArrayList<Double>();
			meanUnempRate = new ArrayList<Double>();
		}
		
		double last = nUnemps/nCons ;
		unempRate.add(last);
		
		if(unempRate.size() == ma) {
			double sum = 0 ;
			for(double rate : unempRate) {
				sum += rate ;
			}
			meanUnempRate.add(sum/ma);
		}
		else if(unempRate.size() > ma) {
			double oldMean = meanUnempRate.get(meanUnempRate.size() - 1);
			double first = unempRate.get(unempRate.size() - 1 - ma);
			double newMean = ((ma*oldMean - first) + last)/ma ;
			meanUnempRate.add(newMean);
		}
		else {
			meanUnempRate.add(0.0);
		}
		
		return last;
	}

	public double calculateGdp() {
		
		if(gdp == null) {
			gdp = new ArrayList<Double>();
			gdpGrowth = new ArrayList<Double>();
			meanGdpGrowth = new ArrayList<Double>();
			accumGdpGrowth = new ArrayList<Double>();
			gdpDev = new ArrayList<Double>();
		}
		
		IndexedIterable<Agent> firms = context.getObjects(Firm.class);
		Iterator<Agent> itr = firms.iterator() ;
		
		double last = 0 ;
		while(itr.hasNext()) {
			Firm firm = (Firm) itr.next() ;
			last += firm.getIncome() ;
		}
		
		gdp.add(last);
		
		double growth = 0 ;
		if(gdp.size() >= 2) {
			int index = gdp.size() - 1 ;
			growth = (gdp.get(index) - gdp.get(index - 1))/gdp.get(index-1);
			
		}
		
		gdpGrowth.add(growth);
		
		double mean = 0 ;
		if(gdpGrowth.size() == ma) {
			double sum = 0 ;
			for(double grt : gdpGrowth) {
				sum += grt ;
			}
			mean = sum/ma ;
		}
		else if(gdpGrowth.size() > ma) {
			double oldMean = meanGdpGrowth.get(meanGdpGrowth.size() - 1);
			double first = gdpGrowth.get(gdpGrowth.size() - 1 - ma);
			mean = ((ma*oldMean - first) + growth)/ma ;
		}
		
		meanGdpGrowth.add(mean);
		gdpDev.add(growth - mean);
		
		if(gdpGrowth.size() >= 12) {
			int firstInx = gdpGrowth.size() - 1 ;
			int lastInx = gdpGrowth.size() - 12 ;
			
			double sum = 0 ;
			for(int i = lastInx; i <= firstInx; i++) {
				sum += gdpGrowth.get(i);
			}
			accumGdpGrowth.add(sum);
		} 
		else {
			accumGdpGrowth.add(0.0);
		}
		
		return last;
	}

	public double calculateVacanRatio() {
		
		if(vacanRatio == null) {
			vacanRatio = new ArrayList<Double>();
		}
		
		double nEmps = context.getObjects(EmployedConsumer.class).size();
		
		IndexedIterable<Agent> firms = context.getObjects(Firm.class);
		Iterator<Agent> itr = firms.iterator() ;
		
		double vacan = 0 ;
		while(itr.hasNext()) {
			Firm firm = (Firm) itr.next() ;
			ArrayList<JobOffering> offerings = firm.getJobOfferings() ;
			
			if(offerings != null) {
				for(JobOffering offering : offerings) {
					if(!offering.getTaken()) {
						vacan++ ;
					}
				}
			}
		}
		
		double last = vacan/nEmps ;
		vacanRatio.add(last);
		
		return last;
	}

	public double calculateInflation() {
		
		if(priceLevel == null) {
			priceLevel = new ArrayList<Double>();
			inflation = new ArrayList<Double>();
			accumInflation = new ArrayList<Double>();
		}
		
		IndexedIterable<Agent> firms = context.getObjects(ConsumptionGoodsFirm.class);
		double sum = 0 ;
		
		for(Agent firm : firms) {
			sum += ((ConsumptionGoodsFirm) firm).getPrice() ;
		}
		
		priceLevel.add(sum/firms.size());
		
		double last = 0 ;
		
		if(priceLevel.size() > 1) {
			int inx = priceLevel.size() - 1 ;
			last = (priceLevel.get(inx) - priceLevel.get(inx-1))/priceLevel.get(inx-1);
		}
		
		inflation.add(last);
		
		if(inflation.size() >= 12) {
			int firstInx = inflation.size() - 1 ;
			int lastInx = inflation.size() - 12 ;
			
			double accum = 0 ;
			for(int i = lastInx; i <= firstInx; i++) {
				accum += inflation.get(i);
			}
			
			accumInflation.add(accum);
		}
		else {
			accumInflation.add(0.0);
		}
		
		return last ;
	}
	
	public ArrayList<Double> getUnempRateSeries() {
		return unempRate;
	}

	public ArrayList<Double> getGdpSeries() {
		return gdp;
	}
	
	public ArrayList<Double> getGdpGrowthSeries() {
		return gdpGrowth;
	}

	public ArrayList<Double> getAccumGdpGrowthSeries() {
		return accumGdpGrowth;
	}
	
	public ArrayList<Double> getMeanGdpGrowthSeries() {
		return meanGdpGrowth;
	}
	
	public ArrayList<Double> getGdpDevSeries() {
		return gdpDev ;
	}

	public ArrayList<Double> getVacanRatioSeries() {
		return vacanRatio;
	}

	public ArrayList<Double> getInflationSeries() {
		return inflation;
	}

	public ArrayList<Double> getMeanUnempRateSeries() {
		return meanUnempRate;
	}

	public ArrayList<Double> getAccumInflationSeries() {
		return accumInflation;
	}

	public double getLastUnempRate() {
		return unempRate.get(unempRate.size() - 1);
	}

	public double getLastGdp() {
		return gdp.get(gdp.size() - 1);
	}

	public double getLastAccumGdpGrowth() {
		return accumGdpGrowth.get(accumGdpGrowth.size() - 1);
	}
	
	public double getLastMeanGdpGrowth() {
		return meanGdpGrowth.get(meanGdpGrowth.size() - 1);
	}
	
	public double getLastGdpGrowth() {
		return gdpGrowth.get(gdpGrowth.size() - 1);
	}
	
	public double getLastGdpDev() {
		return gdpDev.get(gdpDev.size() - 1);
	}

	public double getLastVacanRatio() {
		return vacanRatio.get(vacanRatio.size() - 1);
	}

	public double getLastInflation() {
		return inflation.get(inflation.size() - 1);
	}

	public double getLastMeanUnempRate() {
		return meanUnempRate.get(meanUnempRate.size() - 1);
	}

	public double getLastAccumInflation() {
		return accumInflation.get(accumInflation.size() - 1);
	}
	
	public int getLastNEmployees() {
		return context.getObjects(EmployedConsumer.class).size() ;
	}
	
	public int getCount(Class<? extends Agent> cls) {
		return context.getObjects(cls).size() ;
	}
	
	public int dum() {
		return gdp.size() ;
	}
	
}
