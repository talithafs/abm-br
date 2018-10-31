package abm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import abm.agents.Agent;
import abm.agents.Bank;
import abm.agents.CapitalGoodsFirm;
import abm.agents.Consumer;
import abm.agents.ConsumptionGoodsFirm;
import abm.agents.EmployedConsumer;
import abm.agents.Firm;
import abm.agents.NonFinancialAgent;
import abm.agents.UnemployedConsumer;
import abm.markets.ConsumptionGoodsMarket;
import abm.markets.CreditMarket;
import abm.markets.LaborMarket;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;

public class Scheduler {
	
	private static Scheduler instance = new Scheduler();
	private ISchedule schedule ;
	private HashMap<Agent, List<ISchedulableAction>> map ;
	
	private int cycle  ;

	private Scheduler() {
		schedule = RunEnvironment.getInstance().getCurrentSchedule();
		map = new HashMap<Agent, List<ISchedulableAction>>();
		cycle = 0 ;
	}
	
	public static Scheduler getInstance() {
		return instance ;
	}

	public void schedulePlanning(ArrayList<Firm> firms, ArrayList<Consumer> consumers) {
		scheduleArray(ScheduleParameters.createOneTime(1), firms, "planProduction");
		schedule.schedule(ScheduleParameters.createOneTime(2), ConsumptionGoodsMarket.getInstance(), "initSelectionFunction");
		scheduleArray(ScheduleParameters.createOneTime(3), consumers, "calculateDemand");
	}
	
	public void scheduleCreditMarket(ArrayList<NonFinancialAgent> agents, ArrayList<Bank> banks) {
		scheduleArray(ScheduleParameters.createOneTime(1), banks, "calculateTotalCredit");
		scheduleArray(ScheduleParameters.createOneTime(1), agents, "calculateNeededCredt");
		scheduleArray(ScheduleParameters.createOneTime(2), banks, "calculateAvailableCredit");
		schedule.schedule(ScheduleParameters.createOneTime(3), CreditMarket.getInstance(), "match", banks, agents);
	}
	
	public void scheduleLaborMarket(ArrayList<Firm> firms) {
		scheduleArray(ScheduleParameters.createOneTime(4), firms, "postJobOfferings");
		scheduleArray(ScheduleParameters.createOneTime(5), firms, "payEmployees");
		schedule.schedule(ScheduleParameters.createOneTime(6), LaborMarket.getInstance(), "match");
	}
	
	public void rescheduleLaborMarket(ArrayList<Firm> firms) {
		int ticks = (int) schedule.getTickCount();
		schedule.schedule(ScheduleParameters.createOneTime(ticks + cycle), LaborMarket.getInstance(), "match", firms);
	}

	
	public void scheduleCapitalGoodsMarket(ArrayList<ConsumptionGoodsFirm> conFirms) {
		scheduleArray(ScheduleParameters.createOneTime(9), conFirms, "buyMachines");
	}
	
	public void scheduleProduction(ArrayList<ConsumptionGoodsFirm> conFirms, ArrayList<CapitalGoodsFirm> capFirms) {
		scheduleArray(ScheduleParameters.createOneTime(10), conFirms, "produceGoods");
		scheduleArray(ScheduleParameters.createOneTime(10), capFirms, "innovate");
	}
	
	public void scheduleTrade(ArrayList<ConsumptionGoodsFirm> conFirms, ArrayList<Consumer> consumers) {
		
	}
	
	public void scheduleAccounting() {
		
	}
	
	public void scheduleUnemployedConsumer(UnemployedConsumer unemp) {
		
	}
	
	public void scheduleEmployedConsumer(EmployedConsumer emp) {
		
	}
	
	public void unschedule(Agent agent) {
		
		List<ISchedulableAction> actions = map.get(agent);
		
		if(actions != null) {
			
			for(ISchedulableAction action : actions) {
				schedule.removeAction(action);
			}
			
			map.remove(agent);
		}
		
	}
	 
	private void scheduleArray(ScheduleParameters params, Iterable agents, String methodName, Object ... methodParams) {
		
		Iterator itr = agents.iterator() ;
		
		while(itr.hasNext()) {
			
			Agent agent = (Agent) itr.next() ;
			ISchedulableAction action = schedule.schedule(params, agent, methodName, methodParams);
			addAction(agent, action);
		}
	
	}
	
	private void addAction(Agent agent, ISchedulableAction action) {
		
		if(map.get(agent) == null) {
			ArrayList<ISchedulableAction> actions = new ArrayList<ISchedulableAction>();
			actions.add(action);
			map.put(agent, actions);
		}
		else {
			map.get(agent).add(action);
		}
	}


}
