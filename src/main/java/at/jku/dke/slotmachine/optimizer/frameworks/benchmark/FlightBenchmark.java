package at.jku.dke.slotmachine.optimizer.frameworks.benchmark;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import at.jku.dke.slotmachine.optimizer.domain.Slot;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * Individual planning entity for Benchmark (OptaPlanner) framework.
 */
@PlanningEntity
public class FlightBenchmark {
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;
    private List<Slot> slots;

    @PlanningVariable(valueRangeProviderRefs = "possibleSlots")
    private Slot slot;

    public FlightBenchmark(String flightId, Instant scheduledTime, int[] weightMap, List<Slot> slots) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
        this.slots = slots;
    }

    public FlightBenchmark() {
    	// empty default constructor needed for OptaPlanner
    }
    
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public int[] getWeightMap() {
        return weightMap;
    }

    public void setWeightMap(int[] weightMap) {
        this.weightMap = weightMap;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
    
    /**
     * Returns possible slots of the current flight.
     * @return list of possible slots
     */
    @ValueRangeProvider(id = "possibleSlots")
    public List<Slot> getPossibleSlots() {
    	List<Slot> possibleSlots = new LinkedList<Slot>();
    	for (Slot s: slots) {
    		if (!s.getTime().isBefore(this.getScheduledTime())) {
    			possibleSlots.add(s);
    		}
    	}
    	return possibleSlots;
    }
}
