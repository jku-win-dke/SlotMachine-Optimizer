package at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import at.jku.dke.slotmachine.optimizer.domain.Slot;

import java.time.Instant;

@PlanningEntity(difficultyComparatorClass = FlightDifficultyComparator.class)
public class FlightBenchmarkOptaPlanner {
	@PlanningId
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;

    @PlanningVariable(valueRangeProviderRefs = "slotRange", 
    		strengthComparatorClass = SlotStrengthComparator.class)
    private Slot slot;

    public FlightBenchmarkOptaPlanner(String flightId, Instant scheduledTime, int[] weightMap, Slot slot) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
        this.slot = slot;
    }

    public FlightBenchmarkOptaPlanner() {
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
}
