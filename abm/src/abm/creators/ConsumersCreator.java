package abm.creators;

import java.util.ArrayList;
import java.util.HashMap;

import abm.agents.Bank;
import abm.agents.Consumer;
import abm.agents.EmployedConsumer;
import abm.agents.UnemployedConsumer;
import abm.helpers.Constants;
import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;

public class ConsumersCreator implements NonFinancialAgentsCreator<Consumer> {
	
	private HashMap<String, Double> params ;
	
	public ConsumersCreator(HashMap<String, Double> params){
		this.params = params ;
	}

	@Override
	public ArrayList<Consumer> create(ArrayList<Integer[]> distribution){
		
		double savingPerc = params.get(Constants.Keys.SAVING_PERC);
		double debtPerc = params.get(Constants.Keys.HH_DEBT);
		
		Uniform savUnif = RandomHelper.createUniform(0, 2*savingPerc);	
		Uniform debUnif = RandomHelper.createUniform(0, 2*debtPerc);
		
		ArrayList<Consumer> consumers = new ArrayList<Consumer>();
		double sum = 0 ;
		
		for(Integer[] row : distribution) {
			
			int nEmps = row[0];
			int nUnemps = row[1];
			double wage = row[2];
			double wealth = row[3];
			
			for(int i = 0; i < nEmps; i++) {
				
				savingPerc =  savUnif.nextDouble() ; 
				debtPerc =  debUnif.nextDouble() ;
				Consumer cons = new EmployedConsumer(savingPerc, wage, wealth, debtPerc*12*wage, wage);
				consumers.add(cons);
			}
			
			for(int i = 0; i < nUnemps; i++) {
				
				savingPerc =  savUnif.nextDouble() ;
				debtPerc =  debUnif.nextDouble() ;
				Consumer cons = new UnemployedConsumer(savingPerc, wage, wealth, debtPerc*12*wage);
				consumers.add(cons);
			}
			
			
		}
	
		return consumers ;
	}

	@Override
	public void createSavings(ArrayList<Consumer> consumers, ArrayList<Bank> banks) {
		
		for(Consumer consumer : consumers) {
			
			int inx = RandomHelper.nextIntFromTo(0, banks.size()-1);
			double amount = consumer.getAssets() ;
			
			Bank bank = banks.get(inx);
			bank.deposit(consumer, amount);
		}
	}

	@Override
	public void createLoans(ArrayList<Consumer> consumers, ArrayList<Bank> banks) {
		
		double meanInterest = params.get(Constants.Keys.MEAN_INT_CONS);
		Uniform runif = RandomHelper.createUniform(0,2*meanInterest);
		
		for(Consumer consumer : consumers) {
			
			int inx = RandomHelper.nextIntFromTo(0, banks.size()-1);
			double amount = consumer.getDebt() ;
			
			Bank bank = banks.get(inx);
			bank.lend(consumer, amount,  runif.nextDouble());
		}
	}
	
}
