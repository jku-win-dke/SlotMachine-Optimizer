package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OptaplannerOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    public OptaplannerOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
    }

    @Override
    public Map<Flight, Slot> run() {
        // TODO make it work with configuration specified via REST interface
        SolverFactory<FlightPrioritization> solverFactory = SolverFactory.createFromXmlResource("solver_config.xml");

        Solver<FlightPrioritization> solver = solverFactory.buildSolver();

        logger.info("Compute weight map for flights.");
        for(int i = 0; i < this.getFlights().length; i++) {
            this.getFlights()[i].computeWeightMap(this.getSlots());
        }

        logger.info("Get OptaPlanner domain model.");
        List<FlightPlanningEntity> flights = Arrays.stream(this.getFlights())
                .map(flight -> new FlightPlanningEntity(flight)).toList();

        List<SlotProblemFact> slots = Arrays.stream(this.getSlots())
                .map(slot -> new SlotProblemFact(slot)).toList();


        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);

        FlightPrioritization solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);

        logger.info("Finished optimization with Optaplanner. Score of solution is: " + solvedFlightPrioritization.getScore());

        return solvedFlightPrioritization.getResultMap();
    }

    @Override
    public OptimizationConfiguration getDefaultConfiguration() {
        return null;
    }

    @Override
    public OptimizationConfiguration getConfiguration() {
        return null;
    }

    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {

    }

    @Override
    public OptimizationStatistics getStatistics() {
        return null;
    }
}
