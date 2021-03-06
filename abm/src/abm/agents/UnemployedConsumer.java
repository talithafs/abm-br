package abm.agents;

import java.util.ArrayList;

import abm.MetaParameters;
import abm.Controller;
import abm.components.JobOffering;
import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;

public class UnemployedConsumer extends Consumer {

	public UnemployedConsumer(double savingPerc, double resWage, double assets, double debt) {
		super(savingPerc, resWage, assets, debt);
		this.payment = 0 ;
	}
	
	public UnemployedConsumer(double savingPerc, double resWage, double assets, double debt, double percWealth, int id) {
		super(savingPerc, resWage, assets, debt, percWealth, id);
		this.payment = 0 ;
	}

	public void adjustReservationWage() {
		Uniform unif = RandomHelper.createUniform(0, MetaParameters.getResWageAdjustment());
		this.resWage *= (1-unif.nextDouble());
	}


	public Pair<Firm, JobOffering> chooseJobOffering() {
		
		JobOffering chosen = null ;	
		ArrayList<Firm> firms  = laborMarket.getRandomFirms(3);
		
		int index = 0 ;
		Firm firm = null ;
		
		if(firms.size() > 0) {
			while(chosen == null && index < firms.size()) {

				firm = firms.get(index);
				ArrayList<JobOffering> offerings = laborMarket.getJobOfferings(firm);
				
				double resWage = this.getResWage() ;
				
				for(JobOffering job : offerings) {
					
					if(job.getWage() >= resWage) {
						chosen = job ;
						break ;
					}
				}
				index++ ;
			}
		}
		
		if(chosen != null) {
			return new Pair<Firm,JobOffering>(firm, chosen) ;
		}
		
		return null ;
	}
}
