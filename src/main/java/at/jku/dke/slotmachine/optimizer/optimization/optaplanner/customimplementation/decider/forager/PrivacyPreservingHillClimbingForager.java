package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

import java.util.List;

public class PrivacyPreservingHillClimbingForager<Solution_> extends AbstractPrivacyPreservingForager<Solution_>{

    public PrivacyPreservingHillClimbingForager(int acceptedCountLimit_, OptaplannerOptimizationStatistics statistics) {
        super(acceptedCountLimit_, statistics);
    }

    /**
     * Implements a selection mechanism representing the Hill-Climbing idea by picking a move if the
     * highest score of the candidates is higher than the score of the current solution.
     * @return the winning move of the step
     */
    @Override
    protected boolean isAccepted(LocalSearchMoveScope<Solution_> winner) {
        var score = (HardSoftScore) winner.getScore();

        // Pick move only if score gets improved
        if(score.compareTo(highScore) >= 0){
            logger.info("Found new winner with score: " + score);
            return true;
        }
        return false;
    }
}
