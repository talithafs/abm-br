package abm;

public class Calibrable {
	
	private Calibrable() { } 
	
	private static Integer monthsOfIncome = null ;
	private static Double percTotalCredit = null ;
	private static Double marginPercSpread = null ;
	private static Double firmsRiskPremium = null ;
	private static Double consRiskPremium = null ;
	private static Double resWageAdjustment = null ;
	private static Double repExponent = null ;
	private static Double informationDispersion = null ; 
	private static Double propConsumeWealth = null ;
	private static Double innExponent = null ;
	
	public static void initParameters() {
		Calibrable.initMonthsOfIncome(24);
		Calibrable.initPercTotalCredit(0.1);
		Calibrable.initMarginPercSpread(0.1);
		Calibrable.initResWageAdjustment(0.01);
		Calibrable.initFirmsRiskPremium(0.5);
		Calibrable.initRepExponent(0.0000001);
		Calibrable.initInformationDispersion(0.1); 
		Calibrable.initPropConsumeWealth(0.5);
		Calibrable.initConsRiskPremium(0.5);
		Calibrable.initInnExponent(0.00001);
	}

	public static Double getInnExponent() {
		return innExponent;
	}

	public static void initInnExponent(Double innExponnent) {
		
		if(Calibrable.innExponent == null) {
			Calibrable.innExponent = innExponnent;
		}
	}

	private static void initPropConsumeWealth(double prop) {
		
		if(Calibrable.propConsumeWealth == null) {
			Calibrable.propConsumeWealth = prop;
		}
	}
	
	public static Double getPropConsumeWealth() {
		return propConsumeWealth;
	}

	public static int getMonthsOfIncome() {
		return monthsOfIncome;
	}
	
	public static double getPercTotalCredit() {
		return percTotalCredit;
	}

	public static void initMonthsOfIncome(Integer monthsOfIncome) {
		
		if(Calibrable.monthsOfIncome == null) {
			Calibrable.monthsOfIncome = monthsOfIncome;
		}
	}

	public static void initPercTotalCredit(Double percTotalCredit) {
		
		if(Calibrable.percTotalCredit == null) {
			Calibrable.percTotalCredit = percTotalCredit;
		}
	}

	public static Double getMarginPercSpread() {
		return marginPercSpread;
	}

	public static void initMarginPercSpread(Double marginPercSpread) {
		
		if(Calibrable.marginPercSpread == null) {
			Calibrable.marginPercSpread = marginPercSpread;
		}
		
	}

	public static Double getFirmsRiskPremium() {
		return firmsRiskPremium;
	}

	public static void initFirmsRiskPremium(Double riskPremium) {
		
		if(Calibrable.firmsRiskPremium  == null) {
			Calibrable.firmsRiskPremium = riskPremium;
		}	
	}
	
	public static Double getConsRiskPremium() {
		return consRiskPremium;
	}

	public static void initConsRiskPremium(Double riskPremium) {
		
		if(Calibrable.consRiskPremium  == null) {
			Calibrable.consRiskPremium = riskPremium;
		}	
	}
	
	public static Double getResWageAdjustment() {
		return resWageAdjustment;
	}

	public static void initResWageAdjustment(Double riskPremium) {
		
		if(Calibrable.resWageAdjustment  == null) {
			Calibrable.resWageAdjustment  = riskPremium;
		}	
	}

	public static Double getRepExponent() {
		return repExponent;
	}

	public static void initRepExponent(Double repExponent) {
		
		if(Calibrable.repExponent == null) {
			Calibrable.repExponent = repExponent;
		}
	}

	public static Double getInformationDispersion() {
		return informationDispersion;
	}

	public static void initInformationDispersion(Double informationDispersion) {
		
		if(Calibrable.informationDispersion == null) {
			Calibrable.informationDispersion = informationDispersion;
		}
	}
	
	
}
