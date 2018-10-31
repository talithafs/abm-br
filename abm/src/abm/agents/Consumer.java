package abm.agents;

import java.util.ArrayList;

import abm.MetaParameters;
import abm.Controller;
import abm.helpers.Utils;
import abm.links.Account;
import abm.markets.ConsumptionGoodsMarket;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

public class Consumer extends NonFinancialAgent {
	
	// State variables
	protected double resWage  ;
	protected double savingPerc ;
	protected double payment ;
	private double totalValue = 0 ;
	
	// Parameters
	private double consInertia =  0.5 ;
	private double percFromWealth = -1 ;
	
	// Auxiliary variables
	protected long demand = 0 ;
	protected ConsumptionGoodsFirm chosenFirm ;
	protected ArrayList<ConsumptionGoodsFirm> chosenFirms ;
	private double valueSpent = 0 ;
	private long boughtQty = 0 ; 
	
	// Markets
	protected final static ConsumptionGoodsMarket consumptionGoodsMarket = ConsumptionGoodsMarket.getInstance() ;
	
	public Consumer(double savingPerc, double resWage, double assets, double debt) {
		super(assets, debt);
		this.savingPerc = savingPerc ;
		this.resWage = resWage ; 
		this.payment = 0 ;
	}
	
	public Consumer(double savingPerc, double resWage, double assets, double debt, double percWealth, int id) {
		super(assets, debt, id);
		this.percFromWealth = percWealth ;
		this.savingPerc = savingPerc ;
		this.resWage = resWage ; 
		this.payment = 0 ;
	}
	
	public double initDemand(double fromWealth) {
		
		if(percFromWealth == -1) {
			this.percFromWealth =  (fromWealth/this.getNetWorth());
			this.valueSpent = fromWealth + liquidPayment();
		}

		return this.valueSpent ;
	}
	
	public void calculateDemand() {
		
		this.valueSpent = 0 ;
		this.boughtQty = 0 ; 
		this.chosenFirms = new ArrayList<ConsumptionGoodsFirm>();
		this.totalValue = getValueToSpend();
		this.chosenFirm = chooseFirm();
		this.demand = (long) (this.totalValue / chosenFirm.getPrice());
	}
	
	public ConsumptionGoodsFirm chooseFirm() {
		
		ConsumptionGoodsFirm random = consumptionGoodsMarket.selectFirm() ;
		ArrayList<ConsumptionGoodsFirm> firms = consumptionGoodsMarket.getAdjacent(this);
		
		if(!firms.contains(random)) {
			firms.add(random);
		}
		
		double minPrice = Double.MAX_VALUE ;
		ConsumptionGoodsFirm chosen = null ;
		
		for(ConsumptionGoodsFirm firm : firms) {
			if(firm.getPrice() < minPrice && !chosenFirms.contains(chosen)) {
				chosen = firm ;
				minPrice = firm.getPrice() ;
			}
		}
		
		chosenFirms.add(chosen);
		return chosen ;
	}
	
	private double getValueToSpend() {
		
		double prop = RandomHelper.nextDoubleFromTo(0, 1) ;
		double value = liquidPayment();
			
		if(prop < MetaParameters.getPropConsumeWealth()) {
			
			if(this.getNetWorth() > 0) {
				value += this.percFromWealth * this.getNetWorth() ;
			}
		}
				
		return value ;
	}
	
	public double liquidPayment() {
		
		if(this.payment != 0) {
			return (1 - this.savingPerc) * this.payment ;		
		}
		else {
			return this.consInertia * (1 - this.savingPerc) * this.resWage ;
		}
	}
	
	@Override
	public void calculateNeededCredit() {
		
		if(this.payment == 0) {
			
			if(this.getNetWorth() < this.totalValue) {
				this.neededCredit = this.totalValue ;
			}
		}
		else {
			this.neededCredit = 0 ;
		}
	}
	
	@Override
	public void receiveGrantedCredit() {
		
		this.grantedCredit = 0 ;

		if(this.payment > 0) {
			Bank bank = creditMarket.getBankWithDeposits(this);
			bank.deposit(this, this.payment);
			this.payment = 0 ;
		}
		super.receiveGrantedCredit();
	}
	
	public long decideToBuy(long quantity) {

		if(this.totalValue > this.valueSpent) {
			
			double value = quantity * this.chosenFirm.getPrice() ;
			double assets = this.getAssets() ;
			assets -= this.valueSpent ;
			
			if(this.totalValue - (value + this.valueSpent) < 0) {
				value = this.totalValue - this.valueSpent ;
				quantity = (long) (value/this.chosenFirm.getPrice()) ;
			}

			if(quantity > 0 && assets - value >= 0) {

				this.valueSpent += value;
				
				if(this.demand - quantity > 0) {
					this.chosenFirm = chooseFirm();
					this.demand = (int) ((this.totalValue - this.valueSpent)/this.chosenFirm.getPrice()) ;	
				}
				
				boughtQty += quantity ;
				return quantity ;
			}
			else {
				return 0 ;
			}
		}
		else {
			return 0 ;
		}
	}	

	public static final UnemployedConsumer changeStatus(EmployedConsumer emp) {
		
		UnemployedConsumer unemp = new UnemployedConsumer(emp.getSavingPerc(), emp.getResWage(), emp.getAssets(), emp.getDebt(), emp.getPercFromWealth(), emp.getId());
		unemp.payment = emp.getPayment();
		changeStatus(emp, unemp);
		return unemp ;
	}
	
	public static final EmployedConsumer changeStatus(UnemployedConsumer unemp, double wage) {
		
		EmployedConsumer emp = new EmployedConsumer(unemp.getSavingPerc(), unemp.getResWage(), unemp.getAssets(), unemp.getDebt(), wage, unemp.getPercFromWealth(), unemp.getId());
		changeStatus(unemp,emp);
		return emp ;
	}
	
	@SuppressWarnings("unchecked")
	private static final void changeStatus(Consumer oldCon, Consumer newCon) {
		
		// TODO ContextUtils does not work in unit tests
		Context<Agent> context = ContextUtils.getContext(oldCon);
		context.add(newCon);
		
		ArrayList<Account> accounts = creditMarket.getEdges(oldCon);
		
		for(Account account : accounts) {
			creditMarket.removeEdge(account);
			account.updateTarget(newCon);
			creditMarket.addEdge(account);
 		}

		ArrayList<ConsumptionGoodsFirm> conFirms = consumptionGoodsMarket.getAdjacent(oldCon);
		consumptionGoodsMarket.removeEdges(oldCon);
		consumptionGoodsMarket.addEdges(conFirms, newCon);	
		
		context.remove(oldCon);
	}
	
	public ConsumptionGoodsFirm getChosenFirm() {
		return this.chosenFirm ;
	}
	

	public double getPayment() {
		return this.payment ;
	}

	public double getResWage() {
		return resWage;
	}

	public void setResWage(double resWage) {
		this.resWage = resWage;
	}

	public double getSavingPerc() {
		return savingPerc;
	}
	
	public long getDemand() {
		return demand;
	}

	public void setSavingPerc(double savingPerc) {
		this.savingPerc = savingPerc;
	}

	public double getTotalValue() {
		return totalValue;
	}

	public double getConsInertia() {
		return consInertia;
	}

	public double getPercFromWealth() {
		return percFromWealth;
	}


	public double getValueSpent() {
		return valueSpent;
	}

	public long getBoughtQty() {
		return boughtQty;
	}

	public ArrayList<ConsumptionGoodsFirm> getChosenFirms() {
		return chosenFirms;
	}

	public void adjustReservationWage() {
		
	}

	@Override
	public String toString() {
		
		String strAgent = super.toString() ;
		
		Double resWage = this.resWage;
		Double savingPerc = this.savingPerc ;
		Long demand = this.demand ;

		String[][] fields = { { "Reservation Wage", resWage.toString()}, 
							{"Saving Percentage", savingPerc.toString()},
							{"Demand", demand.toString()}} ;
		
	    return strAgent + Utils.getAgentDescriptor(fields, false) ;
	}	
}
