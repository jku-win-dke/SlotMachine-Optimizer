package at.jku.dke.slotmachine.optimizer.domain;

public class PopulationOrder {
	private int maximum;
	private int[] order;
	
	public PopulationOrder() {
		
	}
	
	public PopulationOrder(int maximum, int[] order) {
		super();
		this.maximum = maximum;
		this.order = order;
	}
	
	public int getMaximum() {
		return maximum;
	}
	
	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}
	
	public int[] getOrder() {
		return order;
	}
	
	public void setOrder(int[] order) {
		this.order = order;
	}
}
