package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.PrivacyPreservingSolverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OptaplannerOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private OptaplannerOptimizationConfiguration configuration = null;
    private OptaplannerOptimizationStatistics statistics = null;

    public OptaplannerOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);

        this.statistics = new OptaplannerOptimizationStatistics();
    }

    @Override
    public Map<Flight, Slot> run() {
        SolverConfig solverConfig = null;

        if(this.getConfiguration() != null) {
            solverConfig = this.getConfiguration().getSolverConfig();
        }

        if(this.getConfiguration() == null || solverConfig == null) {
            solverConfig = this.getDefaultConfiguration().getSolverConfig();
        }

        if(this.getConfiguration() != null && this.getConfiguration().getSecondsSpentLimit() > 0) {
            logger.info("Setting seconds spent limit to " + this.getConfiguration().getSecondsSpentLimit());
            solverConfig.getTerminationConfig().setSecondsSpentLimit(this.getConfiguration().getSecondsSpentLimit());
        }

        logger.info("Create the solver factory.");

        // Use custom solver factory if configuration-name indicates it
        SolverFactory<FlightPrioritization> solverFactory = null;
        if(this.getConfiguration().getConfigurationName().contains("CUSTOM")){
            solverFactory = new PrivacyPreservingSolverFactory<>(solverConfig);
        }else{ // Otherwise use default factory
           SolverFactory.create(solverConfig);
        }

        logger.info("Build the solver.");
        Solver<FlightPrioritization> solver = solverFactory.buildSolver();


        logger.info("Compute weight map for flights.");
        for(int i = 0; i < this.getFlights().length; i++) {
            this.getFlights()[i].computeWeightMap(this.getSlots());
        }

        logger.info("Get OptaPlanner domain model.");
        List<FlightPlanningEntity> flights = Arrays.stream(this.getFlights())
                .map(flight -> new FlightPlanningEntity(flight)).sorted().toList();

        logger.info("Get slots");
        List<SlotProblemFact> slots = Arrays.stream(this.getSlots())
                .map(slot -> new SlotProblemFact(slot)).sorted().toList();

        logger.info("Initially allocate slots according to scheduled time.");
        for(int i = 0; i < flights.size(); i++) {
            flights.get(i).setSlot(slots.get(i));
        }

        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);

        logger.info("Running OptaPlanner optimization ...");

        FlightPrioritization solvedFlightPrioritization = null;

        try {
            // TODO implement as "solve and listen"
            solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        } catch (Exception e) {
            logger.error(e);
        }


        Map<Flight,Slot> resultMap = null;

        if(solvedFlightPrioritization != null) {
            logger.info("Finished optimization with OptaPlanner. Score of solution is: " + solvedFlightPrioritization.getScore());
            logger.info("VERIFY SCORE: " + new FlightPrioritizationEasyScoreCalculator().calculateScore(solvedFlightPrioritization));
            resultMap = solvedFlightPrioritization.getResultMap();

            logger.info("Setting statistics for this optimization.");
            if(this.statistics != null){
                this.statistics = new OptaplannerOptimizationStatistics();
            }

            // Todo: Check why i had to add these lines so that asynchronous optimization works

            //Get potentially existing results
            var existingResults = this.getResults();
            var resultList = new ArrayList<Map<Flight, Slot>>();

            // Add results to new list
            if(existingResults != null){
                resultList.addAll(Arrays.asList(existingResults));
            }
            // Get score of current result
            var score = solvedFlightPrioritization.getScore().getSoftScore();

            // Update maximum fitness of this and add prepend result if score > this.maximumFitness
            if(score > this.getMaximumFitness()){
                resultList.add(0, resultMap);
                this.setMaximumFitness(score);
            }else{
                resultList.add(resultMap);
            }
            // Set updated list of results
            this.setResults(resultList);

            // Update result fitness of statistics
            this.getStatistics().setResultFitness(this.getMaximumFitness());

            // End of changes

            this.getStatistics().setFitnessFunctionInvocations(solvedFlightPrioritization.getFitnessFunctionInvocations());

            logger.info("Number of fitness function invocations: " + this.getStatistics().getFitnessFunctionInvocations());
        }

        return resultMap;
    }

    @Override
    public OptaplannerOptimizationConfiguration getDefaultConfiguration() {
        OptaplannerOptimizationConfiguration defaultConfiguration = new OptaplannerOptimizationConfiguration();

        defaultConfiguration.setConfigurationName("HILL_CLIMBING");

        return defaultConfiguration;
    }

    @Override
    public OptaplannerOptimizationConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        OptaplannerOptimizationConfiguration newConfiguration = new OptaplannerOptimizationConfiguration();

        Object configurationName = parameters.get("configurationName");
        Object secondsSpentLimit = parameters.get("secondsSpentLimit");

        // set the parameters
        try {
            if (configurationName != null) {
                newConfiguration.setConfigurationName((String) configurationName);
            } else {
                newConfiguration.setConfigurationName(this.getDefaultConfiguration().getConfigurationName());
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("configurationName", String.class);
        }

        try {
            if (secondsSpentLimit != null) {
                newConfiguration.setSecondsSpentLimit(((Integer) secondsSpentLimit).longValue());
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("secondsSpentLimit", Long.class);
        }

        // replace the configuration if no error was thrown
        this.configuration = newConfiguration;
    }

    @Override
    public OptaplannerOptimizationStatistics getStatistics() {

        return this.statistics;
    }
}
