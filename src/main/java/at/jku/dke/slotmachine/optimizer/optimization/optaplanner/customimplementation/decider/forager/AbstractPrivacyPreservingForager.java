package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.PropertiesLoader;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.SimulatedPrivacyEngine;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.decider.forager.AbstractLocalSearchForager;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    // Helper fields for debugging
    protected int increasedCountLimit;

    // Privacy Engine
    private final SimulatedPrivacyEngine privacyEngine;
    private final boolean useAuthenticPrivacyEngineSimulation;

    // Score director to execute moves
    protected InnerScoreDirector director;

    // Highest score encountered
    HardSoftScore highScore;

    // Current winner of the search phase
    protected LocalSearchMoveScope<Solution_> currentWinner;

    // Collections storing candidates
    protected List<LocalSearchMoveScope<Solution_>> candidateList;
    protected Map<FlightPrioritization, LocalSearchMoveScope<Solution_> > flightPrioritizationMoveScopeMap;
    protected Map<Integer[], LocalSearchMoveScope<Solution_>> flightOrderArrayMoveScopeMap;
    protected Integer[][] flightOrderArraysOfCandidates;
    protected Integer numberOfFlights;

    // TODO: maybe also safe other good solutions (or solve with custom BestSolutionRecaller)

   protected AbstractPrivacyPreservingForager() {
       this(50);
   }


    protected AbstractPrivacyPreservingForager(int acceptedCountLimit)  {
        logger.info("Initialized " + this.getClass());
        this.acceptedCountLimit = acceptedCountLimit;

        // TODO: Define minimum acceptedCountLimit in configuration
        if (acceptedCountLimit < 50) {
            throw new IllegalArgumentException("The acceptedCountLimit (" + acceptedCountLimit
                    + ") cannot below 50 for privacy-preserving optimization session.");
        }

        try {
            this.configuration = PropertiesLoader.loadProperties("customoptaplanner.properties");
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }

        this.privacyEngine = new SimulatedPrivacyEngine();
        this.highScore = HardSoftScore.of(0, 0);
        this.increasedCountLimit = 0;
        this.candidateList = new ArrayList<>();
        this.flightPrioritizationMoveScopeMap = new HashMap<>();
        this.flightOrderArrayMoveScopeMap = new HashMap<>();
        this.currentWinner = null;
        this.useAuthenticPrivacyEngineSimulation = Boolean.parseBoolean(configuration.getProperty(PropertiesLoader.getAuthenticPrivacyEngineKey()));


    }


    // ************************************************************************
    // Worker methods
    // ************************************************************************
    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        this.director = phaseScope.getScoreDirector();
        this.numberOfFlights = ((FlightPrioritization)phaseScope.getWorkingSolution()).getFlights().size();
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
        if (moveScope.getAccepted()) {
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
        stepScope.setSelectedMoveCount(selectedMoveCount);
        stepScope.setAcceptedMoveCount(acceptedMoveCount);

        var peMap = getSortedListFromPrivacyEngine();
        if(peMap == null || peMap.isEmpty()){
            return null;
        }

        var result = pickMoveUsingPrivacyEngineMap(peMap);
        return result != null ? result : currentWinner;
    }


    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;
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
     * @param peMap the map
     * @return the winning move scope or null
     */
    protected abstract LocalSearchMoveScope<Solution_> pickMoveUsingPrivacyEngineMap(Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> peMap);


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
        var map = privacyEngine.evaluateFlightOrderArrays(this.flightOrderArraysOfCandidates, (FlightPrioritization) this.director.cloneWorkingSolution());
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
            var undoMove = moveScope.getMove().doMove(director);
            // Get the solution representing the move
            if(director.cloneWorkingSolution() instanceof FlightPrioritization flightPrioritization){
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
            undoMove.doMove(director);
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
        var currentSolution = director.getWorkingSolution();
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
