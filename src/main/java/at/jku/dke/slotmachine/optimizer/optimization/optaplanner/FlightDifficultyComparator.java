package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import java.util.Comparator;

// required for construction heuristic phase, to allow all different types
// required for FIRST_FIT_DECREASING, WEAKEST_FIT_DECREASING, STRONGEST_FIT_DECREASING
public class FlightDifficultyComparator implements Comparator<FlightPlanningEntity>{

	@Override
	public int compare(FlightPlanningEntity o1, FlightPlanningEntity o2) {
		// no Flight is supposed to be "more difficult" for the solver currently
		return 0;
	}

}
