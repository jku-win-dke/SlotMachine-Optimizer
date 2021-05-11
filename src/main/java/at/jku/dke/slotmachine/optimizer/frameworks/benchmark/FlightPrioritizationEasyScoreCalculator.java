package at.jku.dke.slotmachine.optimizer.frameworks.benchmark;

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
        if (flightPrioritization.getApplications() < 0) {									// used for logger
			flightPrioritization.setApplications(0);
		}
        flightPrioritization.setApplications(flightPrioritization.getApplications() + 1); 
        
		List<Instant> sortedSlots = new LinkedList<Instant>();
		for (Slot s: flightPrioritization.getSlots()) {
			sortedSlots.add(s.getTime());
		}
		Collections.sort(sortedSlots);
        
        for (FlightBenchmark f: flightPrioritization.getFlights()) {
        	// constraints
        	if (f.getSlot() != null && f.getScheduledTime().isAfter(f.getSlot().getTime())) {
        		hardScore--;
        	}
            for(FlightBenchmark e : flightPrioritization.getFlights()) {
                if(!f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore--;
                }
            }
            
            int posOfSlot = -1;
			if (posOfSlot >= 0) {
				softScore += f.getWeightMap()[posOfSlot];
			}
            
        }
        
        
        
		/*if (flightPrioritization.getApplications() < 0) {									// used for logger
			flightPrioritization.setApplications(0);
		}
		flightPrioritization.setApplications(flightPrioritization.getApplications() + 1); 	// used for logger
        for(Flight f : flightPrioritization.getFlights()) {
            if(f.getSlot() != null && f.getScheduledTime().isAfter(f.getSlot().getTime())) {
                hardScore--;
            } else {
            	//hardScore--;
            }

            for(Flight e : flightPrioritization.getFlights()) {
                if(f.getSlot() != null && !f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore--;
                } else {
                	//
                }
            }

			// sorted list of instants (needed to get positions of slots accurately)
			List<Instant> sortedSlots = new LinkedList<Instant>();
			for (Slot s: flightPrioritization.getSlots()) {
				sortedSlots.add(s.getTime());
			}
			Collections.sort(sortedSlots);
            
			// returns position of slot in weight array
			int posOfSlot = -1;
			if (f.getSlot() != null) {
				posOfSlot = sortedSlots.indexOf(f.getSlot().getTime());
			}
			// adds weight at position of slot
			if (posOfSlot >= 0) {
				softScore += f.getWeightMap()[posOfSlot];
			} else {
				softScore += 0;
			}
        }*/

        return HardSoftScore.of(hardScore, softScore);
    }
}
