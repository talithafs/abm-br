package abm.creators;

import java.util.ArrayList;
import java.util.HashMap;

import abm.agents.Bank;
import abm.agents.Government;
import abm.helpers.Constants;
import abm.helpers.Constants.Keys;

public class BanksCreator implements AgentsCreator<Bank> {
	
	private HashMap<String, Double> params ;
	
	public BanksCreator(HashMap<String, Double> params) { 
		this.params = params ;
	}

	@Override
	public ArrayList<Bank> create(ArrayList<Integer[]> distribution) {
		
		int nBanks = params.get(Constants.Keys.N_BANKS).intValue();
		ArrayList<Bank> banks = new ArrayList<Bank>();
		
		for(int i = 0; i < nBanks; i++) {
			Bank bank = new Bank();
			bank.initInterestOnDeposits(Government.getInstance().getParam(Keys.MEAN_SAVS_INT));
			banks.add(bank);
		}
		
		return banks;
	}
	
	public void initMargins(ArrayList<Bank> banks) {
		
		for(Bank bank : banks) {
			bank.initMargin(); 
		}
	}

}
