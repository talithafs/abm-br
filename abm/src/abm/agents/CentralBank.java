package abm.agents;

public final class CentralBank extends FinancialAgent {
	
	private static double selic ;
	private static CentralBank instance = new CentralBank();
	
	private CentralBank() { } 
	
	public static CentralBank getInstance() {
		return instance ;
	}
	
	public double getSelic() {
		return selic ;
	}
	
	public void setSelic(double s) {
		selic = s ;
	}
}
