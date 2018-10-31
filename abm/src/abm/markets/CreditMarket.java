package abm.markets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import abm.Controller;
import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.Firm;
import abm.agents.NonFinancialAgent;
import abm.components.CreditOffering;
import abm.components.Loan;
import abm.helpers.Utils;
import abm.links.Account;
import repast.simphony.util.collections.IndexedIterable;

public final class CreditMarket extends AbstractMarket<Bank,NonFinancialAgent,Account>{
	
	private static CreditMarket instance = new CreditMarket();
	private HashMap<NonFinancialAgent, Bank> chosenBanks;
	private HashMap<NonFinancialAgent, ArrayList<CreditOffering>> offeringsMap = 
									new HashMap<NonFinancialAgent, ArrayList<CreditOffering>>() ;
	
	private CreditMarket() { } 
	
	public static CreditMarket getInstance() {
		return instance ;
	}
	
	public void executeTransaction(Firm firm, NonFinancialAgent nonFin, double amount) {
		Bank source = getBankWithDeposits(nonFin);
		Bank target = getBankWithDeposits(firm) ;
		source.withdraw(nonFin, amount);
		target.deposit(firm, amount);
	}
	
	public Bank getBankWithDeposits(NonFinancialAgent agent) {
		
		ArrayList<Account> accounts = getEdges(agent);
		
		for(Account account : accounts) {
			if(account.getDeposits() != 0) {
				return (Bank) account.getSource();
			}
		}
		
		if(accounts != null && accounts.size() > 0) {
			return (Bank) accounts.get(0).getSource();
		}
		
		return null ;
	}
	
	public LinkedList<Loan> getLoans(NonFinancialAgent agent){
		
		ArrayList<Account> accounts = getEdges(agent);
		LinkedList<Loan> loans = new LinkedList<Loan>();
		
		for(Account account : accounts) {
			LinkedList<Loan> current = account.getLoans() ;
			
			if(current != null && current.size() != 0) {
				loans.addAll(account.getLoans());
			}
		}
		
		Collections.sort(loans);
		return loans ;
	}
	
	public ArrayList<NonFinancialAgent> getRandomAgents(Bank bank, int n){

		int i = 0 ;
		int trials = 5 ;
		
		ArrayList<NonFinancialAgent> randomAgents = new ArrayList<NonFinancialAgent>() ;
		ArrayList<NonFinancialAgent> holders = getAdjacent(bank);
		
		while(i < n && trials > 0) {
			
			Iterator<Agent> itr = context.getRandomObjects(NonFinancialAgent.class, 1).iterator() ;
			NonFinancialAgent agent = (NonFinancialAgent) itr.next() ;
			
			if(!holders.contains(agent)) {
				randomAgents.add(agent);
				i++ ;
			}
			else {
				trials-- ;
			}
		}
	
		return randomAgents ;
	}
	
	public void postCreditOffering(NonFinancialAgent agent, CreditOffering offering) {
		
		if(offeringsMap.get(agent) == null) {
			offeringsMap.put(agent, new ArrayList<CreditOffering>());
		}
		
		offeringsMap.get(agent).add(offering);
	}
	
	public ArrayList<CreditOffering> getCreditOfferings(NonFinancialAgent agent){
		return offeringsMap.get(agent);
	}
	
	public ArrayList<CreditOffering> getCreditOfferings(Bank bank){
		
		Set<NonFinancialAgent> nonFins = offeringsMap.keySet() ;
		ArrayList<CreditOffering> offerings = new ArrayList<CreditOffering>();
		
		for(NonFinancialAgent nonFin : nonFins) {
			
			ArrayList<CreditOffering> nonFinOffs = getCreditOfferings(nonFin);
			
			if(nonFinOffs != null) {
				
				for(CreditOffering off : nonFinOffs) {
					
					if(off.getBank().equals(bank)) {
						offerings.add(off);
					}
				}
			}
		}
		
		return offerings ;
	}
	
	public CreditOffering getCreditOffering(NonFinancialAgent agent, Bank bank){
		
		ArrayList<CreditOffering> offerings = offeringsMap.get(agent);
		
		for(CreditOffering offering : offerings) {
			if(offering.getBank() == bank) {
				return offering ;
			}
		}
		
		return null ;
	}

	
	public void match(IndexedIterable<Agent> banks, IndexedIterable<Agent> agents) {
		
		ArrayList<Agent> shuffled = Utils.shuffle(agents);
		chosenBanks = new HashMap<NonFinancialAgent, Bank>();
	
		for(Agent agent : shuffled) {
			
			NonFinancialAgent nonFin = (NonFinancialAgent) agent ;
			Bank bank = nonFin.chooseBank();
			enterQueue(bank, nonFin);

			chosenBanks.put(nonFin, bank);
		}
		
		for(Agent bank : banks) {
			((Bank) bank).grantLoans();
		}
		
		for(Agent agent : agents) {
			((NonFinancialAgent) agent).receiveGrantedCredit();
		}
	}
	
	public double getGrantedCredit(NonFinancialAgent agent) {
		
		Bank chosenBank = chosenBanks.get(agent);
		
		if(chosenBank != null) {
			if(inQueue(agent, chosenBank)) {
				return 0 ;
			}
			else {
				Account account = getEdge(chosenBank, agent);
				return account.getAvailableCredit() ;
			}
		}
		
		return 0 ;
	}
}
