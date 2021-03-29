package at.jku.dke.slotmachine.optimizer.algorithms.optaplanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO;

@PlanningSolution
public class SlotSequence {

	private Map<FlightOptaPlanner,SlotDTO> sequence = null;
	
    @ValueRangeProvider(id = "slotRange")
    @ProblemFactCollectionProperty
    private List<SlotDTO> slots;

    @PlanningEntityCollectionProperty
    private List<FlightOptaPlanner> flights;

    @PlanningScore
    private HardSoftScore score;

    private SlotSequence() {

    }
	
	public SlotSequence(List<SlotDTO> slots, List<FlightOptaPlanner> flightsOptaPlanner) {
        this.slots = slots;
        this.flights = flightsOptaPlanner;
    }

    public List<SlotDTO> getSlots() {
        return slots;
    }

    public List<FlightOptaPlanner> getFlights() {
        return flights;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public Map<FlightOptaPlanner, SlotDTO> getSequence() {
        if(this.sequence == null) {
            this.sequence = new HashMap<FlightOptaPlanner, SlotDTO>();

            for (FlightOptaPlanner f : flights) {
                this.sequence.put(f, f.getSlot());
            }
        }

        return this.sequence;
    }
}
