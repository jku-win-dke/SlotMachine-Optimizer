package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.localsearch.AssignmentProblemType;
import org.optaplanner.core.impl.localsearch.decider.forager.privacypreserving.LocalSearchStatistics;
import org.optaplanner.core.impl.solver.DefaultSolverFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class OptaplannerOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private OptaplannerOptimizationConfiguration configuration = null;
    private OptaplannerOptimizationStatistics statistics = null;
    private List<Map<Score, FlightPrioritization>> intermediateResults = new ArrayList<>();

    public OptaplannerOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
        // TODO: update statistics with relevant times etc. during optimization
        this.statistics = new OptaplannerOptimizationStatistics();
        var timeCreated = LocalDateTime.now();
        this.statistics.setTimeCreated(timeCreated);
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

        if(this.getConfiguration() != null && this.getConfiguration().getSecondsSpentLimit() != null && this.getConfiguration().getSecondsSpentLimit() > 0 && this.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING) {
            logger.info("Setting seconds spent limit to " + this.getConfiguration().getSecondsSpentLimit());
            solverConfig.getTerminationConfig().setSecondsSpentLimit(this.getConfiguration().getSecondsSpentLimit());
        }

        if(this.getConfiguration() != null && this.getMode() == OptimizationMode.BENCHMARKING && this.getTheoreticalMaximumFitness() != Double.MIN_VALUE){
            logger.info("Setting soft score limit to " + this.getTheoreticalMaximumFitness());
            solverConfig.getTerminationConfig().setBestScoreLimit("0hard/"+Math.round((float)this.getTheoreticalMaximumFitness())+"soft");
        }

        logger.info("Create the solver factory.");
        SolverFactory<FlightPrioritization> solverFactory = null;
        solverFactory = SolverFactory.create(solverConfig);

        logger.info("Build the solver.");
        Solver<FlightPrioritization> solver = null;

        AssignmentProblemType assignmentProblemType = AssignmentProblemType.BALANCED;
        if(this.getFlights().length < this.getSlots().length) assignmentProblemType = AssignmentProblemType.UNBALANCED;

        LocalSearchStatistics localSearchStatistics = null;
        if(solverFactory instanceof DefaultSolverFactory<FlightPrioritization> defaultSolverFactory){
            defaultSolverFactory.setAssignmentProblemType(assignmentProblemType);
            defaultSolverFactory.setTerminationFitness(this.getTheoreticalMaximumFitness() != Double.MIN_VALUE ? this.getTheoreticalMaximumFitness() : Double.MAX_VALUE);
            localSearchStatistics = defaultSolverFactory.getLocalSearchStatistics();

        }
        statistics.setLocalSearchStatistics(localSearchStatistics);

        solver = solverFactory.buildSolver();
        solver.addEventListener(event -> { HashMap<Score, FlightPrioritization> solution = new HashMap<>();
            solution.put(event.getNewBestScore(), event.getNewBestSolution());
            intermediateResults.add(0, solution);
        });

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

        var timeStarted = LocalDateTime.now();
        try {
            solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        } catch (Exception e) {
            logger.error(e);
        }
        var timeFinished = LocalDateTime.now();

        Map<Flight,Slot> resultMap = null;

        if(solvedFlightPrioritization != null) {
            logger.info("Finished optimization with OptaPlanner. Score of solution is: " + solvedFlightPrioritization.getScore());
            HardSoftScore verificationScore = new FlightPrioritizationEasyScoreCalculator().calculateScore(solvedFlightPrioritization);
            this.setMaximumFitness(verificationScore.getSoftScore());

            logger.info("VERIFY SCORE: " + verificationScore);
            resultMap = solvedFlightPrioritization.getResultMap();

            logger.info("Setting statistics for this optimization.");
            if(this.statistics != null){
                this.statistics = new OptaplannerOptimizationStatistics();
            }
            this.statistics.setTimeStarted(timeStarted);
            this.statistics.setTimeFinished(timeFinished);

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
            if(verificationScore.getSoftScore() > this.getMaximumFitness()){
                resultList.add(0, resultMap);
                this.setMaximumFitness(verificationScore.getSoftScore());
            }else{
                resultList.add(resultMap);
            }
            // Set updated list of results
            this.setResults(resultList);

            // Update result fitness of statistics
            this.getStatistics().setResultFitness(this.getMaximumFitness());
            if(localSearchStatistics != null){
                getStatistics().setIterations(localSearchStatistics.getIterations());
                getStatistics().setFitnessEvolution(new ArrayList<>());
                for(var step : localSearchStatistics.getSteps()){
                    FitnessEvolutionStep evolutionStep = new FitnessEvolutionStep();
                    getStatistics().getFitnessEvolution().add(evolutionStep);
                    evolutionStep.setGeneration(step.getStepIndex());
                    evolutionStep.setEvaluatedPopulation(new Double[]{(double) (((HardSoftScore) step.getStepScore()).getSoftScore())});
                    if(step.getThresholdScore() != null){
                        evolutionStep.setEstimatedPopulation(new Double[]{(double) (((HardSoftScore) step.getThresholdScore()).getSoftScore())});
                    }else{
                        evolutionStep.setEstimatedPopulation(new Double[]{(double) (((HardSoftScore) step.getStepScore()).getSoftScore())});
                    }
                }
            }

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

    @Override
    public int computeInitialFitness() {
        // TODO return initial fitness
        return Integer.MIN_VALUE;
    }
}
