package abm.components;

import abm.agents.ConsumptionGoodsFirm;

public class ConsumptionGoodsOrder extends Order {
	
	private ConsumptionGoodsFirm conFirm ;
	
	public ConsumptionGoodsOrder(ConsumptionGoodsFirm conFirm, int quantity) {
		this.conFirm = conFirm;
		this.quantity = quantity;
	}

	public ConsumptionGoodsFirm getConFirm() {
		return conFirm;
	}

	@Override
	public boolean equals(Object other) {
		
		if(other instanceof ConsumptionGoodsOrder) {
			
			if(this.conFirm.equals(((ConsumptionGoodsOrder) other).conFirm)){
				return true ;
			}
			return false ;
		}
		
		return false ;
	}
}
