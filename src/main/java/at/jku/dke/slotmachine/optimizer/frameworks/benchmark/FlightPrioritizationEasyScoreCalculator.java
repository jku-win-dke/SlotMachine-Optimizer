package at.jku.dke.slotmachine.optimizer.frameworks.benchmark;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import ch.qos.logback.classic.Logger;

import org.apache.logging.log4j.LogManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

public class FlightPrioritizationEasyScoreCalculator implements EasyScoreCalculator<FlightPrioritization, HardSoftScore> {
	
	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
	
	/**
	 * Calculates the score for FlightPrioritization.
	 * 
	 * If the suggested slot is before ScheduledTime, add -1 to hardScore. If two flights 
	 * use the same slot for suggested slots, add -100 to hardScore. If a flight does not 
	 * have a suggested slot, add -10 to hardScore. OptaPlanner tries to achieve a final 
	 * hardScore of 0, therefore, the defined constraints should be fulfilled.
	 * 
	 * SoftScore is added according to the weight. OptaPlanner tries to maximize the 
	 * softScore value.
	 * 
	 * @param flightPrioritization planning solution
	 * @return HardSoftScore score according to given planning solution
	 */
    @Override
    public HardSoftScore calculateScore(FlightPrioritization flightPrioritization) {
        int hardScore = 0;
        int softScore = 0;
		if (flightPrioritization.getApplications() < 0) {									// used for logger
			flightPrioritization.setApplications(0);
		}
		flightPrioritization.setApplications(flightPrioritization.getApplications() + 1); 	// used for logger
        for(FlightBenchmark f : flightPrioritization.getFlights()) {
            if(f.getSlot() != null && f.getScheduledTime().isAfter(f.getSlot().getTime())) {
                hardScore--;
            }

            for(FlightBenchmark e : flightPrioritization.getFlights()) {
                if(!f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore -= 100;
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
				// if no slot is assigned here
				softScore += 0;
				hardScore -= 10;
			}
        }

        return HardSoftScore.of(hardScore, softScore);
    }
}
