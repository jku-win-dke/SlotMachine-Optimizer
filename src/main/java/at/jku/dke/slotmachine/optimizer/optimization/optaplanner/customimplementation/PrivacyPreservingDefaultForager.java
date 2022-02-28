package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.decider.forager.AbstractLocalSearchForager;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

import java.util.*;

/**
 * A privacy preserving forager that uses the privacy-engine to evaluate the candidates of the search steps.
 * This will probably be the class that implements the custom search algorithms
 * @param <Solution_>
 */
public class PrivacyPreservingDefaultForager<Solution_> extends AbstractLocalSearchForager<Solution_> {
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

    // Score director to execute moves
    private InnerScoreDirector director;

    // Highest score encountered
    HardSoftScore highScore;

    // Current winner of the search phase
    private LocalSearchMoveScope<Solution_> currentWinner;

    // Collections storing candidates
    List<LocalSearchMoveScope<Solution_>> candidateList;
    Map<FlightPrioritization, Integer > flightPrioritizationToMoveIndexMap;

    // TODO: maybe also safe other good solutions
    /**
     * Holds the currently winning solution of the search phase for comparison with new candidates
     */
    public PrivacyPreservingDefaultForager(int acceptedCountLimit) {
        logger.info("Initialized " + this.getClass());
        this.acceptedCountLimit = acceptedCountLimit;

        if (acceptedCountLimit < 50) {
            throw new IllegalArgumentException("The acceptedCountLimit (" + acceptedCountLimit
                    + ") cannot below 50 for privacy-preserving optimization session.");
        }


        this.privacyEngine = new SimulatedPrivacyEngine();
        this.highScore = HardSoftScore.of(0, 0);
        this.increasedCountLimit = 0;
        this.candidateList = new ArrayList<>();
        this.flightPrioritizationToMoveIndexMap = new HashMap<>();
        this.currentWinner = null;
    }


    // ************************************************************************
    // Worker methods
    // ************************************************************************
    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        this.director = phaseScope.getScoreDirector();
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
        flightPrioritizationToMoveIndexMap.clear();
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
     * This method can be overridden by foragers to implement a different selection scheme/search algorithm.
     * @param peMap the map
     * @return the winning move scope or null
     */
    public LocalSearchMoveScope<Solution_> pickMoveUsingPrivacyEngineMap(Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> peMap) {
        var optEntry = peMap.entrySet().stream().findFirst();
        var optWinner = optEntry.get().getValue().stream().findFirst();

        if(optWinner.isEmpty()) return null;

        var score = optEntry.get().getKey();
        var winner = optEntry.get().getValue().stream().findFirst().get();


        if(score.compareTo(highScore) > 0){
            logger.info("Found new winner with score: " + score);
            winner.setScore(score);
            this.highScore = score;
            this.currentWinner = winner;
            this.currentWinner.setScore(score);
            return winner;
        }
        increasedCountLimit++;
        return null;
    }




    /**
     * Gets the mapping from the max score of the candidates to a sorted list from the privacy engine
     * @return the map
     */
    private Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> getSortedListFromPrivacyEngine() {
        // Map FlightPriorizations to LocalSearchMoveScope for conversion with privacy engine
        /*
        for(var candidate : candidateList){

            Move<Solution_> move = candidate.getMove();
            if(move.getPlanningEntities().size() < 2) continue;
            // TODO: account for changemoves with size = 1
            var newFlightPrioritization = new FlightPrioritization(new ArrayList<>(workingSolution.getSlots()), new ArrayList<>(workingSolution.getFlights()));
            var planningEntities = (Collection< FlightPlanningEntity>) move.getPlanningEntities();

            FlightPlanningEntity firstPlanningEntity = null;
            FlightPlanningEntity secondPlanningEntity = null;
            int i = 0;
            for (var e : planningEntities){
                if(i == 0) firstPlanningEntity = e;
                else secondPlanningEntity = e;
                i++;
            }

            FlightPlanningEntity finalFirstPlanningEntity = firstPlanningEntity;
            FlightPlanningEntity finalSecondPlanningEntity = secondPlanningEntity;

            if(finalSecondPlanningEntity != null && finalFirstPlanningEntity != null){
                var firstFlight = newFlightPrioritization.getFlights().stream()
                        .filter(f -> f.getWrappedFlight().getFlightId() == finalFirstPlanningEntity.getWrappedFlight().getFlightId()).findFirst().get();

                var secondFlight = newFlightPrioritization.getFlights().stream()
                        .filter(f -> f.getWrappedFlight().getFlightId() == finalSecondPlanningEntity.getWrappedFlight().getFlightId()).findFirst().get();

                var firstSlot = firstFlight.getSlot();
                var secondSlot = secondFlight.getSlot();

                newFlightPrioritization.getFlights().remove(firstFlight);
                newFlightPrioritization.getFlights().remove(secondFlight);

                FlightPlanningEntity firstNewFlightPlanningEntity = new FlightPlanningEntity();
                firstNewFlightPlanningEntity.setSlot(secondSlot);
                firstNewFlightPlanningEntity.setWrappedFlight(firstFlight.getWrappedFlight());

                FlightPlanningEntity secondNewEntity = new FlightPlanningEntity();
                secondNewEntity.setSlot(firstSlot);
                secondNewEntity.setWrappedFlight(secondFlight.getWrappedFlight());

                newFlightPrioritization.getFlights().add(firstNewFlightPlanningEntity);
                newFlightPrioritization.getFlights().add(secondNewEntity);
            }
            var newFlightPrioritization = getFlightPrioritizationFromMoveScope(candidate);
                flightPrioritizationToMoveIndexMap.put(newFlightPrioritization, candidate.getMoveIndex());

        }
        */
        initializeCollectionsFromMoveScope();

        // Initialize Result
        HashMap<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> result = new HashMap<>();
        List<LocalSearchMoveScope<Solution_>> sortedMoveScopeList = new ArrayList<>();
        HardSoftScore maxScore = null;


        // Get sorted list and max score from privacy engine
        List<FlightPrioritization> sortedFlightPrioritizationList = new ArrayList<>();
        Map<HardSoftScore, List<FlightPrioritization>> map = privacyEngine.evaluateCandidates(flightPrioritizationToMoveIndexMap.keySet().stream().toList());
        var optEntry = map.entrySet().stream().findFirst();

        // Check if entry is present and set max-score and assign list
        if(optEntry.isPresent()){
            sortedFlightPrioritizationList = optEntry.get().getValue();
            maxScore = optEntry.get().getKey();
        }

        // Iterate over sorted flight prioritizations and add corresponding MoveScope to result
        for(FlightPrioritization flightPrio : sortedFlightPrioritizationList){
            int index = flightPrioritizationToMoveIndexMap.get(flightPrio);
            var moveScopeOpt = candidateList.stream().filter(ms -> ms.getMoveIndex() == index).findFirst();
            moveScopeOpt.ifPresent(sortedMoveScopeList::add);
        }

        result.put(maxScore, sortedMoveScopeList);

        if(!sortedMoveScopeList.isEmpty())
            return result;
        return null;
    }

    /**
     * Initializes the map that maps a flight prioritization constructed from a moves stored in the candidate list
     * to the corresponding index of the move.
     */
    private void initializeCollectionsFromMoveScope() {
        for(var moveScope : candidateList){
            var undoMove = moveScope.getMove().doMove(director);
            if(director.cloneWorkingSolution() instanceof FlightPrioritization changedFlightPrio){
                flightPrioritizationToMoveIndexMap.put(changedFlightPrio, moveScope.getMoveIndex());
            }
            undoMove.doMove(director);
        }
    }

}
