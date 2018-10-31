package abm.components;

import abm.helpers.Utils;

public class NewMachine extends Machine {
	
	private double price ;
	private int soldUnits ;
	
	public NewMachine(long capacity, double maxWages, double price, int units) {
		super(capacity, maxWages);
		this.price = price;
		this.soldUnits = units;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getUnits() {
		return soldUnits;
	}

	public void increaseUnits(int by) {
		this.soldUnits += by;
	}
	
	public UsedMachine toUsedMachine() {
		return new UsedMachine(this.capacity, this.maxWages);
	}
	
	@Override
	public String toString() {
		
		String strAgent = super.toString() ;
		
		Integer units = this.soldUnits ;
		Double price = this.price ;

		String[][] fields = { { "Units", units.toString()}, 
							{"Price", price.toString()} } ;
		
	    return strAgent + Utils.getComponentDescriptor(fields, false) ;
	}

}
