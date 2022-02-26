package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.SlotProblemFact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.config.solver.monitoring.SolverMetric;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.localsearch.decider.forager.AbstractLocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.finalist.FinalistPodium;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * A privacy preserving forager that uses the privacy-engine to evaluate the candidates of the search steps.
 * This will probably be the class that implements the custom search algorithms
 * @param <Solution_>
 */
public class PrivacyPreservingForager<Solution_> extends AbstractLocalSearchForager<Solution_> implements LocalSearchForager<Solution_> {
    protected final FinalistPodium<Solution_> finalistPodium;
    protected final boolean breakTieRandomly;

    /**
     * Specifies how many moves are gathered before a winner is picked
     */
    protected int acceptedCountLimit;

    // Helper fields for debugging
    protected int increasedCountLimit;
    protected int addedMoves;
    protected int pickedMoves;


    protected long selectedMoveCount;
    protected long acceptedMoveCount;

    private final PrivacyEngine privacyEngine;
    private LocalSearchPhaseScope<Solution_> phaseScope;
    private LocalSearchStepScope<Solution_> stepScope;
    HardSoftScore currentlyHighestScore;
    // Collections storing candidates
    List<LocalSearchMoveScope<Solution_>> candidateList;
    Map<FlightPrioritization, Integer > flightPrioritizationToMoveIndexMap;

    // TODO: maybe also safe other good solutions
    /**
     * Holds the currently winning solution of the search phase for comparison with new candidates
     */
    protected LocalSearchMoveScope<Solution_> currentlyWinningMoveScope;

    public PrivacyPreservingForager(FinalistPodium<Solution_> finalistPodium,
                                    int acceptedCountLimit, boolean breakTieRandomly, SolverScope<Solution_> solverScope) {
        logger.info("Initialized " + this.getClass());
        this.finalistPodium = finalistPodium;
        this.acceptedCountLimit = acceptedCountLimit;

        this.increasedCountLimit = 0;
        this.addedMoves = 0;
        this.pickedMoves = 0;

        if (acceptedCountLimit < 1) {
            throw new IllegalArgumentException("The acceptedCountLimit (" + acceptedCountLimit
                    + ") cannot be negative or zero.");
        }
        this.breakTieRandomly = breakTieRandomly;
        currentlyWinningMoveScope = null;



        privacyEngine = new PrivacyEngine();
        this.candidateList = new ArrayList<>();
        this.flightPrioritizationToMoveIndexMap = new HashMap<>();
    }


    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
    }

    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        this.phaseScope = phaseScope;
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

        this.candidateList = new ArrayList<>();
        this.flightPrioritizationToMoveIndexMap = new HashMap<>();
        this.stepScope = new LocalSearchStepScope<>(this.phaseScope);
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
        addedMoves++;
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
        pickedMoves++;
        stepScope.setSelectedMoveCount(selectedMoveCount);
        stepScope.setAcceptedMoveCount(acceptedMoveCount);

        var peMap = getSortedListFromPrivacyEngine();
        if(peMap == null || peMap.isEmpty()){
            return null;
        }

        var optEntry = peMap.entrySet().stream().findFirst();
        var optWinner = optEntry.get().getValue().stream().findFirst();

        if(optWinner.isEmpty()) return null;

        var score = optEntry.get().getKey();
        var winner = optEntry.get().getValue().stream().findFirst().get();


        if(currentlyWinningMoveScope == null
                || score.compareTo(currentlyHighestScore) > 0){
            logger.info("Found new winner with score: " + score);
            this.currentlyWinningMoveScope = winner;
            winner.setScore(score);
            this.currentlyHighestScore = score;
            this.currentlyWinningMoveScope.setScore(score);
            return winner;
        }
        increasedCountLimit++;
        return currentlyWinningMoveScope;
    }

    // TODO: make method more efficient, maybe use the doMove method i found somewhere
    private Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> getSortedListFromPrivacyEngine() {
        // Map FlightPriorizations to LocalSearchMoveScope for conversion with privacy engine
        var workingSolution = (FlightPrioritization) this.stepScope.getWorkingSolution();
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

                flightPrioritizationToMoveIndexMap.put(newFlightPrioritization, candidate.getMoveIndex());
            }
        }

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

        // TODO: return no map but set the max score for the first move element
        result.put(maxScore, sortedMoveScopeList);

        if(!sortedMoveScopeList.isEmpty())
            return result;
        return null;
    }

    @Override
    public void stepEnded(LocalSearchStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;

        this.candidateList = new ArrayList<>();
        this.flightPrioritizationToMoveIndexMap = new HashMap<>();
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        logger.info("Increased count limit " + increasedCountLimit + " times.");
        logger.info("Considered " + addedMoves + " moves.");
        logger.info("Picked " + pickedMoves + " moves.");
        super.solvingEnded(solverScope);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + acceptedCountLimit + ")";
    }

}
