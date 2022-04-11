package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.score.ScoreUtils;

import java.util.List;

public class PrivacyPreservingSimulatedAnnealingForager<Solution_> extends AbstractPrivacyPreservingForager<Solution_>{

    protected Score startingTemperature;

    protected int levelsLength = -1;
    protected double[] startingTemperatureLevels;
    protected double[] temperatureLevels;
    protected double temperatureMinimum = 1.0E-100; // Double.MIN_NORMAL is E-308


    public PrivacyPreservingSimulatedAnnealingForager(int acceptedCountLimit_, List<Solution_> intermediateResults, OptaplannerOptimizationStatistics statistics, Score startingTemperature) {
        super(acceptedCountLimit_, intermediateResults, statistics);
        this.startingTemperature = startingTemperature;
    }

    @Override
    protected boolean isAccepted(LocalSearchMoveScope<Solution_> stepWinner) {
        HardSoftScore moveScore = (HardSoftScore) stepWinner.getScore();
        if(this.iterations == 1){
            this.startingTemperature = ScoreUtils.parseScore(HardSoftScore.class, "0hard/"+moveScore.getSoftScore()/100+"soft");
            logger.info("Set starting temperature to "+moveScore.getSoftScore()/100 + " for soft score of "+moveScore.getSoftScore());
        }

        if (moveScore.compareTo(highScore) >= 0) {
            logger.info("Found new winner with score: " + moveScore);
            return true;
        }

        Score moveScoreDifference = highScore.subtract(moveScore);
        double[] moveScoreDifferenceLevels = ScoreUtils.extractLevelDoubles(moveScoreDifference);
        double acceptChance = 1.0;
        for (int i = 0; i < levelsLength; i++) {
            double moveScoreDifferenceLevel = moveScoreDifferenceLevels[i];
            double temperatureLevel = temperatureLevels[i];
            double acceptChanceLevel;
            if (moveScoreDifferenceLevel <= 0.0) {
                // In this level, moveScore is better than the lastStepScore, so do not disrupt the acceptChance
                acceptChanceLevel = 1.0;
            } else {
                acceptChanceLevel = Math.exp(-moveScoreDifferenceLevel / temperatureLevel);
            }
            acceptChance *= acceptChanceLevel;
        }
        if (stepWinner.getWorkingRandom().nextDouble() < acceptChance) {
            logger.info("Accepted move with score: " + moveScore);
            return true;
        } else {
            return false;
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        for (double startingTemperatureLevel : ScoreUtils.extractLevelDoubles(startingTemperature)) {
            if (startingTemperatureLevel < 0.0) {
                throw new IllegalArgumentException("The startingTemperature (" + startingTemperature
                        + ") cannot have negative level (" + startingTemperatureLevel + ").");
            }
        }
        startingTemperatureLevels = ScoreUtils.extractLevelDoubles(startingTemperature);
        temperatureLevels = startingTemperatureLevels;
        levelsLength = startingTemperatureLevels.length;
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        startingTemperatureLevels = null;
        temperatureLevels = null;
        levelsLength = -1;
    }

    @Override
    public void stepStarted(LocalSearchStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        // TimeGradient only refreshes at the beginning of a step, so this code is in stepStarted instead of stepEnded
        double timeGradient = stepScope.getTimeGradient();
        double reverseTimeGradient = 1.0 - timeGradient;
        temperatureLevels = new double[levelsLength];
        for (int i = 0; i < levelsLength; i++) {
            temperatureLevels[i] = startingTemperatureLevels[i] * reverseTimeGradient;
            if (temperatureLevels[i] < temperatureMinimum) {
                temperatureLevels[i] = temperatureMinimum;
            }
            logger.info("New Temperature Level["+i+"] is "+ temperatureLevels[i]);
        }
    }

}
