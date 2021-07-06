package at.jku.dke.slotmachine.optimizer.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightDifficultyComparator;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.SlotStrengthComparator;

import java.time.Instant;

// TODO make this class agnostic to optimization framework
@PlanningEntity(difficultyComparatorClass = FlightDifficultyComparator.class)
public class Flight {
	// PlanningId is used for OptaPlanner (move thread count)
	@PlanningId
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;
    private Margins margins;

    @PlanningVariable(valueRangeProviderRefs = "slotRange",
                      strengthComparatorClass = SlotStrengthComparator.class)
    private Slot slot;

    public Flight(String flightId, Instant scheduledTime, int[] weightMap) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
    }

    public Flight() {
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

    public Margins getMargins() { return margins; }

    public void setMargins(Margins margins) { this.margins = margins; }
}
