package abm.helpers;

public final class Constants {
	
	private Constants() {}
	
	public static final class Paths { 
		
		private static final String inputs = "/home/talithafs/Dropbox/Dissertação/Modelo/abm/input/" ;
		
		public static final String PARAMS_FILE = inputs + "parameters.csv";
		public static final String DIST_FIR_FILE = inputs + "dist_fir.csv";
		public static final String DIST_INC_FILE = inputs + "dist_inc.csv";
	}

	public static final class Keys { 
		
		public static final String N_CONSUMERS = "n_consumers" ;
		public static final String N_UNEMPLOYED = "n_unemployed" ; 
		public static final String N_CON_FIRMS = "n_con_firms" ; 
		public static final String N_CAP_FIRMS = "n_cap_firms" ;
		public static final String N_BANKS = "n_banks" ;
		public static final String GDP = "gdp" ;
		public static final String GDP_PER_EMPL = "gdp_per_empl" ;
		public static final String PERC_SAL = "perc_sal" ;
		public static final String PRF_PER_EMPL = "prf_per_empl" ;
		public static final String HH_DEBT = "hh_debt" ;
		public static final String DEBT_PER_EMPL = "debt_per_empl" ;
		public static final String MEAN_INT_CONS = "mean_int_cons" ;
		public static final String MEAN_INT_FIRMS = "mean_int_firms" ;
		public static final String SAVING_PERC = "saving_perc";
		public static final String MEAN_SAVS_INT = "mean_savs_int";
		public static final String USED_CAP = "used_cap";
		public static final String MIN_WAGE = "min_wage";
		public static final String BKR_RATE = "bkr_rate";
		public static final String GROSS_SAL = "gross_sal";
		public static final String DEBT_TO_EQUITY = "debt_to_equity";
		public static final String MEAN_SELIC = "mean_selic";
	}
}
