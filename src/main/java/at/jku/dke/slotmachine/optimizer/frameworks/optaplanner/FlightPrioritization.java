package at.jku.dke.slotmachine.optimizer.frameworks.optaplanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private Map<Flight,Slot> sequence = null;
	
    @ValueRangeProvider(id = "slotRange")
    @ProblemFactCollectionProperty
    private List<Slot> slots;

    @PlanningEntityCollectionProperty
    private List<Flight> flights;

    @PlanningScore
    private HardSoftScore score;

    private FlightPrioritization() {

    }
	
	public FlightPrioritization(List<Slot> slots, List<Flight> flights) {
        this.slots = slots;
        this.flights = flights;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public Map<Flight, Slot> getSequence() {
        if(this.sequence == null) {
            this.sequence = new HashMap<Flight, Slot>();

            for (Flight f : flights) {
                this.sequence.put(f, f.getSlot());
            }
        }

        return this.sequence;
    }
}
