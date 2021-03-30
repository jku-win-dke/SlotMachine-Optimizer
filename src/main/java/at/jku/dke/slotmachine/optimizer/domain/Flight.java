package at.jku.dke.slotmachine.optimizer.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.Instant;

@PlanningEntity
public class Flight {
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;

    @PlanningVariable(valueRangeProviderRefs = "slotRange")
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
}
