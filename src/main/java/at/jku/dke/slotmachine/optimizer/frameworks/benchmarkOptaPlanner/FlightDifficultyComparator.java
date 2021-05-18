package at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner;

import java.util.Comparator;

public class FlightDifficultyComparator implements Comparator<FlightBenchmarkOptaPlanner>{

	@Override
	public int compare(FlightBenchmarkOptaPlanner o1, FlightBenchmarkOptaPlanner o2) {
		// no Flight is supposed to be "more difficult" for the solver currently
		return 0;
	}

}
