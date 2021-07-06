package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import java.util.Comparator;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
// required for construction heuristic phase, to allow all different types
// required for FIRST_FIT_DECREASING, WEAKEST_FIT_DECREASING, STRONGEST_FIT_DECREASING
public class FlightDifficultyComparator implements Comparator<Flight>{

	@Override
	public int compare(Flight o1, Flight o2) {
		// no Flight is supposed to be "more difficult" for the solver currently
		return 0;
	}

}
