package at.jku.dke.slotmachine.optimizer.algorithms.optaplanner;

import java.time.Instant;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO;

// used as a subclass of FlightDTO to be used by OptaPlanner
// as there is no class for slot<->flight assignments
// -> therefore FlightOptaPlanner gets the added attribute 'slot'
@PlanningEntity
public class FlightOptaPlanner extends FlightDTO{

    public FlightOptaPlanner(String flightId, Instant scheduledTime, int[] weightMap) {
		super(flightId, scheduledTime, weightMap);
	}

    public FlightOptaPlanner() {
    	super();
    }
    
	@PlanningVariable(valueRangeProviderRefs = "slotRange")
    private SlotDTO slot;

    public SlotDTO getSlot() {
        return slot;
    }

    public void setSlot(SlotDTO slot) {
        this.slot = slot;
    }
}
