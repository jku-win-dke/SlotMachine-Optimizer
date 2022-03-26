package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.PropertiesLoader;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

import java.util.List;
import java.util.Map;

public class PrivacyPreservingStepCountingHillClimbingForager<Solution_> extends AbstractPrivacyPreservingForager<Solution_>{
    protected int stepCountingHillClimbingSize = 5;

    protected HardSoftScore thresholdScore;
    protected int count = -1;

    public PrivacyPreservingStepCountingHillClimbingForager(int acceptedCountLimit_) {
        super(acceptedCountLimit_);
        this.stepCountingHillClimbingSize = Integer.parseInt(configuration.getProperty(PropertiesLoader.getStepCountingSizeKey()));
    }

    @Override
    protected LocalSearchMoveScope<Solution_> pickMoveUsingPrivacyEngineMap(Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> peMap) {
        var optEntry = peMap.entrySet().stream().findFirst();
        var optWinner = optEntry.get().getValue().stream().findFirst();

        if(optWinner.isEmpty()) return null;

        var score = optEntry.get().getKey();
        var winner = optEntry.get().getValue().stream().findFirst().get();


        if(score.compareTo(highScore) >= 0 || score.compareTo(thresholdScore) >= 0){
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
