package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

import java.util.List;
import java.util.Map;

public class PrivacyPreservingHillClimbingForager<Solution_> extends AbstractPrivacyPreservingForager<Solution_>{

    public PrivacyPreservingHillClimbingForager(int acceptedCountLimit_) {
        super(acceptedCountLimit_);
    }

    /**
     * Implements a selection mechanism representing the Hill-Climbing idea by picking a move if the
     * highest score of the candidates is higher than the score of the current solution.
     * @param peMap the map
     * @return the winning move of the step
     */
    @Override
    protected LocalSearchMoveScope<Solution_> pickMoveUsingPrivacyEngineMap(Map<HardSoftScore, List<LocalSearchMoveScope<Solution_>>> peMap) {
        var optEntry = peMap.entrySet().stream().findFirst();
        var optWinner = optEntry.get().getValue().stream().findFirst();

        if(optWinner.isEmpty()) return null;

        var score = optEntry.get().getKey();
        var winner = optEntry.get().getValue().stream().findFirst().get();


        if(score.compareTo(highScore) >= 0){
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
}
