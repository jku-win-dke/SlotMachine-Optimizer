package at.jku.dke.slotmachine.optimizer.algorithms.optaplanner;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import at.jku.dke.slotmachine.optimizer.service.dto.*;

public class OptaPlannerApplication {

	private static final Logger logger = LogManager.getLogger();
	
	public static Map<FlightDTO,SlotDTO> run(List<FlightDTO> flights, List<SlotDTO> slots) {
		logger.info("Uses optaplanner algorithm.");
		SolverFactory<SlotSequence> solverFactory = SolverFactory.createFromXmlResource("solver_config.xml");

        Solver<SlotSequence> solver = solverFactory.buildSolver();

        List<FlightOptaPlanner> flightsOptaPlanner = new LinkedList<FlightOptaPlanner>();
        for (FlightDTO f: flights) {
        	flightsOptaPlanner.add(new FlightOptaPlanner(f.getFlightId(),f.getScheduledTime(),
        			f.getWeightMap()));
        }
        
        for(int i = 0; i < flightsOptaPlanner.size(); i++) {
        	flightsOptaPlanner.get(i).setSlot(slots.get(i));
        }

        SlotSequence unsolvedSlotSequence = new SlotSequence(slots, flightsOptaPlanner);

        SlotSequence solvedSlotSequence = solver.solve(unsolvedSlotSequence);

        logger.info("print solved sequence:\n");
        for(FlightOptaPlanner f : solvedSlotSequence.getFlights()) {
            logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
        }

        logger.info("Score of solved Slotsequence: " + solvedSlotSequence.getScore());
        Map<FlightDTO, SlotDTO> solution = new LinkedHashMap<FlightDTO, SlotDTO>();
       for(Map.Entry<FlightOptaPlanner, SlotDTO> entry: solvedSlotSequence.getSequence().entrySet()) {
    	   FlightDTO f = new FlightDTO(entry.getKey().getFlightId(),
    			   entry.getKey().getScheduledTime(),
    			   entry.getKey().getWeightMap());
    	   solution.put(f, entry.getValue());
       }
       
       logger.info("Size: " + solution.keySet().size());

       // can be used to check created solution object
       /*for(FlightDTO f : solution.keySet()) {
           System.out.println(f.getFlightId() + " " + solution.get(f).getTime());
       }*/
       
       return solution;
        
	}
}
