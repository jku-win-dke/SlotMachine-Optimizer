package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import org.optaplanner.core.impl.localsearch.decider.forager.AbstractLocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.finalist.FinalistPodium;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;

import java.util.List;

/**
 * A privacy preserving forager that uses the privacy-engine to evaluate the candidates of the search steps.
 * This will probably be the class that implements search algorithms
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

    // TODO: maybe also safe other good solutions
    /**
     * Holds the currently winning solution of the search phase for comparison with new candidates
     */
    protected LocalSearchMoveScope<Solution_> currentlyWinningMoveScope;

    public PrivacyPreservingForager(FinalistPodium<Solution_> finalistPodium,
                                    int acceptedCountLimit, boolean breakTieRandomly){
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
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        finalistPodium.solvingStarted(solverScope);
    }

    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        finalistPodium.phaseStarted(phaseScope);
    }

    /**
     * Resets the move counts and notifies the finalistPodium
     * @param stepScope the step scope
     */
    @Override
    public void stepStarted(LocalSearchStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        finalistPodium.stepStarted(stepScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;
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
        }
        finalistPodium.addMove(moveScope);
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

        List<LocalSearchMoveScope<Solution_>> finalistList = finalistPodium.getFinalistList();
        if (finalistList.isEmpty()) {
            return null;
        }
        if (finalistList.size() == 1 || !breakTieRandomly) {
            return finalistList.get(0);
        }
        int randomIndex = stepScope.getWorkingRandom().nextInt(finalistList.size());
        var finalist = finalistList.get(randomIndex);
        if(currentlyWinningMoveScope == null
                || finalist.getScore().compareTo(currentlyWinningMoveScope.getScore()) > 0){
            logger.info("Found new winner with score: " + finalist.getScore().toString());
            this.currentlyWinningMoveScope = finalist;
            return finalist;
        }
        this.acceptedCountLimit = this.acceptedCountLimit * 2;
        this.increasedCountLimit ++;
        return currentlyWinningMoveScope;
    }

    @Override
    public void stepEnded(LocalSearchStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        finalistPodium.stepEnded(stepScope);
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        finalistPodium.phaseEnded(phaseScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        logger.info("Increased count limit " + increasedCountLimit + " times.");
        logger.info("Considered " + addedMoves + " moves.");
        logger.info("Picked " + pickedMoves + " moves.");
        super.solvingEnded(solverScope);
        finalistPodium.solvingEnded(solverScope);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + acceptedCountLimit + ")";
    }

}
