package abm;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class MetaParameters {
	
	private MetaParameters() { } 
	
	private static int monthsOfIncome = 24 ;
	private static double percTotalCredit = 0.1 ;
	private static double marginPercSpread = 0.1 ;
	private static double firmsRiskPremium = 0.5 ;
	private static double consRiskPremium = 0.5 ;
	private static double resWageAdjustment = 0.01;
	private static double repExponent = 1;
	private static double informationDispersion = 0.1 ; 
	private static double propConsumeWealth = 0.5 ;
	private static double innExponent = 1 ;
	
	public static void initParameters() {
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		monthsOfIncome = params.getInteger("months_of_income");
		percTotalCredit = params.getDouble("perc_total_credit");
		marginPercSpread = params.getDouble("margin_perc_spread");
		firmsRiskPremium = params.getDouble("firms_risk_premium");
		consRiskPremium = params.getDouble("cons_risk_premium");
		resWageAdjustment = params.getDouble("res_wage_adjustment");
		repExponent = params.getDouble("rep_exponent");
		informationDispersion = params.getDouble("information_dispersion");
		propConsumeWealth = params.getDouble("prop_consume_wealth");
		innExponent = params.getDouble("inn_exponent");
	}

	public static double getInnExponent() {
		return innExponent;
	}
	
	public static double getPropConsumeWealth() {
		return propConsumeWealth;
	}

	public static int getMonthsOfIncome() {
		return monthsOfIncome;
	}
	
	public static double getPercTotalCredit() {
		return percTotalCredit;
	}

	public static double getMarginPercSpread() {
		return marginPercSpread;
	}

	public static double getFirmsRiskPremium() {
		return firmsRiskPremium;
	}

	public static double getConsRiskPremium() {
		return consRiskPremium;
	}

	public static double getResWageAdjustment() {
		return resWageAdjustment;
	}

	public static double getRepExponent() {
		return repExponent;
	}

	public static double getInformationDispersion() {
		return informationDispersion;
	}
}
