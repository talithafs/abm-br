package abm.components;

public class Order {

	protected int quantity ;
	
	public int getQuantity() {
		return quantity;
	}
	
	public void increaseQuantity(int by) {
		this.quantity += by ;
	}
	
	public void decreaseQuantity(int by) {
		this.quantity -= by ;
	}
}
