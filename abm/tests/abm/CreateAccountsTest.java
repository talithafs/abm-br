package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.Consumer;
import abm.agents.Firm;
import abm.agents.Government;
import abm.agents.NonFinancialAgent;
import abm.components.Loan;
import abm.components.Statistics;
import abm.creators.BanksCreator;
import abm.creators.ConsumersCreator;
import abm.creators.FirmsCreator;
import abm.helpers.Constants.Keys;
import abm.helpers.Utils;
import abm.links.Account;
import abm.links.AccountCreator;
import abm.markets.CreditMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;

@SuppressWarnings("unchecked")
class CreateAccountsTest {
	
	private static HashMap<String,Double> params = null ;
	private static ConsumersCreator consumersCreator = null ;
	private static FirmsCreator firmsCreator = null ;
	private static BanksCreator banksCreator = null ;
	
	private static ArrayList<Consumer> consumers = null ;
	private static ArrayList<Firm> firms = null ;
	private static ArrayList<Bank> banks = null ;
	
	private static Context<Agent> context = null ;
	
	 @BeforeAll
	 static void initAll() {
		 
		 MetaParameters.initParameters(); 
		 Statistics stats = Statistics.getInstance() ;
		 stats.init(context);
		 
		 ArrayList<Integer[]> distFir = null ;
		 ArrayList<Integer[]> distInc = null ;
			
		 params = Utils.readParameters();
		 distFir = Utils.readDistribution("fir");
		 distInc = Utils.readDistribution("inc");
		 
		 Government.getInstance().setParams(params, stats);
		 
		 consumersCreator = new ConsumersCreator(params);
		 firmsCreator = new FirmsCreator(params);
		 banksCreator = new BanksCreator(params);
		 
		 consumers = consumersCreator.create(distInc);
		 firms = firmsCreator.create(distFir);
		 
	 }
	 
	 @Nested
	 @DisplayName("Consumers accounts")
	 @TestInstance(Lifecycle.PER_CLASS)
	 class ConsumersAccounts {
		 
		@BeforeEach
		 void init() {
			
			 banks = banksCreator.create(null);
			
			 context = new DefaultContext<Agent>();
			 context.setId("abm");
			 context.addAll(consumers);
			 context.addAll(banks);
			
			 NetworkBuilder<Agent> accountsBuilder = new NetworkBuilder<Agent>("accounts",context,false);
			 accountsBuilder.setEdgeCreator(new AccountCreator());
			 accountsBuilder.buildNetwork() ;
			 CreditMarket.getInstance().init(context, (Network<Agent>) context.getProjection("accounts")) ;
		 }
		
		 @Test
		 @DisplayName("Number of accounts")
		 void testNumber() {
			 
			 consumersCreator.createSavings(consumers, banks);
			 
			 double sum = 0 ;
			 
			 for(Bank bank : banks) {
				 sum += CreditMarket.getInstance().getEdges(bank).size() ;
			 }
			 
			 assertEquals(sum,consumers.size());
		 }
		 
		 @Test
		 @DisplayName("Equivalences bank debt/consumer assets and consumers assets/deposits")
		 void testAssets() {
			 
			 consumersCreator.createSavings(consumers, banks);
			 
			 for(Bank bank : banks) {
				 
				 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				 double conSum = 0 ;
				 
				 for(Account account : accounts) {
					 Consumer con = (Consumer) account.getTarget() ;
					 assertEquals(con.getAssets(), account.getDeposits());
					 conSum += account.getDeposits();
				 }
				 
				 assertEquals(conSum, bank.getDebt());
			 }
		 }
		 
		 @Test
		 @DisplayName("Equivalences bank assets/consumer debt and consumers debt/loans")
		 void testDebt() {
			 
			 consumersCreator.createLoans(consumers, banks);
			 
			 for(Bank bank : banks) {
				
				 ArrayList<NonFinancialAgent> cons = CreditMarket.getInstance().getAdjacent(bank);
				 double sumCons = 0 ;
				 
				 for(NonFinancialAgent con : cons) {
					 sumCons += con.getDebt() ;
				 }
				 
				 assertEquals(sumCons, bank.getAssets());
			 }
			 
			 for(Consumer con : consumers) {
				 
				 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(con);
				 double sumLoans  = 0 ;
				 
				 for(Account account : accounts) {
					 sumLoans += account.getLoansAmount() ;
				 }
				 
				 assertEquals(sumLoans, con.getDebt());
			 }
		 }
		 
		 @Test 
		 @DisplayName("Interest rates")
		 void testInterest() {
			 
			 consumersCreator.createSavings(consumers, banks);
			 consumersCreator.createLoans(consumers, banks);
			 
			 double meanDeps = Government.getInstance().getParam(Keys.MEAN_SAVS_INT);
			 double meanLoans = Government.getInstance().getParam(Keys.MEAN_INT_CONS);
			 
			 for(Consumer con : consumers) {
				 Bank bank = CreditMarket.getInstance().getBankWithDeposits(con);
				 assertEquals(bank.getInterestOnDeposits(),meanDeps);
			 }
			 
			 double sum = 0 ;
			 int count = 0 ;
			 
			 for(Bank bank : banks) {
				 
				 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				 for(Account account : accounts) {
					 
					 for(Loan loan : account.getLoans()) {
						 sum += loan.getInterest() ;
						 count++ ;
					 }
				 }
			 }
			 
			 double mean = sum / count ;
			 assertTrue(mean < 1.05*meanLoans && mean > 0.95*meanLoans) ;
		 }
	 }
	 
	 @Nested
	 @DisplayName("Firms accounts")
	 @TestInstance(Lifecycle.PER_CLASS)
	 class FirmsAccounts {
		 
		@BeforeEach
		 void init() {
			
			 banks = banksCreator.create(null);
			
			 context = new DefaultContext<Agent>();
			 context.setId("abm");
			 context.addAll(firms);
			 context.addAll(banks);
			
			 NetworkBuilder<Agent> accountsBuilder = new NetworkBuilder<Agent>("accounts",context,false);
			 accountsBuilder.buildNetwork() ;
			 CreditMarket.getInstance().init(context, (Network<Agent>) context.getProjection("accounts")) ;
		 }
		 
		 @Test
		 @DisplayName("Number of accounts")
		 void testNumber() {
			 
			 firmsCreator.createSavings(firms, banks);
			 
			 double sum = 0 ;
			 
			 for(Bank bank : banks) {
				 sum += CreditMarket.getInstance().getEdges(bank).size() ;
			 }
			 
			 assertEquals(sum,firms.size());
		 }
		 
		 @Test
		 @DisplayName("Equivalences bank assets/firms assets and firms assets/deposits")
		 void testEquivalences() {
			 
			 firmsCreator.createSavings(firms, banks);
			 
			 for(Bank bank : banks) {
				 
				 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				 double firSum = 0 ;
				 
				 for(Account account : accounts) {
					 Firm firm = (Firm) account.getTarget() ;
					 assertEquals(firm.getAssets(), account.getDeposits());
					 firSum += account.getDeposits();
				 }
				 
				 assertEquals(firSum, bank.getDebt());
			 }
		 }
		 
		 @Test
		 @DisplayName("Equivalences bank assets/firm debt and firms debt/loans")
		 void testDebt() {
			 
			 firmsCreator.createLoans(firms, banks);
			 
			 for(Bank bank : banks) {
				
				 ArrayList<NonFinancialAgent> cons = CreditMarket.getInstance().getAdjacent(bank);
				 double sumCons = 0 ;
				 
				 for(NonFinancialAgent con : cons) {
					 sumCons += con.getDebt() ;
				 }
				 
				 assertEquals(sumCons, bank.getAssets());
			 }
			 
			 for(Firm con : firms) {
				 
				 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(con);
				 double sumLoans  = 0 ;
				 
				 for(Account account : accounts) {
					 sumLoans += account.getLoansAmount() ;
				 }
				 
				 assertEquals(sumLoans, con.getDebt());
			 }
		 }
		 
		 @Test 
		 @DisplayName("Interest rates")
		 void testInterest() {
			 
			 firmsCreator.createSavings(firms, banks);
			 firmsCreator.createLoans(firms, banks);
			 
			 double meanDeps = Government.getInstance().getParam(Keys.MEAN_SAVS_INT);
			 double meanLoans = Government.getInstance().getParam(Keys.MEAN_INT_FIRMS);
			 
			 for(Firm con : firms) {
				 Bank bank = CreditMarket.getInstance().getBankWithDeposits(con);
				 assertEquals(bank.getInterestOnDeposits(),meanDeps);
			 }
			 
			 double sum = 0 ;
			 int count = 0 ;
			 
			 for(Bank bank : banks) {
				 
				 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(bank);
				 for(Account account : accounts) {
					 
					 for(Loan loan : account.getLoans()) {
						 sum += loan.getInterest() ;
						 count++ ;
					 }
				 }
			 }
			 
			 double mean = sum / count ;
			 assertTrue(mean < 1.05*meanLoans && mean > 0.95*meanLoans) ;
		 }
	 }
}
