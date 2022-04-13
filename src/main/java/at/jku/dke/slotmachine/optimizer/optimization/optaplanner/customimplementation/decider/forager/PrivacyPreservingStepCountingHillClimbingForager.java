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

    public PrivacyPreservingStepCountingHillClimbingForager(int acceptedCountLimit_,  OptaplannerOptimizationStatistics statistics, int stepCountingHillClimbingSize, StepCountingHillClimbingType stepCountingHillClimbingType_) {
        super(acceptedCountLimit_, statistics);
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
        // Pick move if score gets improved or thresholdScore does not get violated
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

        // Initialize thresholdscore
        thresholdScore = phaseScope.getBestScore();
        count = 0;
    }

    @Override
    public void stepEnded(LocalSearchStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);

        // Update thresholdscore and reset count if necessary
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

    /**
     * Determines the count increment for every step according to the type
     * @param stepScope the step scope
     * @return the increment
     */
    private int determineCountIncrement(LocalSearchStepScope<Solution_> stepScope) {
        switch (stepCountingHillClimbingType) {
            case SELECTED_MOVE: // Increase count by number of selected moves
                long selectedMoveCount = stepScope.getSelectedMoveCount();
                return selectedMoveCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) selectedMoveCount;
            case ACCEPTED_MOVE: // Increase count by number of accepted moves
                long acceptedMoveCount = stepScope.getAcceptedMoveCount();
                return acceptedMoveCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) acceptedMoveCount;
            case STEP: // Increse count by 1
                return 1;
            case EQUAL_OR_IMPROVING_STEP: // Increase count if score got improved or equal
                return ((Score) stepScope.getScore()).compareTo(
                        stepScope.getPhaseScope().getLastCompletedStepScope().getScore()) >= 0 ? 1 : 0;
            case IMPROVING_STEP: // Increase count by 1 if score got improved
                return ((Score) stepScope.getScore()).compareTo(
                        stepScope.getPhaseScope().getLastCompletedStepScope().getScore()) > 0 ? 1 : 0;
            default:
                throw new IllegalStateException("The stepCountingHillClimbingType (" + stepCountingHillClimbingType
                        + ") is not implemented.");
        }
    }
}
