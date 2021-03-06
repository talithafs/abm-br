package abm.agents;

import java.util.HashMap;

import abm.components.Statistics;

public class Government extends Agent {

	private static Government instance = new Government();
	private Statistics stats = null ;
	private HashMap<String,Double> params = null ;
	
	private double unempRate ;
	private double empRate ;
	private double meanUnempRate ;
	private double vacanRatio ;
	
	private double gdpDev ;
	private double gdpGrowth ;
	private double accumGdp ;
	private double gdp ;
	private double meanGdp ;
	
	private double inflation ;
	private double accumInflation ;
	
	public void setParams(HashMap<String,Double> params, Statistics stats) {
		
		if(this.params == null) {
			this.params =  params ;
			this.stats = stats ;
		}
	}
	
	public int getCount(Class<? extends Agent> cls) {
		return stats.getCount(cls);
	}

	public void calculateStatistics() {
		
		unempRate = stats.calculateUnempRate() ;
		empRate = 1 - unempRate ;
		meanUnempRate = stats.getLastMeanUnempRate();
		vacanRatio = stats.calculateVacanRatio() ;
		
		gdp = stats.calculateGdp() ;
		gdpDev = stats.getLastGdpDev() ;
		gdpGrowth = stats.getLastGdpGrowth() ;
		accumGdp = stats.getLastAccumGdpGrowth();
		meanGdp = stats.getLastMeanGdpGrowth();
		
		inflation = stats.calculateInflation() ;
		accumInflation = stats.getLastAccumInflation();
	}
	
	public double getUnempRate() {
		return unempRate;
	}

	public double getEmpRate() {
		return empRate;
	}

	public double getMeanUnempRate() {
		return meanUnempRate;
	}

	public double getVacanRatio() {
		return vacanRatio;
	}

	public double getGdpDev() {
		return gdpDev;
	}

	public double getGdpGrowth() {
		return gdpGrowth;
	}

	public double getAccumGdp() {
		return accumGdp;
	}

	public double getGdp() {
		return gdp;
	}

	public double getMeanGdp() {
		return meanGdp;
	}

	public double getInflation() {
		return inflation;
	}

	public double getAccumInflation() {
		return accumInflation;
	}

	public double getParam(String key) {
		return params.get(key);
	}
	
	public String getDum() {
		return "" ;
	}
	    
	private Government() { } 
	
	public static Government getInstance() {
		return instance ;
	}
	
	

}
