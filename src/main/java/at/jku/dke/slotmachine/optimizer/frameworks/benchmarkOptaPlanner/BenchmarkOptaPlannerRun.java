package at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner;

import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

public class BenchmarkOptaPlannerRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Runs the Benchmark with OptaPlanner framework, solver_config_benchmarkOptaPlanner.xml and given data.
	 * 
	 * @param flights available flights
	 * @param slots available slots
	 * @return map with flights assigned to slots
	 */
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots) {
		logger.info("Benchmark Factory of OptaPlanner is used - no direct result will be returned - expect server error!");
		PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource("solver_config_benchmarkOptaPlanner.xml");
        for(int i = 0; i < flights.size(); i++) {
        	flights.get(i).setSlot(slots.get(i));
        }
		PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(new FlightPrioritization(slots, flights));
		benchmark.benchmarkAndShowReportInBrowser();
		return null;        
	}
}
