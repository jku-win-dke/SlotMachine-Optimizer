package at.jku.dke.slotmachine.optimizer.optimization;

import at.jku.dke.slotmachine.optimizer.domain.*;

import java.util.Map;
import java.util.UUID;

/***
 *
 */
public abstract class Optimization {
	private UUID optId;
    private Flight[] flights;
    private Slot[] slots;

	public Optimization(Flight[] flights, Slot[] slots) {
		this.flights = flights;
		this.slots = slots;
	}

	/**
	 * Runs the optimization using the underlying framework, returning the optimized flight list.
	 * @return an optimized mapping from flight to slots
	 */
	public abstract Map<Flight, Slot> run();

	public Flight[] getFlights() {
		return flights;
	}

	public void setFlights(Flight[] flights) {
		this.flights = flights;
	}

	public Slot[] getSlots() {
		return slots;
	}

	public void setSlots(Slot[] slots) {
		this.slots = slots;
	}

	public UUID getOptId() {
		return optId;
	}

	public void setOptId(UUID optId) {
		this.optId = optId;
	}

	public abstract OptimizationConfiguration getDefaultConfiguration();

	public abstract OptimizationConfiguration getConfiguration();

	public abstract void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException;

	public abstract OptimizationStatistics getStatistics();
}
