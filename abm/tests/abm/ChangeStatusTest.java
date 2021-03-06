package abm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import abm.agents.Agent;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.UnemployedConsumer;
import abm.links.Account;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.CreditMarket;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.space.graph.RepastEdge;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ChangeStatusTest {

	protected Context<Agent> context ; 
	
	@BeforeEach
	void init() {
		
		context = new DefaultContext();
		Controller builder = new Controller();
		context = builder.build(context);
	}
	
	@Test
	@DisplayName("Test sizes")
	void testChange() {
		  
		int empsOldSize = context.getObjects(EmployedConsumer.class).size() ;
		int unempsOldSize = context.getObjects(UnemployedConsumer.class).size() ;
		 
		Iterable<Agent> emps = context.getRandomObjects(EmployedConsumer.class, 5);
		Iterator<Agent> itr = emps.iterator() ;
		 
		 while(itr.hasNext()) {
			 EmployedConsumer emp = (EmployedConsumer) itr.next() ;
			 Consumer.changeStatus(emp);
		 }
		 
		 int empsNewSize = context.getObjects(EmployedConsumer.class).size() ;
		 int unempsNewSize = context.getObjects(UnemployedConsumer.class).size() ;
		 
		 assertEquals(empsNewSize, empsOldSize - 5);
		 assertEquals(unempsNewSize, unempsOldSize + 5);
	 }
	
	@Test
	@DisplayName("Test accounts")
	void accounts() {
		 
		Iterable<Agent> emps = context.getRandomObjects(EmployedConsumer.class, 5);
		Iterator<Agent> itr = emps.iterator() ;
		 
		 while(itr.hasNext()) {
			 EmployedConsumer emp = (EmployedConsumer) itr.next() ;
			 ArrayList<Account> accounts = CreditMarket.getInstance().getEdges(emp);
			 int oldN = accounts.size() ;
			 
			 UnemployedConsumer unemp = Consumer.changeStatus(emp);
			 
			 ArrayList<Account> emptyAccs = CreditMarket.getInstance().getEdges(emp);
			 assertEquals(emptyAccs.size(), 0);
			 
			 ArrayList<Account> accs = CreditMarket.getInstance().getEdges(unemp);
			 int newN = accs.size() ;
			 
			 assertEquals(oldN, newN) ;
			 
			 for(Account acc : accs) {
				 assertTrue(accounts.contains(acc));
			 }
		 }
	 }
	
	@Test
	@DisplayName("Test consumption goods market")
	void market() {
		 
		Iterable<Agent> emps = context.getRandomObjects(EmployedConsumer.class, 5);
		Iterator<Agent> itr = emps.iterator() ;
		 
		 while(itr.hasNext()) {
			 EmployedConsumer emp = (EmployedConsumer) itr.next() ;
			 ArrayList<ConsumptionGoodsFirm> empSup = ConsumptionGoodsMarket.getInstance().getAdjacent(emp);
			 
			 UnemployedConsumer unemp = Consumer.changeStatus(emp);
			 ArrayList<ConsumptionGoodsFirm> unempSup = ConsumptionGoodsMarket.getInstance().getAdjacent(unemp);
			 
			 int size = ConsumptionGoodsMarket.getInstance().getEdges(emp).size();
			 
			 assertEquals(size, 0);
			 assertEquals(empSup.size(), unempSup.size());
			 
			 for(ConsumptionGoodsFirm sup : unempSup) {
				 assertTrue(empSup.contains(sup));
			 }
		 }
	 }
}
