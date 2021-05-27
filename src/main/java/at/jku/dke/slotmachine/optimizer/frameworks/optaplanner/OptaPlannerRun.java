package at.jku.dke.slotmachine.optimizer.frameworks.optaplanner;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.OptaPlannerConfig;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import at.jku.dke.slotmachine.optimizer.service.dto.*;

public class OptaPlannerRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Run OptaPlanner, without prior configuration, use solver_config.xml as configuration file
	 * @param flights
	 * @param slots
	 * @return Map (solution)
	 */
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots) {
		logger.info("Start optimization using OptaPlanner framework.");
		SolverFactory<FlightPrioritization> solverFactory = SolverFactory.createFromXmlResource("solver_config.xml");

        Solver<FlightPrioritization> solver = solverFactory.buildSolver();
        
        for(int i = 0; i < flights.size(); i++) {
        	flights.get(i).setSlot(slots.get(i));
        }

        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);
        logger.info("Score Calculations applications for unsolved: " + unsolvedFlightPrioritization.getApplications());
        FlightPrioritization solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        logger.info("Score Calculations applications for solved: " + solvedFlightPrioritization.getApplications());
        logger.info("print solved sequence:\n");
        for(Flight f : solvedFlightPrioritization.getFlights()) {
            logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
        }

        logger.info("Score of solved Slotsequence: " + solvedFlightPrioritization.getScore());
        Map<Flight, Slot> solution = new LinkedHashMap<Flight, Slot>();

        for(Map.Entry<Flight, Slot> entry: solvedFlightPrioritization.getSequence().entrySet()) {
            Flight f = new Flight(entry.getKey().getFlightId(),
    	   		                  entry.getKey().getScheduledTime(),
    			                  entry.getKey().getWeightMap());
    	    solution.put(f, entry.getValue());
        }
       
       logger.info("Size: " + solution.keySet().size());
       
       return solution;
        
	}
	
	/**
	 * Run OptaPlanner, with prior configuration, do not use solver_config.xml as configuration file
	 * @param flights
	 * @param slots
	 * @param optConfig configuration of OptaPlanner
	 * @return Map (solution)
	 */
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots, OptaPlannerConfig optConfig) {
		logger.info("Start optimization using OptaPlanner framework - with configuration due to optaPlannerConfig.");
		SolverFactory<FlightPrioritization> solverFactory = null;
		
		// configured SolverConfig
		SolverConfig sc = new SolverConfig();
		
		// move thread count (how many threads act in parallel)
		if (optConfig.getMoveThreadCount() != null) {
			sc.setMoveThreadCount(optConfig.getMoveThreadCountString());
			logger.info("Move Thread Count set to " + sc.getMoveThreadCount() + ".");
		} else {
			logger.info("Move Thread Count was not set. Default of NONE is used.");
			sc.setMoveThreadCount("NONE");
		}
		
		// environment mode
		if (optConfig.getEnvironmentMode() != null) {
			sc.setEnvironmentMode(optConfig.getEnvironmentModeSolver());
			logger.info("Environment Mode set to "+ sc.getEnvironmentMode() + ".");
		} else {
			logger.info("Environment Mode was not set. Default of REPRODUCIBLE is used.");
			sc.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
		}
		
		// Solution class, entity class (list) and easy score calculator are used with default values
		sc.setSolutionClass(at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.FlightPrioritization.class);
		List<Class<?>> classListEntity = new LinkedList<Class<?>>();
		classListEntity.add(at.jku.dke.slotmachine.optimizer.domain.Flight.class);
		sc.setEntityClassList(classListEntity);
		ScoreDirectorFactoryConfig sdfc = new ScoreDirectorFactoryConfig();
		sdfc.setEasyScoreCalculatorClass(at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.FlightPrioritizationEasyScoreCalculator.class);
		sc.setScoreDirectorFactoryConfig(sdfc);
		
		// termination
		TerminationConfig tc = new TerminationConfig();
		if (optConfig.getTermination() != null) {
			// default values, currently (use set values from optaPlannerConfig)
			tc.setUnimprovedSecondsSpentLimit((long) 10);
			sc.setTerminationConfig(tc);
		} else {
			logger.info("No Termination method has been set. Default of UNIMPROVED_SECONDS_SPENT_LIMIT (10 seconds) is used.");
			tc.setUnimprovedSecondsSpentLimit((long) 10);
			sc.setTerminationConfig(tc);
		}
		
		// construction heuristics phase
		
		// local search phase

		solverFactory = SolverFactory.create(sc);
		

        Solver<FlightPrioritization> solver = solverFactory.buildSolver();
        
        for(int i = 0; i < flights.size(); i++) {
        	flights.get(i).setSlot(slots.get(i));
        }

        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);
        logger.info("Score Calculations applications for unsolved: " + unsolvedFlightPrioritization.getApplications());
        FlightPrioritization solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        logger.info("Score Calculations applications for solved: " + solvedFlightPrioritization.getApplications());
        logger.info("print solved sequence:\n");
        for(Flight f : solvedFlightPrioritization.getFlights()) {
            logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
        }

        logger.info("Score of solved Slotsequence: " + solvedFlightPrioritization.getScore());
        Map<Flight, Slot> solution = new LinkedHashMap<Flight, Slot>();

        for(Map.Entry<Flight, Slot> entry: solvedFlightPrioritization.getSequence().entrySet()) {
            Flight f = new Flight(entry.getKey().getFlightId(),
    	   		                  entry.getKey().getScheduledTime(),
    			                  entry.getKey().getWeightMap());
    	    solution.put(f, entry.getValue());
        }
       
       logger.info("Size: " + solution.keySet().size());
       
       return solution;
        
	}
}
