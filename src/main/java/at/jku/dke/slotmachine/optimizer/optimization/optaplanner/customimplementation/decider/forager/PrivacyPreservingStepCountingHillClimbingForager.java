package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.PropertiesLoader;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.config.localsearch.decider.acceptor.stepcountinghillclimbing.StepCountingHillClimbingType;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

import java.util.List;

public class PrivacyPreservingStepCountingHillClimbingForager<Solution_> extends AbstractPrivacyPreservingForager<Solution_>{
    protected int stepCountingHillClimbingSize = 5;

    protected HardSoftScore thresholdScore;
    protected int count = -1;

    protected StepCountingHillClimbingType stepCountingHillClimbingType;

    public PrivacyPreservingStepCountingHillClimbingForager(int acceptedCountLimit_, List<Solution_> intermediateResults, OptaplannerOptimizationStatistics statistics, int stepCountingHillClimbingSize, StepCountingHillClimbingType stepCountingHillClimbingType_) {
        super(acceptedCountLimit_, intermediateResults, statistics);
        if(stepCountingHillClimbingSize == -1){
            try{
                this.stepCountingHillClimbingSize = Integer.parseInt(configuration.getProperty(PropertiesLoader.STEP_COUNTING_SIZE));
            }catch (NumberFormatException e){
                logger.warn("Could not parse StepCountingHillClimbing-Size");
            }
        }else{
            this.stepCountingHillClimbingSize = stepCountingHillClimbingSize;
        }
        this.stepCountingHillClimbingType = stepCountingHillClimbingType_;
    }

    @Override
    protected boolean isAccepted(LocalSearchMoveScope<Solution_> winner) {
        var score = (HardSoftScore) winner.getScore();
        if(score.compareTo(highScore) >= 0 || score.compareTo(thresholdScore) >= 0){
            logger.info("Found new winner with score: " + score);
            return true;
        }
        return false;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        thresholdScore = phaseScope.getBestScore();
        count = 0;
    }

    @Override
    public void stepEnded(LocalSearchStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        count += determineCountIncrement(stepScope);
        if (count >= stepCountingHillClimbingSize) {
            thresholdScore = highScore;
            count = 0;
        }
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        thresholdScore = null;
        count = -1;
    }

    private int determineCountIncrement(LocalSearchStepScope<Solution_> stepScope) {
        switch (stepCountingHillClimbingType) {
            case SELECTED_MOVE:
                long selectedMoveCount = stepScope.getSelectedMoveCount();
                return selectedMoveCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) selectedMoveCount;
            case ACCEPTED_MOVE:
                long acceptedMoveCount = stepScope.getAcceptedMoveCount();
                return acceptedMoveCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) acceptedMoveCount;
            case STEP:
                return 1;
            case EQUAL_OR_IMPROVING_STEP:
                return ((Score) stepScope.getScore()).compareTo(
                        stepScope.getPhaseScope().getLastCompletedStepScope().getScore()) >= 0 ? 1 : 0;
            case IMPROVING_STEP:
                return ((Score) stepScope.getScore()).compareTo(
                        stepScope.getPhaseScope().getLastCompletedStepScope().getScore()) > 0 ? 1 : 0;
            default:
                throw new IllegalStateException("The stepCountingHillClimbingType (" + stepCountingHillClimbingType
                        + ") is not implemented.");
        }
    }
}
