package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

public class PrivacyPreservingFlightPrioritizationScoreCalculator implements EasyScoreCalculator<FlightPrioritization, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(FlightPrioritization flightPrioritization) {
        return HardSoftScore.of(0, 0);
    }
}
