package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.PropertiesLoader;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

import java.util.List;

public class PrivacyPreservingStepCountingHillClimbingForager<Solution_> extends AbstractPrivacyPreservingForager<Solution_>{
    protected int stepCountingHillClimbingSize = 5;

    protected HardSoftScore thresholdScore;
    protected int count = -1;

    public PrivacyPreservingStepCountingHillClimbingForager(int acceptedCountLimit_, List<Solution_> intermediateResults, OptaplannerOptimizationStatistics statistics) {
        super(acceptedCountLimit_, intermediateResults, statistics);
        this.stepCountingHillClimbingSize = Integer.parseInt(configuration.getProperty(PropertiesLoader.STEP_COUNTING_SIZE));
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
        count += 1;
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

}
