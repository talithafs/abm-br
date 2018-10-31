package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.Government;
import abm.agents.UnemployedConsumer;
import abm.components.Statistics;
import abm.creators.BanksCreator;
import abm.creators.ConsumersCreator;
import abm.creators.FirmsCreator;
import abm.helpers.Constants;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;
import repast.simphony.context.DefaultContext;

class CreateAgentsTest {
	
	private static HashMap<String,Double> params = null ;
	private static ArrayList<Integer[]> distFir = null ;
	private static ArrayList<Integer[]> distInc = null ;
	
	private static ConsumersCreator consumersCreator = null ;
	private static FirmsCreator firmsCreator = null ;
	private static BanksCreator banksCreator = null ;
	
	 @BeforeAll
	 static void initAll() {
		 
		 MetaParameters.initParameters(); 
		 
		 Statistics stats = Statistics.getInstance() ;
		 stats.init(null);
			
		 params = Utils.readParameters();
		 distFir = Utils.readDistribution("fir");
		 distInc = Utils.readDistribution("inc");
		 
		 Government.getInstance().setParams(params, stats);
		 
		 consumersCreator = new ConsumersCreator(params);
		 firmsCreator = new FirmsCreator(params);
		 banksCreator = new BanksCreator(params);
	 }


	@Nested
	@DisplayName("Consumers creation")
	@TestInstance(Lifecycle.PER_CLASS)
	class ConsumersCreation {
		
		ArrayList<Consumer> consumers = null ;
		
		private int sumEmps = 0 ; 
		private int sumUnemps = 0 ;
		
		@BeforeAll
		void init() {
			
			consumers = consumersCreator.create(distInc);
			
			for(Integer[] row : distInc) {
				
				sumEmps += row[0];
				sumUnemps += row[1];
			}
		}

		@Nested
	    @DisplayName("Sizes")
	    class Sizes {
			
			@Test
			@DisplayName("Number of employed consumers")
			void testEmployed() {
				
				int sum = 0 ;
				
				for(Consumer cons : consumers) {
					if(cons instanceof EmployedConsumer) {
						sum++ ;
					}
				}
				
				int emps = (int) (params.get(Keys.N_CONSUMERS) - params.get(Keys.N_UNEMPLOYED)) ;
				
				assertEquals(sum,sumEmps);
				assertEquals(sum,emps);
			}
			
			@Test
			@DisplayName("Number of unemployed consumers")
			void testUnemployed() {
				
				int sum = 0 ;
				
				for(Consumer cons : consumers) {
					if(cons instanceof UnemployedConsumer) {
						sum++ ;
					}
				}
				
				int unemps = params.get(Keys.N_UNEMPLOYED).intValue() ;
				
				assertEquals(sum,sumUnemps);
				assertEquals(sum,unemps);
			}
		}
		
		@Nested
		@DisplayName("Randoms")
		class Randoms {
			
			@Test
			@DisplayName("Saving percentage mean")
			void testSavingPercentage() {
				
				double sum = 0 ;
				double savingPerc = params.get(Constants.Keys.SAVING_PERC);
				
				for(Consumer cons : consumers) {
					sum += cons.getSavingPerc() ;
				}
				
				double mean = sum / consumers.size() ;
				
				assertTrue(mean < 1.05*savingPerc && mean > 0.95*savingPerc) ;
			}
			
			@Test
			@DisplayName("Debt percentage mean")
			void testDebtPercentage() {
				
				double sum = 0 ;
				double debtPerc = params.get(Constants.Keys.HH_DEBT);
				
				for(Consumer cons : consumers) {
					
					double wage ;
					
					if(cons instanceof EmployedConsumer) {
						wage = ((EmployedConsumer) cons).getWage();
					} 
					else {
						wage = cons.getResWage() ;
					}
					
					sum += cons.getDebt()/(12*wage) ;
				}
				
				double mean = sum/consumers.size() ;
				
				assertTrue(mean < 1.05*debtPerc && mean > 0.95*debtPerc) ;
			}
		}
	}
	
	@Nested
	@DisplayName("Firms creation")
	@TestInstance(Lifecycle.PER_CLASS)
	class FirmsCreation {
		
		ArrayList<Firm> firms = null ;
		
		private int sumCapFirms = 0 ; 
		private int sumConFirms = 0 ;
		private int sumCapEmps = 0 ;
		private int sumConEmps = 0 ;
		
		@BeforeAll
		void init() {
			
			firms = firmsCreator.create(distFir);
			
			for(Integer[] row : distFir) {
				
				int nFirms = row[0] ;
				int nEmps = row[1] ; 
				int type = row[2] ;
	
					
				if(type == 0) {
					sumConFirms += nFirms ;
					sumConEmps += nFirms*nEmps ;
					
				}
				else {
					sumCapFirms += nFirms ;
					sumCapEmps += nFirms*nEmps ;
				}
	
			}		
		}

		@Nested
	    @DisplayName("Sizes")
	    class Sizes {
			
			@Test
			@DisplayName("Number of consumption goods firms and of employers of this sector")
			void testConFirms() {
				
				int size = 0 ;
				int emps = 0 ; 
				
				for(Firm firm : firms) {
					if(firm instanceof ConsumptionGoodsFirm) {
						size++ ;
						emps += firm.getNEmployees() ;
					}
				}
				
				assertEquals(size,sumConFirms, "number of firms");
				assertEquals(emps,sumConEmps, "number of employees");
			}
			
			@Test
			@DisplayName("Number of capital goods firms and of employers of this sector")
			void testCapFirms() {
				
				int size = 0 ;
				int emps = 0 ; 
				
				for(Firm firm : firms) {
					if(firm instanceof CapitalGoodsFirm) {
						size++ ;
						emps += firm.getNEmployees() ;
					}
				}
				
				assertEquals(size,sumCapFirms, "number of firms");
				assertEquals(emps,sumCapEmps, "number of employees");
			}
		}
		
		@Nested
		@DisplayName("Randoms")
		class Randoms {
			
			@Test
			@DisplayName("GDP per worker mean")
			void testGDP() {
				
				double sum = 0 ;
				double gdpPerEmpl = params.get(Keys.GDP_PER_EMPL);
				
				for(Firm firm : firms) {
					sum += firm.getIncome() / firm.getNEmployees();
				}
				
				double mean = sum / firms.size() ;
				
				assertTrue(mean < 1.05*gdpPerEmpl && mean > 0.95*gdpPerEmpl) ;
			}
			
			@Test
			@DisplayName("Debt per employee mean")
			void testDebt() {
				
				double sum = 0 ;
				double debtPerEmpl = params.get(Keys.DEBT_PER_EMPL);
				
				for(Firm firm : firms) {
					
					double debt =  (firm.getDebt() / firm.getNEmployees()) ;
					
					sum += debt ;
				}
				
				double mean = sum/firms.size() ;
				
				assertTrue(mean < 1.1*debtPerEmpl && mean > 0.90*debtPerEmpl) ;
			}
		}
		
		@Test
		@DisplayName("Total income versus gross wages")
		void testIncome() {
			
			double totalIncome = 0 ;
			double grossSal = params.get(Keys.GROSS_SAL);
			
			for(Firm firm : firms) {
				
				if(firm instanceof ConsumptionGoodsFirm) {
					totalIncome += firm.getIncome() ;
				}
			}
			
			assertTrue(totalIncome > grossSal);			
		}
	}

	@Nested
	@DisplayName("Banks creation")
	@TestInstance(Lifecycle.PER_CLASS)
	class BanksCreation {
		
		@Test
		@DisplayName("Number of banks")
		void testNumber() {
			
			ArrayList<Bank> banks = banksCreator.create(null);
			int nBanks = params.get(Keys.N_BANKS).intValue();
			
			assertEquals(banks.size(), nBanks);
		}
	}
	
	
	@Test
	@DisplayName("Consistency number of employees/employed consumers")
	void testConsistency() {
		
		ArrayList<Consumer> consumers = consumersCreator.create(distInc);
		ArrayList<Firm> firms = firmsCreator.create(distFir);
		
		double nEmpCons = 0 ;
		double nEmps = 0 ;
		
		for(Consumer con : consumers) {
			if(con instanceof EmployedConsumer) {
				nEmpCons ++ ;
			}
		}
		
		for(Firm firm : firms) {
			nEmps += firm.getNEmployees() ;
		}
		
		assertEquals(nEmps, nEmpCons);
	}
	


}
