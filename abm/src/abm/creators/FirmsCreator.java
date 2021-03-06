package abm.creators;

import java.util.ArrayList;
import java.util.HashMap;

import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.components.UsedMachine;
import abm.helpers.Constants;
import abm.helpers.Constants.Keys;
import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;



public class FirmsCreator implements NonFinancialAgentsCreator<Firm>{
	
	private HashMap<String,Double> params ;
	
	public FirmsCreator(HashMap<String,Double> params){
		this.params = params ;
	}

	public ArrayList<Firm> create(ArrayList<Integer[]> distribution){
		
		ArrayList<Firm> firms = null;
		
		double gdpPerWorker = params.get(Constants.Keys.GDP_PER_EMPL) ;
		double debtPerWorker = params.get(Constants.Keys.DEBT_PER_EMPL);
		double grossSal = params.get(Keys.GROSS_SAL);
		double ratio = params.get(Keys.DEBT_TO_EQUITY);
		
		Uniform unifGdp = RandomHelper.createUniform(0, 2*gdpPerWorker);
		Uniform unifDebt = RandomHelper.createUniform(0, 2*debtPerWorker);
		Uniform unifRatio = RandomHelper.createUniform(0, 2*ratio) ;
		Uniform unifPrice = RandomHelper.createUniform(0.95, 1.05);
		
		double totalIncome = 0 ;
		
		while(totalIncome < grossSal) {
			
			firms = new ArrayList<Firm>();
			totalIncome = 0 ;
			
			for(Integer[] row : distribution) {
				
				int nFirms = row[0] ;
				int nEmps = row[1] ; 
				int type = row[2] ;
				
				for(int i = 0; i < nFirms; i++) {
					
					double income = nEmps*( unifGdp.nextDouble());
					double debt = nEmps*(unifDebt.nextDouble());
					double dte =  unifRatio.nextDouble() ;
					double assets = debt*(1+dte)/dte ;
					double price = unifPrice.nextDouble();
					
					if(type == 0) {
						ConsumptionGoodsFirm con = new ConsumptionGoodsFirm(income, price, debt, assets, nEmps);
						firms.add(con);
						totalIncome += income ;
						
					}
					else {
						CapitalGoodsFirm cap = new CapitalGoodsFirm(income, debt, assets, nEmps);
						firms.add(cap);
					}
					
					
				}
			}		
		}
		
		
		
		return firms ;
	}

	@Override
	public void createSavings(ArrayList<Firm> firms, ArrayList<Bank> banks) {

		for(Firm firm : firms) {
			
			int inx = RandomHelper.nextIntFromTo(0, banks.size()-1);
			double amount = firm.getAssets();
			
			Bank bank = banks.get(inx);
			bank.deposit(firm, amount);
			
		}
	}

	@Override
	public void createLoans(ArrayList<Firm> firms, ArrayList<Bank> banks) {
		
		double meanInterest = params.get(Constants.Keys.MEAN_INT_FIRMS);
		Uniform runif = RandomHelper.createUniform(0,2*meanInterest);
		
		for(Firm firm : firms) {
			
			int inx = RandomHelper.nextIntFromTo(0, banks.size()-1);
			double amount = firm.getDebt();
			
			Bank bank = banks.get(inx);
			bank.lend(firm, amount,  runif.nextDouble());
		}
	}
	
	
	public void createCapital(ArrayList<Firm> firms, ArrayList<Consumer> consumers) {
		
		int j = 0 ;
		double capc = params.get(Constants.Keys.USED_CAP);
		double minWage = params.get(Constants.Keys.MIN_WAGE);
		
		for(Firm firm : firms) {
			
			int nEmps = firm.getNEmployees();
			double totMinWages = 0 ;
			ArrayList<EmployedConsumer> emps = new ArrayList<EmployedConsumer>();

			// create jobs
			for(int i = 0; i < nEmps; i++) {
				
				while(j < consumers.size()) {
					
					Consumer consumer = consumers.get(j);
					j++;
					
					if(consumer instanceof EmployedConsumer) {
						
						double wage = ((EmployedConsumer) consumer).getWage();
						firm.initJob((EmployedConsumer) consumer);
						totMinWages += wage/minWage ;
						
						emps.add((EmployedConsumer) consumer);
						break ;
					}
				}
			}
			
			// create machines
			if(firm instanceof ConsumptionGoodsFirm) {
				
				double qty = ((ConsumptionGoodsFirm) firm).getLastProduction() ;
				double umac = qty / totMinWages ;
			
				for(EmployedConsumer worker : emps) {

					double w = worker.getWage()/minWage ;
					
					long capacity = (long) ((w*umac)/capc) ;
					double maxWages =  w/capc ;
					UsedMachine machine =  new UsedMachine(capacity, maxWages) ;
					machine.addOperator((EmployedConsumer) worker);

					// TODO !!!! As vezes firmas sao criadas com installedCap == 0
					((ConsumptionGoodsFirm) firm).addMachine(machine);	
				}
				
				((ConsumptionGoodsFirm) firm).init();
				
				long inst = ((ConsumptionGoodsFirm) firm).getInstalledCapacity() ;
				
				if(inst == 0) {
					System.out.println("Installed cap is zero!");
				}
			}
		}
	}
	
}
