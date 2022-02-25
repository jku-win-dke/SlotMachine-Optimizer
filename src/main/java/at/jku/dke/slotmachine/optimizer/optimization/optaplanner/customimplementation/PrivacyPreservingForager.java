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

public class PrivacyPreservingForager<Solution_> extends AbstractLocalSearchForager<Solution_> implements LocalSearchForager<Solution_> {
    protected final FinalistPodium<Solution_> finalistPodium;
    protected final LocalSearchPickEarlyType pickEarlyType;
    protected final boolean breakTieRandomly;

    protected int acceptedCountLimit;

    protected int increasedCountLimit;
    protected int addedMoves;
    protected int pickedMoves;

    protected long selectedMoveCount;
    protected long acceptedMoveCount;
    protected LocalSearchMoveScope<Solution_> earlyPickedMoveScope;
    protected LocalSearchMoveScope<Solution_> currentlyWinningMoveScope;

    public PrivacyPreservingForager(FinalistPodium<Solution_> finalistPodium,
                                    LocalSearchPickEarlyType pickEarlyType, int acceptedCountLimit, boolean breakTieRandomly){
        logger.info("Initialized " + this.getClass());
        this.finalistPodium = finalistPodium;
        this.pickEarlyType = LocalSearchPickEarlyType.NEVER;
        this.acceptedCountLimit = 50;

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

    @Override
    public void stepStarted(LocalSearchStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        finalistPodium.stepStarted(stepScope);
        selectedMoveCount = 0L;
        acceptedMoveCount = 0L;
        earlyPickedMoveScope = null;
    }

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
            checkPickEarly(moveScope);
        }
        finalistPodium.addMove(moveScope);
    }

    protected void checkPickEarly(LocalSearchMoveScope<Solution_> moveScope) {
        switch (pickEarlyType) {
            case NEVER:
                break;
            case FIRST_BEST_SCORE_IMPROVING:
                Score bestScore = moveScope.getStepScope().getPhaseScope().getBestScore();
                if (((Score) moveScope.getScore()).compareTo(bestScore) > 0) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            case FIRST_LAST_STEP_SCORE_IMPROVING:
                Score lastStepScore = moveScope.getStepScope().getPhaseScope()
                        .getLastCompletedStepScope().getScore();
                if (((Score) moveScope.getScore()).compareTo(lastStepScore) > 0) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            default:
                throw new IllegalStateException("The pickEarlyType (" + pickEarlyType + ") is not implemented.");
        }
    }

    @Override
    public boolean isQuitEarly() {
        return earlyPickedMoveScope != null || acceptedMoveCount >= acceptedCountLimit;
    }

    @Override
    public LocalSearchMoveScope<Solution_> pickMove(LocalSearchStepScope<Solution_> stepScope) {
        pickedMoves++;
        stepScope.setSelectedMoveCount(selectedMoveCount);
        stepScope.setAcceptedMoveCount(acceptedMoveCount);
        if (earlyPickedMoveScope != null) {
            return earlyPickedMoveScope;
        }
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
        earlyPickedMoveScope = null;
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
        return getClass().getSimpleName() + "(" + pickEarlyType + ", " + acceptedCountLimit + ")";
    }

}
