package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

public class FlightPrioritizationEasyScoreCalculator implements EasyScoreCalculator<FlightPrioritization, HardSoftScore> {
    @Override
    public HardSoftScore calculateScore(FlightPrioritization flightPrioritization) {
        int hardScore = 0;
        int softScore = 0;

        for(FlightPlanningEntity f : flightPrioritization.getFlights()) {
        	// make sure, that the scheduled time is before or at the assigned time
            if(f.getWrappedFlight().getScheduledTime().isAfter(f.getSlot().getTime())) {
                hardScore--;
            }

            for(FlightPlanningEntity e : flightPrioritization.getFlights()) {
                if(!f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore--;
                }
            }

			// add weight of flight to soft score
			softScore += f.getWrappedFlight().getWeight(f.getSlot().getWrappedSlot());
        }

        flightPrioritization.incrementFitnessFunctionApplications();

        return HardSoftScore.of(hardScore, softScore);
    }
}
