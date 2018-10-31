package abm.agents;

import abm.helpers.Utils;
import abm.markets.CreditMarket;

public class Agent {
	
	// State variables
	private double assets ;
	private double debt ;
	private double netWorth ;
	
	// Auxiliary variables
	private static int idCounter = 0 ;
	private int id ;

	// Markets
	protected static final CreditMarket creditMarket = CreditMarket.getInstance();
	
	public Agent() { 
		this.id = idCounter++ ;
	}
	
	public Agent(double assets, double debt) {
		this.assets = assets ;
		this.debt = debt ;
		this.updateNetWorth();
		this.id = idCounter++ ;
	}
	
	public Agent(double assets, double debt, int id) {
		this.assets = assets ;
		this.debt = debt ;
		this.updateNetWorth();
		this.id = id ;
	}
	
	public int getId() {
		return id ;
	}
	
	public void setAssets(double assets) {
		this.assets = assets ;
		this.updateNetWorth();
	}
	
	public double getAssets() {
		return(this.assets);
	}
	
	public void setDebt(double debt) {
		this.debt = debt ;
		this.updateNetWorth();
	}
	
	public double getDebt() {
		return(this.debt);
	}
	
	public double getNetWorth() {
		return(this.netWorth);
	}
	
	public void updateDebt(double amount) {
		this.debt += amount ;
		this.updateNetWorth();
	}
	
	public void updateAssets(double amount) {
		this.assets += amount ;
		this.updateNetWorth();
	}
	
	private void updateNetWorth() {
		this.netWorth = this.assets - this.debt ;
	}

	
	@Override 
	public boolean equals(Object obj) {
		
		if(obj instanceof Agent) {
			
			if(((Agent) obj).getId() == this.id) {
				return true ;
			}
			
			return false ;
		}
		
		return false ;
	}
	
	@Override
	public String toString() {
		
		Integer id = this.id ;
		Double assets = this.assets ;
		Double debt = this.debt ;
		Double netWorth = this.netWorth ;

		String[][] fields = { { "Id", id.toString()}, 
							{"Assets", assets.toString()},
							{"Debt", debt.toString()},
							{"Net Worth", netWorth.toString()} } ;
		
	    return Utils.getAgentDescriptor(fields, true) ;
	}

}
