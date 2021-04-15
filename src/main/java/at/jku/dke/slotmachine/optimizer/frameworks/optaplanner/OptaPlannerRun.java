package at.jku.dke.slotmachine.optimizer.frameworks.optaplanner;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import at.jku.dke.slotmachine.optimizer.service.dto.*;

public class OptaPlannerRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots) {
		logger.info("Start optimization using OptaPlanner framework.");
		SolverFactory<FlightPrioritization> solverFactory = SolverFactory.createFromXmlResource("solver_config.xml");
		/*SolverConfig sc = new SolverConfig();
		try {
			sc.setSolutionClass(Class.forName("at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.FlightPrioritization"));
			List<Class<?>> classListEntity = new LinkedList<Class<?>>();
			classListEntity.add(Class.forName("at.jku.dke.slotmachine.optimizer.domain.Flight"));
			sc.setEntityClassList(classListEntity);
			ScoreDirectorFactoryConfig sdfc = new ScoreDirectorFactoryConfig();
			sdfc.setEasyScoreCalculatorClass((Class<? extends EasyScoreCalculator>) Class.forName("at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.FlightPrioritizationEasyScoreCalculator"));
			sc.setScoreDirectorFactoryConfig(sdfc);
			TerminationConfig tc = new TerminationConfig();
			tc.setSecondsSpentLimit((long) 10);
			sc.setTerminationConfig(tc);
			solverFactory = SolverFactory.create(sc);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			solverFactory = SolverFactory.createFromXmlResource("solver_config.xml");
			
		}*/
		

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
