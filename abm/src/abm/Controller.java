package abm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.CentralBank;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.Firm;
import abm.agents.Government;
import abm.components.NewMachine;
import abm.components.Statistics;
import abm.components.UsedMachine;
import abm.creators.BanksCreator;
import abm.creators.ConsumersCreator;
import abm.creators.FirmsCreator;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;
import abm.links.AccountCreator;
import abm.links.JobCreator;
import abm.markets.CapitalGoodsMarket;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import cern.jet.random.Uniform;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.SimUtilities;

public class Controller implements ContextBuilder<Agent>{
	
	private HashMap<String,Double> params = null ;
	private ArrayList<Integer[]> distFir = null ;
	private ArrayList<Integer[]> distInc = null ;
	
	@SuppressWarnings("unchecked")
	@Override
	public Context<Agent> build(Context<Agent> context) {
		
		context.setId("abm");
		
		MetaParameters.initParameters();
		
		params = Utils.readParameters();
		distFir = Utils.readDistribution("fir");
		distInc = Utils.readDistribution("inc");
		
		Statistics stats = Statistics.getInstance() ;
		stats.init(context);
		
		ConsumersCreator consumersCreator = new ConsumersCreator(params);
		FirmsCreator firmsCreator = new FirmsCreator(params);
		BanksCreator banksCreator = new BanksCreator(params);
		
		Government government = Government.getInstance() ;
		context.add(government);
		government.setParams(params, stats);
		
		CentralBank centralBank = CentralBank.getInstance();
		context.add(centralBank);
		centralBank.setSelic(params.get(Keys.MEAN_SELIC));
		
		ArrayList<Consumer> consumers = consumersCreator.create(distInc);
		context.addAll(consumers);
		
		ArrayList<Firm> firms = firmsCreator.create(distFir);
		context.addAll(firms);
		
		ArrayList<Bank> banks = banksCreator.create(null);
		context.addAll(banks);
		
		NetworkBuilder<Agent> accountsBuilder = new NetworkBuilder<Agent>("accounts",context,false);
		accountsBuilder.setEdgeCreator(new AccountCreator());
		accountsBuilder.buildNetwork() ;
		CreditMarket.getInstance().init(context, (Network<Agent>) context.getProjection("accounts")) ;
	
		NetworkBuilder<Agent> jobsBuilder = new NetworkBuilder<Agent>("jobs",context,false);
		jobsBuilder.setEdgeCreator(new JobCreator());
		jobsBuilder.buildNetwork() ;
		LaborMarket.getInstance().init(context, (Network<Agent>) context.getProjection("jobs")) ;
		
		NetworkBuilder<Agent> capMarketBuilder = new NetworkBuilder<Agent>("capMarket", context, false);
		capMarketBuilder.buildNetwork();
		CapitalGoodsMarket.getInstance().init(context, (Network<Agent>) context.getProjection("capMarket"));
		
		NetworkBuilder<Agent> conMarketBuilder = new NetworkBuilder<Agent>("conMarket", context, false);
		conMarketBuilder.buildNetwork();
		ConsumptionGoodsMarket.getInstance().init(context, (Network<Agent>) context.getProjection("conMarket"));
		
		SimUtilities.shuffle(consumers, RandomHelper.getUniform());
		SimUtilities.shuffle(firms, RandomHelper.getUniform());
		SimUtilities.shuffle(banks, RandomHelper.getUniform());
		
		firmsCreator.createCapital(firms, consumers);

		firmsCreator.createSavings(firms, banks);
		firmsCreator.createLoans(firms, banks);
		
		consumersCreator.createSavings(consumers, banks);
		consumersCreator.createLoans(consumers, banks);
		
		banksCreator.initMargins(banks);
		
		createCapitalGoodsMarket(firms);
		createConsumptionGoodsMarket(firms, consumers);
		
		Flow.initFlow(context);
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(72);
		}

		return context;
	}
	
	public ArrayList<CapitalGoodsFirm> createCapitalGoodsMarket(ArrayList<Firm> firms) {
		
		ArrayList<NewMachine> newMachines = new ArrayList<NewMachine>();
		ArrayList<CapitalGoodsFirm> capFirms = new ArrayList<CapitalGoodsFirm>();
		HashMap<Integer, ArrayList<ConsumptionGoodsFirm>> map = 
									new HashMap<Integer, ArrayList<ConsumptionGoodsFirm>>();
		double totCapProd = 0 ;
		double totEff = 0 ;
		
		for(Firm firm : firms) {
			
			if(firm instanceof CapitalGoodsFirm) {
				totCapProd += firm.getIncome() ;
				capFirms.add((CapitalGoodsFirm) firm);
			}
			
			if(firm instanceof ConsumptionGoodsFirm) {
				
				ArrayList<UsedMachine> usedMachines = ((ConsumptionGoodsFirm) firm).getMachines();
				
				for(UsedMachine machine : usedMachines) {
					
					NewMachine newMachine = new NewMachine(machine.getCapacity(), machine.getMaxWages(), 0, 0);
					int inx = newMachines.indexOf(newMachine);
					
					if(inx == -1) {
						inx = newMachines.size();
						newMachines.add(newMachine);
						totEff += newMachine.getEfficiency() ;
						map.put(inx, new ArrayList<ConsumptionGoodsFirm>());
					}
					
					ArrayList<ConsumptionGoodsFirm> arr = map.get(inx);
					
					if(!arr.contains(firm)) {
						arr.add((ConsumptionGoodsFirm) firm);
					}
				}
 			}	
		}
		
		for(NewMachine nm : newMachines) {
			nm.setPrice((nm.getEfficiency()/totEff)*totCapProd*MetaParameters.getMonthsOfIncome());
			nm.increaseUnits(1);
		}
		
		for(CapitalGoodsFirm capFirm : capFirms) {
			
			double totProd = capFirm.getIncome() ;
			double fill = 0 ;
			
			for(int i = 0; i < newMachines.size(); i++) {
				
				NewMachine machine = newMachines.get(i);
				double price = machine.getPrice() / MetaParameters.getMonthsOfIncome() ;
				
				if(fill + price <= totProd) {
					
					ArrayList<ConsumptionGoodsFirm> cons = map.get(i);	
					
					if(cons != null) {
						
						capFirm.addMachine(machine);
						fill += price ;
						
						for(ConsumptionGoodsFirm conFirm : cons) {
							CapitalGoodsMarket.getInstance().addEdge(capFirm, conFirm);
						}
						
						map.remove(i);
					}
				}
			}
		}
		
		Set<Integer> keys = map.keySet() ;
		
		for(Integer inx : keys) {
			
			ArrayList<ConsumptionGoodsFirm> cons = map.get(inx);	
			int random = RandomHelper.nextIntFromTo(0, capFirms.size() - 1);
			CapitalGoodsFirm capFirm = capFirms.get(random) ;
			
			if(cons != null) {
				
				capFirm.addMachine(newMachines.get(inx));
				
				for(ConsumptionGoodsFirm conFirm : cons) {
					CapitalGoodsMarket.getInstance().addEdge(capFirm, conFirm);
				}
			}
		}
		
		return capFirms ;
	}
	
	public ArrayList<ConsumptionGoodsFirm> createConsumptionGoodsMarket(ArrayList<Firm> firms, ArrayList<Consumer> consumers) {
		
		HashMap<ConsumptionGoodsFirm, Double> map = new HashMap<ConsumptionGoodsFirm, Double>();
		ArrayList<ConsumptionGoodsFirm> keys = new ArrayList<ConsumptionGoodsFirm>();
		
		double totIncome = 0 ;
		double parExpend = 0 ;
		
		// OK: lastDemand*price = income
		for(Firm firm : firms) {
			
			if(firm instanceof ConsumptionGoodsFirm) {
				map.put((ConsumptionGoodsFirm) firm, ((ConsumptionGoodsFirm) firm).getIncome());
				keys.add((ConsumptionGoodsFirm) firm);
				totIncome += firm.getIncome() ;
			}
		}
		
		for(Consumer con : consumers) {
			parExpend += con.liquidPayment() ;
		}
		
		// OK: remExpend > 0 
		double remExpend = totIncome - parExpend ;

		// OK: random sum is almost equal to remExpend (= fromWealth)
		double limit = (remExpend/consumers.size())*2 ;
		Uniform unif = RandomHelper.createUniform(0, limit) ;
		int maxInx = keys.size()-1;
		int nFirms = keys.size() ;
				
        for(Consumer consumer : consumers) {
        	
            double spent = consumer.initDemand(unif.nextDouble()) ;
            
            while(spent > 0 && nFirms > 0) {
               
                int inx = RandomHelper.nextIntFromTo(0, maxInx);
                ConsumptionGoodsFirm firm = keys.get(inx);
               
                double firIncome = map.get(firm) ;
               
                if(firIncome != 0) {
                    if(spent >= firIncome) {
                        map.replace(firm, 0.0);
                        nFirms-- ;
                        spent -= firIncome ;
                    }
                    else { // spent < firIncome
                        map.replace(firm, firIncome - spent);
                        spent = 0 ;
                    }
                   
                    ConsumptionGoodsMarket.getInstance().addEdge(firm, consumer);
                }
            }
        }
        
		return null ;
	}

}
