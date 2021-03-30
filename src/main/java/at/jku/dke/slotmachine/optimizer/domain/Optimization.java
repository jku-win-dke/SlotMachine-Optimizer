package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import java.util.List;
import java.util.UUID;

/***
 *
 */
public class Optimization {
	private UUID optId;
    private List<Flight> flightList;
    private List<Slot> slotList;
    private Class<? extends Run> optimization; // specify the application used to run the optimization.
    
	public Optimization(List<Flight> flightList, List<Slot> slotList, Class<? extends Run> optimization, UUID optId) {
		this.flightList = flightList;
		this.slotList = slotList;
		this.optimization = optimization;
		this.setOptId(optId);
	}

	public List<Flight> getFlightList() {
		return flightList;
	}

	public void setFlightList(List<Flight> flightList) {
		this.flightList = flightList;
	}

	public List<Slot> getSlotList() {
		return slotList;
	}

	public void setSlotList(List<Slot> slotList) {
		this.slotList = slotList;
	}

	public Class<? extends Run> getOptimization() {
		return optimization;
	}

	public void setOptimization(Class<? extends Run> optimization) {
		this.optimization = optimization;
	}

	public UUID getOptId() {
		return optId;
	}

	public void setOptId(UUID optId) {
		this.optId = optId;
	}

	

    
}
