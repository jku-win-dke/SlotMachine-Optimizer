package at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.jku.dke.slotmachine.optimizer.domain.Slot;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

public class FlightPrioritizationEasyScoreCalculator implements EasyScoreCalculator<FlightPrioritization, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(FlightPrioritization flightPrioritization) {
        int hardScore = 0;
        int softScore = 0;
		if (flightPrioritization.getApplications() < 0) {									// used for logger
			flightPrioritization.setApplications(0);
		}
		flightPrioritization.setApplications(flightPrioritization.getApplications() + 1); 	// used for logger
        for(FlightBenchmarkOptaPlanner f : flightPrioritization.getFlights()) {
            if(f.getSlot() != null && f.getScheduledTime().isAfter(f.getSlot().getTime())) {
                hardScore--;
            }

            for(FlightBenchmarkOptaPlanner e : flightPrioritization.getFlights()) {
                if(!f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore--;
                }
            }

			// sorted list of instants (needed to get positions of slots accurately)
			List<Instant> sortedSlots = new LinkedList<Instant>();
			for (Slot s: flightPrioritization.getSlots()) {
				sortedSlots.add(s.getTime());
			}
			Collections.sort(sortedSlots);
            
			// returns position of slot in weight array
			if (f.getSlot() != null) {
				int posOfSlot = sortedSlots.indexOf(f.getSlot().getTime());
			
				// adds weight at position of slot
				softScore += f.getWeightMap()[posOfSlot];
			} else {
				hardScore--;
			}
        }

        return HardSoftScore.of(hardScore, softScore);
    }
}
