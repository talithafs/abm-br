package abm.components;

import abm.agents.CapitalGoodsFirm;

public class MachineOrder extends Order implements Comparable<MachineOrder> {
	
	private NewMachine machine ;
	private CapitalGoodsFirm capFirm ;
	
	
	public MachineOrder(NewMachine machine, CapitalGoodsFirm capFirm, int quantity) {
		this.machine = machine;
		this.capFirm = capFirm;
		this.quantity = quantity;
	}
	
	public NewMachine getMachine() {
		return machine;
	}
	public CapitalGoodsFirm getCapFirm() {
		return capFirm;
	}
	
	@Override
	public boolean equals(Object other) {
		
		if(other instanceof MachineOrder) {
			
			boolean cond1 = ((MachineOrder) other).machine.equals(this.machine) ;
			boolean cond2 = ((MachineOrder) other).capFirm.equals(this.capFirm) ;
			
			if(cond1 && cond2) {
				return true ;
			}
			
			return false ;
		}
		
		return false ;
	}

	@Override
	public int compareTo(MachineOrder other) {
		if(this.machine.getEfficiency() == other.machine.getEfficiency()) {
			return 0 ;
		} 
		else if(this.machine.getEfficiency() < other.machine.getEfficiency()) {
			return -1 ;
		} 
		else {
			return 1 ;
		}
	}
}
