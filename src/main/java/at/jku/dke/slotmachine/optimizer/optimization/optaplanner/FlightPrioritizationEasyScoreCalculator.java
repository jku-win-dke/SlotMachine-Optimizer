package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

public class FlightPrioritizationEasyScoreCalculator implements EasyScoreCalculator<FlightPrioritization, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(FlightPrioritization flightPrioritization) {
        int hardScore = 0;
        int softScore = 0;

		if (flightPrioritization.getFitnessFunctionInvocations() < 0) {	// used for logger
			flightPrioritization.setFitnessFunctionInvocations(0);
		}

		flightPrioritization.setFitnessFunctionInvocations(flightPrioritization.getFitnessFunctionInvocations() + 1); // used for logger

        for(FlightPlanningEntity f : flightPrioritization.getFlights()) {
            if(f.getWrappedFlight().getScheduledTime().isAfter(f.getSlot().getTime())) {
                hardScore--;
            }

            for(FlightPlanningEntity e : flightPrioritization.getFlights()) {
                if(!f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore--;
                }
            }

			// sorted list of instants (needed to get positions of slots accurately)
			List<Instant> sortedSlots = new LinkedList<>();
			for (SlotProblemFact s: flightPrioritization.getSlots()) {
				sortedSlots.add(s.getTime());
			}

			Collections.sort(sortedSlots);
            
			// returns position of slot in weight array
			int posOfSlot = sortedSlots.indexOf(f.getSlot().getTime());
			
			// adds weight at position of slot
			softScore += f.getWrappedFlight().getWeights()[posOfSlot];
        }

        return HardSoftScore.of(hardScore, softScore);
    }
}
