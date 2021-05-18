package at.jku.dke.slotmachine.optimizer.frameworks.benchmark;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;

public class BenchmarkRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Runs the Benchmark with OptaPlanner framework, solver_config_benchmark.xml and given data.
	 * 
	 * @param flights available flights
	 * @param slots available slots
	 * @return map with flights assigned to slots
	 */
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots) {
		logger.info("Start optimization to determine benchmark (using OptaPlanner).");
		SolverFactory<FlightPrioritization> solverFactory = SolverFactory.createFromXmlResource("solver_config_benchmark.xml");	

        Solver<FlightPrioritization> solver = solverFactory.buildSolver();
        // adds event listener to state the best current solution
        solver.addEventListener(new SolverEventListener<FlightPrioritization>() {
			@Override
			public void bestSolutionChanged(BestSolutionChangedEvent<FlightPrioritization> event) {
				logger.info("A new best solution has been found with the score: " + event.getNewBestScore());
		        logger.info("print current sequence:\n");
		        for(FlightBenchmark f : event.getNewBestSolution().getFlights()) {
		        	if (f.getSlot() != null) {
		        		logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
		        	} else {
		        		logger.info(f.getFlightId() + " : no slot assigned!");
		        	}
		        }
			}
        });
        
        for(int i = 0; i < flights.size(); i++) {
        	flights.get(i).setSlot(slots.get(i));
        }

        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);
        logger.debug("Score Calculations applications for unsolved: " + unsolvedFlightPrioritization.getApplications());
        FlightPrioritization solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        logger.info("\n\n === Optimization ended ===");
        logger.debug("Score Calculations applications for solved: " + solvedFlightPrioritization.getApplications());
        logger.info("print solved sequence:\n");
        for(FlightBenchmark f : solvedFlightPrioritization.getFlights()) {
        	if (f.getSlot() != null) {
        		logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
        	} else {
        		logger.info(f.getFlightId() + " : no slot assigned!");
        	}
        }

        logger.info("Score of solved Slotsequence: " + solvedFlightPrioritization.getScore());
        Map<Flight, Slot> solution = new LinkedHashMap<Flight, Slot>();

        for(Map.Entry<FlightBenchmark, Slot> entry: solvedFlightPrioritization.getSequence().entrySet()) {
            Flight f = new Flight(entry.getKey().getFlightId(),
    	   		                  entry.getKey().getScheduledTime(),
    			                  entry.getKey().getWeightMap());
    	    solution.put(f, entry.getValue());
        }
       
       logger.info("Size: " + solution.keySet().size());
       
       return solution;
        
	}
}
