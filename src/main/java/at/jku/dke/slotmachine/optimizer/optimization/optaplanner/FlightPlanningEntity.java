package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity(difficultyComparatorClass = FlightDifficultyComparator.class)
public class FlightPlanningEntity implements Comparable<FlightPlanningEntity> {
    // PlanningId is used for OptaPlanner (move thread count)
    @PlanningId
    private String flightId;

    private Flight wrappedFlight;

    @PlanningVariable(
        valueRangeProviderRefs = "slotRange",
        strengthComparatorClass = SlotStrengthComparator.class
    )
    private SlotProblemFact slot;

    public FlightPlanningEntity(Flight wrappedFlight) {
        this.wrappedFlight = wrappedFlight;
        this.flightId = wrappedFlight.getFlightId();
    }

    public FlightPlanningEntity() {
        // empty default constructor needed for OptaPlanner
    }

    public Flight getWrappedFlight() {
        return wrappedFlight;
    }

    public void setWrappedFlight(Flight wrappedFlight) {
        this.wrappedFlight = wrappedFlight;
    }

    public SlotProblemFact getSlot() {
        return slot;
    }

    public void setSlot(SlotProblemFact slot) {
        this.slot = slot;
    }

    @Override
    public int compareTo(FlightPlanningEntity flightPlanningEntity) {
        return this.getWrappedFlight().compareTo(flightPlanningEntity.getWrappedFlight());
    }
}
