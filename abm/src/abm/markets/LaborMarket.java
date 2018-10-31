package abm.markets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import abm.Controller;
import abm.agents.Agent;
import abm.agents.Consumer;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.UnemployedConsumer;
import abm.components.JobOffering;
import abm.links.Job;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.Pair;

public final class LaborMarket extends AbstractMarket<Firm, EmployedConsumer, Job>{

	private static LaborMarket instance = new LaborMarket();
	private HashMap<Firm, ArrayList<JobOffering>> offeringsMap = null ;
	private int totalOfferings = 0 ;
	
	private LaborMarket() { } 
	
	@Override
	public void init(Context<Agent> context, Network<Agent> network) {
		super.init(context, network);
		offeringsMap = new HashMap<Firm, ArrayList<JobOffering>>();
		this.totalOfferings = 0 ;
	}

	public static LaborMarket getInstance() {
		return instance ;
	}
	
	public Firm getEmployer(EmployedConsumer emp) {
		return getAdjacent(emp).get(0);
	}
	
	public Job getJob(EmployedConsumer emp) {
		return getEdges(emp).get(0);
	}
	
	public void postJobOfferings(Firm firm, ArrayList<JobOffering> offerings) {
		
		if(offeringsMap.get(firm) != null) {
			totalOfferings -= offeringsMap.get(firm).size() ;
			offeringsMap.remove(firm);
		}
		
		if(offerings != null && offerings.size() > 0) {
			offeringsMap.put(firm, offerings);
			totalOfferings += offerings.size() ;
		}
		
	}
	
	public ArrayList<JobOffering> getJobOfferings(Firm firm){	
		return offeringsMap.get(firm);
	}
	
	
	public ArrayList<Firm> getRandomFirms(int n) {
		
		int i = 0 ;
		ArrayList<Firm> firms = new ArrayList<Firm>() ;
		int size = offeringsMap.size() ;
		
		if(size < n) {
			n = size ;
		}
		
		if(n > 0 && totalOfferings > 0) {
			
			while(i < n) {
				
				// TODO This is inefficient. Choose only among firms that offered jobs.
				Iterator<Agent> itr = context.getRandomObjects(Firm.class, 1).iterator() ;
				Firm firm = (Firm) itr.next() ;
				
				if(offeringsMap.get(firm) != null && offeringsMap.get(firm).size() > 0) {
					firms.add(firm);
					i++ ;
				}
			}
		}
		
		return firms ;
	}
	
	public void match() {
		
		ArrayList<Consumer> unempCons = getUnemployed();
		SimUtilities.shuffle(unempCons, RandomHelper.getUniform());

		for(Consumer consumer : unempCons) {
			
			Pair<Firm, JobOffering> job = ((UnemployedConsumer) consumer).chooseJobOffering() ;
			
			if(job != null) {
				JobOffering chosen = job.getSecond() ;
				Firm firm = job.getFirst() ;
				
				consumer = firm.hire(consumer, chosen);
				offeringsMap.get(firm).remove(chosen);
				
				if(offeringsMap.get(firm) != null && offeringsMap.get(firm).size() == 0) {
					offeringsMap.remove(firm);
				}
				
				totalOfferings -= 1 ;
			}
			
			consumer.adjustReservationWage();
		}
		
		offeringsMap = new HashMap<Firm, ArrayList<JobOffering>>();
		totalOfferings = 0 ;
	}
	
	private ArrayList<Consumer> getUnemployed(){
		
		Iterable<Agent> itUnemps = context.getObjects(UnemployedConsumer.class);
		ArrayList<Consumer> unemps = new ArrayList<Consumer>() ;
		
		for(Agent unemp : itUnemps) {
			unemps.add((Consumer) unemp);
		}
		
		return unemps ;
	}
}
