package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import java.util.Comparator;

import at.jku.dke.slotmachine.optimizer.domain.Slot;

//required for construction heuristic phase, to allow all different types
//required for WEAKEST_FIT, WEAKEST_FIT_DECREASING, STRONGEST_FIT,
// STRONGEST_FIT_DECREASING
public class SlotStrengthComparator implements Comparator<Slot> {

	@Override
	public int compare(Slot o1, Slot o2) {
		// no slot is supposed "stronger" than the other
		return 0;
	}
	
}
