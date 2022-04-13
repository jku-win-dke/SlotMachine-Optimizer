package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.PropertiesLoader;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.SimulatedPrivacyEngine;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.localsearch.decider.forager.AbstractLocalSearchForager;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * A privacy preserving forager that uses the privacy-engine to evaluate the candidates of the search steps.
 * This class can be extended to implement different selection mechanisms, by implementing the pickMoveUsingPrivacyEngineMap()  method
 * @param <Solution_>
 */
@Component
public abstract class AbstractPrivacyPreservingForager<Solution_> extends AbstractLocalSearchForager<Solution_> {
    protected Properties configuration;
    /**
     * Specifies how many moves are gathered before a winner is picked
     */
    protected int acceptedCountLimit;

    protected long selectedMoveCount;
    protected long acceptedMoveCount;

    // Privacy Engine
    private final SimulatedPrivacyEngine privacyEngine;
    private final boolean useAuthenticPrivacyEngineSimulation;

    // Score director to execute moves
    protected ScoreDirector<Solution_> scoreDirector;
    protected InnerScoreDirector<Solution_, ?> innerScoreDirector;

    // Highest score encountered
    HardSoftScore highScore;

    // Current winner of the search phase
    protected LocalSearchMoveScope<Solution_> lastPickedMoveScope;

    // Collections storing candidates
    protected List<LocalSearchMoveScope<Solution_>> candidateList;
    protected Map<FlightPrioritization, LocalSearchMoveScope<Solution_> > flightPrioritizationMoveScopeMap;
    protected Map<Integer[], LocalSearchMoveScope<Solution_>> flightOrderArrayMoveScopeMap;
    protected Integer[][] flightOrderArraysOfCandidates;
    protected Integer numberOfFlights;

    // Statistics
    protected final OptaplannerOptimizationStatistics statistics;
    protected int iterations;

   protected AbstractPrivacyPreservingForager() {
       this(50, new OptaplannerOptimizationStatistics());
   }


    protected AbstractPrivacyPreservingForager(int acceptedCountLimit, OptaplannerOptimizationStatistics statistics)  {
        logger.info("Initialized " + this.getClass());
        try {
            this.configuration = PropertiesLoader.loadProperties("customoptaplanner.properties");
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
        this.acceptedCountLimit = acceptedCountLimit;
        this.statistics = statistics;

        int minimumAcceptedCountLimit = Integer.parseInt(configuration.getProperty(PropertiesLoader.MINIMUM_ACCEPTED_COUNT_LIMIT));
        if (acceptedCountLimit < minimumAcceptedCountLimit) {
            throw new IllegalArgumentException("The acceptedCountLimit (" + acceptedCountLimit
                    + ") cannot be below " + minimumAcceptedCountLimit +" for privacy-preserving optimization session.");
        }


        this.privacyEngine = new SimulatedPrivacyEngine();
        this.highScore = HardSoftScore.of(0, 0);
        this.candidateList = new ArrayList<>();
        this.flightPrioritizationMoveScopeMap = new HashMap<>();
        this.flightOrderArrayMoveScopeMap = new HashMap<>();
        this.lastPickedMoveScope = null;
        this.useAuthenticPrivacyEngineSimulation = Boolean.parseBoolean(configuration.getProperty(PropertiesLoader.USE_AUTHENTIC_PRIVACY_ENGINE));
    }


    // ************************************************************************
    // Worker methods
    // ************************************************************************
    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        this.scoreDirector = phaseScope.getScoreDirector();
        if(scoreDirector != null)
            this.innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        this.numberOfFlights = ((FlightPrioritization)phaseScope.getWorkingSolution()).getFlights().size();
        this.statistics.setInitialFitness(-1);
        this.iterations = 0;
    }

    /**
     * Resets the move counts and notifies the finalistPodium
     * @param stepScope the step scope
     */
    @Override
    public void stepStarted(LocalSearchStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;

        candidateList.clear();
        flightPrioritizationMoveScopeMap.clear();
        flightOrderArrayMoveScopeMap.clear();
        iterations++;
    }

    /**
     * Required to check compliance with a never ending move selector, otherwise steps would never end
     * @return boolean indicating if acceptedCountLimit has been set
     */
    @Override
    public boolean supportsNeverEndingMoveSelector() {
        // TODO FIXME magical value Integer.MAX_VALUE coming from ForagerConfig
        return acceptedCountLimit < Integer.MAX_VALUE;
    }

    @Override
    public void addMove(LocalSearchMoveScope<Solution_> moveScope) {
        selectedMoveCount++;
        if (Boolean.TRUE.equals(moveScope.getAccepted())) {
            acceptedMoveCount++;
            candidateList.add(moveScope);
        }
    }

    /**
     * Checks if enough moves have been gathered according to the limit of accepted moves
     * @return boolean indicating if limit has been reached
     */
    @Override
    public boolean isQuitEarly() {
        return acceptedMoveCount >= acceptedCountLimit;
    }

    /**
     * Picks a move from all the candidates for the next step
     * @param stepScope the scope of the step
     * @return the winning move
     */
    @Override
    public LocalSearchMoveScope<Solution_> pickMove(LocalSearchStepScope<Solution_> stepScope) {
        // Statistics
        stepScope.setSelectedMoveCount(selectedMoveCount);
        stepScope.setAcceptedMoveCount(acceptedMoveCount);

        // Request the evaluation from the privacy engine
        var privacyEngineScoreToSortedListMap = getSortedListFromPrivacyEngine();

        if(privacyEngineScoreToSortedListMap == null || privacyEngineScoreToSortedListMap.isEmpty()) return lastPickedMoveScope;

        // Extract the winner and assign the high score to the move
        var optionalPrivacyEngineScoreToSortedListEntrySet = privacyEngineScoreToSortedListMap.entrySet().stream().findFirst();
        var optionalStepWinningMoveScope = optionalPrivacyEngineScoreToSortedListEntrySet.get().getValue().stream().findFirst();

        if(optionalStepWinningMoveScope.isEmpty()) return lastPickedMoveScope;

        LocalSearchMoveScope<Solution_> winner = optionalStepWinningMoveScope.get();
        winner.setScore(optionalPrivacyEngineScoreToSortedListEntrySet.get().getKey());

        // Return winner if accepted
        if(this.isAccepted(winner)){
            this.highScore = (HardSoftScore) winner.getScore();
            this.lastPickedMoveScope = winner;

            var undoMove = winner.getMove().doMove(scoreDirector);
            undoMove.doMove(scoreDirector);

            if(this.iterations == 1) this.statistics.setInitialFitness(this.highScore.getHardScore() >= 0 ? this.highScore.getSoftScore() : 0);

            return winner;
        }
        // Else return current winner
        logger.error("REACHED LOCAL OPTIMUM, RESTARTING THE STEP!");
        return lastPickedMoveScope;
    }


    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;

        this.statistics.setTimeFinished(LocalDateTime.now(ZoneId.of("CET")));
        this.statistics.setResultFitness(this.highScore.getHardScore() >= 0 ? this.highScore.getSoftScore() : 0);
        this.statistics.setIterations(this.iterations);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + acceptedCountLimit + ")";
    }



    // ************************************************************************
    // Custom Methods
    // ************************************************************************


    /**
     * Picks the move according to the mapping of the maximum score of the current candidates and the sorted list of the candidates.
     * This method has to be implemented by foragers to facilitate a certain search algorithm mechanism.
     * Every implementation has to check if the currentWinner is null to avoid not setting a winner if the initial solution is already the best solution.
     * @return the winning move scope or null
     */
    protected abstract boolean isAccepted(LocalSearchMoveScope<Solution_> stepWinner);


    /**
     * Gets the mapping from the max score of the candidates to a sorted list from the privacy engine
     * @return the map
     */
    private Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> getSortedListFromPrivacyEngine() {
        initializeCollectionsFromMoveScopeCandidates();
        if(this.useAuthenticPrivacyEngineSimulation){
            return evaluateExternalDataMode();
        }else{
            return evaluateInternalDataMode();
        }

    }

    private Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> evaluateInternalDataMode(){
        Map<HardSoftScore, List<FlightPrioritization>> map = privacyEngine.evaluateFlightPrioritizations(flightPrioritizationMoveScopeMap.keySet().stream().toList());
        return this.getSortedMoveScopeListFromSortedFlightPrioList(map);
    }

    private Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> getSortedMoveScopeListFromSortedFlightPrioList(Map<HardSoftScore, List<FlightPrioritization>> map){
        var optEntry = map.entrySet().stream().findFirst();

        // Initialize Result
        HashMap<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> result = new HashMap<>();
        List<LocalSearchMoveScope<Solution_>> sortedMoveScopeList = new ArrayList<>();
        HardSoftScore maxScore = null;


        // Get sorted list and max score from privacy engine
        List<FlightPrioritization> sortedFlightPrioritizationList = new ArrayList<>();

        // Check if entry is present and set max-score and assign list
        if(optEntry.isPresent()){
            sortedFlightPrioritizationList = optEntry.get().getValue();
            maxScore = optEntry.get().getKey();
        }

        // Iterate over sorted flight prioritizations and add corresponding MoveScope to result
        for(FlightPrioritization flightPrio : sortedFlightPrioritizationList){
            sortedMoveScopeList.add(this.flightPrioritizationMoveScopeMap.get(flightPrio));
        }

        result.put(maxScore, sortedMoveScopeList);

        if(!sortedMoveScopeList.isEmpty())
            return result;
        return null;
    }


    private Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> evaluateExternalDataMode(){
        var map = privacyEngine.evaluateFlightOrderArrays(this.flightOrderArraysOfCandidates, (FlightPrioritization) this.innerScoreDirector.cloneWorkingSolution());
        //var map = privacyEngine.evaluateFlightOrderArrays2(this.flightOrderArraysOfCandidates, this.director);
        var score = map.entrySet().stream().findFirst().get().getKey();
        var bestOrderArray = map.entrySet().stream().findFirst().get().getValue()[0];

        Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> result = new HashMap<>();
        List<LocalSearchMoveScope<Solution_>> sortedMoveScopeList = new ArrayList<>();
        LocalSearchMoveScope<Solution_> bestMoveScope = null;

        for(var entry : flightOrderArrayMoveScopeMap.entrySet()){
            var orderArray = entry.getKey();
            if(Arrays.equals(orderArray, bestOrderArray)){
                bestMoveScope = entry.getValue();
                break;
            }
        }
        sortedMoveScopeList.add(bestMoveScope);
        result.put(score, sortedMoveScopeList);
        return result;
    }

    /**
     * Initializes the collections for each step required for communication with the privacy-engine
     */
    private void initializeCollectionsFromMoveScopeCandidates() {
        Integer[][] solutionsFlightOrderArray = new Integer[candidateList.size()][this.numberOfFlights];
        // Iterate over the accepted move-scopes of the step
        int i = 0;
        for(var moveScope : candidateList){
            // Execute the move
            var undoMove = moveScope.getMove().doMove(scoreDirector);
            // Get the solution representing the move
            if(innerScoreDirector.cloneWorkingSolution() instanceof FlightPrioritization flightPrioritization){
                // Map the flight-prioritization to the index of the move
                if(this.useAuthenticPrivacyEngineSimulation){
                    var orderArrayCandidate = flightPrioritization.getFlightOrderArray();
                    solutionsFlightOrderArray[i] = orderArrayCandidate;
                    this.flightOrderArrayMoveScopeMap.put(orderArrayCandidate, moveScope);
                }else{
                    flightPrioritizationMoveScopeMap.put(flightPrioritization, moveScope);
                }
            }
            // Undo the move
            undoMove.doMove(scoreDirector);
            i++;
        }
        this.flightOrderArraysOfCandidates = solutionsFlightOrderArray;
    }


    private void initializeArraysFromMoveScopes(){
        Integer[][] solutionsFlightOrderArray = new Integer[candidateList.size()][this.numberOfFlights];
        Integer[] solutionFlightOrderArray;
        Integer[] currentOrder = new Integer[this.numberOfFlights];
        Map<FlightPlanningEntity, Integer> flightPlanningEntitySlotPositionMap = new HashMap<>();

        // Get current solution
        var currentSolution = scoreDirector.getWorkingSolution();
        if(currentSolution instanceof FlightPrioritization currentFlightPrioritization){
            // Construct the flight ordering of the current solution

            // Iterate over sorted slots
            int i = 0;
            for (var slot : currentFlightPrioritization.getSlots()){
                // Iterate over the flightPlanningEntities of current solution
                for(var flightPlanningEntity : currentFlightPrioritization.getFlights()){
                    if(flightPlanningEntity.getSlot().equals(slot)){ // current slot is assigned to the the planning entity
                        // Set the index of the planning entity in the ordering
                        currentOrder[i] = currentFlightPrioritization.getFlights().indexOf(flightPlanningEntity);

                        // Map the planning entity to the slot
                        flightPlanningEntitySlotPositionMap.put(flightPlanningEntity, i);
                        i++;
                        break;
                    }
                }

            }
            // Construct the flight orderings of the candidates using the ordering of the current solution as basis

            // Iterate over candidate moves
            int j = 0;
            for(var candidate : candidateList){
                // Copy the current ordering
                solutionFlightOrderArray = Arrays.copyOf(currentOrder, currentOrder.length);

                // Get the planning entities of the candidate move that have to be swapped
                var planningEntitiesToSwap = candidate.getMove().getPlanningEntities().toArray();

                if(planningEntitiesToSwap.length < 2) continue;

                // Get the current slots of the planning entities
                int slotPositionFlightToSwap = flightPlanningEntitySlotPositionMap.get(planningEntitiesToSwap[0]);
                int slotPositionOtherFlightToSwap = flightPlanningEntitySlotPositionMap.get(planningEntitiesToSwap[1]);

                // Swap the slot of the first entity with the slot of the second entity
                solutionFlightOrderArray[slotPositionFlightToSwap] = slotPositionOtherFlightToSwap;

                // Swap the slot of the second entity with the slot of the first entity
                solutionFlightOrderArray[slotPositionOtherFlightToSwap] = slotPositionFlightToSwap;

                // Add the altered ordering representing this candidate move to the multidimensional array of all candidate orderings
                solutionsFlightOrderArray[j] = solutionFlightOrderArray;

                // Map the ordering to the move scope
                flightOrderArrayMoveScopeMap.put(solutionFlightOrderArray, candidate);
                j++;
            }

            // Set the global array of orderings to the one constructed in this method
            flightOrderArraysOfCandidates = solutionsFlightOrderArray;
        }
    }

}
