package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class FlightPrioritization {

	private int fitnessFunctionInvocations; // applications of score calculations, used for logger
	
    @ValueRangeProvider(id = "slotRange")
    @ProblemFactCollectionProperty
    private List<SlotProblemFact> slots;

    @PlanningEntityCollectionProperty
    private List<FlightPlanningEntity> flights;

    @PlanningScore
    private HardSoftScore score;

    public FlightPrioritization() {

    }
	
	public FlightPrioritization(List<SlotProblemFact> slots, List<FlightPlanningEntity> flights) {
        this.slots = slots;
        this.flights = flights;
        this.fitnessFunctionInvocations = 0;
    }

    public List<SlotProblemFact> getSlots() {
        return slots;
    }

    public List<FlightPlanningEntity> getFlights() {
        return flights;
    }

    public HardSoftScore getScore() {
        return score;
    }

    /**
     * Get the optimization result in terms of the domain classes.
     * @return a mapping of flights to slots
     */
    public Map<Flight, Slot> getResultMap() {
        return this.flights.stream()
                .collect(Collectors.toMap(f -> f.getWrappedFlight(), f -> f.getSlot().getWrappedSlot()));
    }

	public int getFitnessFunctionInvocations() {
        return fitnessFunctionInvocations;
	}

    public void incrementFitnessFunctionApplications() {
        this.fitnessFunctionInvocations++;
    }

    public Integer[] getFlightOrderArray(){
        Integer[] order = new Integer[this.getSlots().size()];
        int i = 0;
        for(var slot : this.slots){
            int j = 0;
            for(var flight : this.flights){
                if(flight.getSlot().getWrappedSlot().getTime().equals(slot.getWrappedSlot().getTime())){
                    order[i] = j;
                    break;
                }
                j++;
            }
            i++;
        }
        return order;
    }
}
