package at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner;

import java.util.Comparator;

import at.jku.dke.slotmachine.optimizer.domain.Slot;

/**
 * Required for Weakest Fit (OptaPlanner) to determine the strength
 * of the Slot (PlanningVariable)
 */
public class SlotStrengthComparator implements Comparator<Slot> {

	@Override
	public int compare(Slot o1, Slot o2) {
		// no slot is supposed "stronger" than the other
		return 0;
	}
	
}
