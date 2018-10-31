package abm;

import java.util.ArrayList;
import java.util.HashMap;

import abm.helpers.Utils;
import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import repast.simphony.random.RandomHelper;

public class Tester {
	
    private static HashMap<String,Double> params = null ;
	private static ArrayList<Integer[]> distFir = null ;
	private static ArrayList<Integer[]> distInc = null ;

	public static void main(String[] args) {
		
		params = Utils.readParameters();
		distFir = Utils.readDistribution("fir");
		distInc = Utils.readDistribution("inc");
		
		double[] vec = { 0.20, 0.30, 0.10, 0.40 } ;
		EmpiricalWalker walker = RandomHelper.createEmpiricalWalker(vec, Empirical.NO_INTERPOLATION);
		


//		
//		Controller controller = Controller.getInstance() ;
//		ConsumersCreator consumersCreator = ConsumersCreator.getInstance();
//		FirmsCreator firmsCreator = FirmsCreator.getInstance();
//		BanksCreator banksCreator = BanksCreator.getInstance();
//		
//		controller.setAccumGdp(params.get(Constants.Keys.GDP));
//		controller.setNConsumers(params.get(Constants.Keys.N_CONSUMERS).intValue());
//		controller.setNUnemployed(params.get(Constants.Keys.N_UNEMPLOYED).intValue());
//		
//		ArrayList<Consumer> consumers = consumersCreator.create(params, distInc);
//		ArrayList<Firm> firms = firmsCreator.create(params, distFir);
//		ArrayList<Bank> banks = banksCreator.create(params, null);
//		
//		
//		ArrayList<Agent> agents = new ArrayList<Agent>();
//		agents.addAll(consumers);
//		agents.addAll(firms);
//		
//		System.out.println(controller.updateAssetSeries(agents));
//		System.out.println(controller.updateDebtSeries(agents));
//		
//		for(Bank bank : banks) {
//			System.out.println(bank.toString());
//		}
//		
//		for(Firm firm : firms) {
//			System.out.println(firm.toString());
//		}
//		
//		for(Consumer cons : consumers) {
//			System.out.println(cons.toString());
//		}
//		
//		System.out.println(consumers.size());
//		
//	
	}
	

}
