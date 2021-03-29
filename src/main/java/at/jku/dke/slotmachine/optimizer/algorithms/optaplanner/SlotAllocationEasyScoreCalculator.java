package at.jku.dke.slotmachine.optimizer.algorithms.optaplanner;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO;

public class SlotAllocationEasyScoreCalculator implements EasyScoreCalculator<SlotSequence, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(SlotSequence slotSequence) {
        int hardScore = 0;
        int softScore = 0;

        for(FlightOptaPlanner f : slotSequence.getFlights()) {
            if(f.getScheduledTime().isAfter(f.getSlot().getTime())) {
                hardScore--;
            }

            // no flight after TimeNotAfter
            //if(f.getWeight(f.getSlot()) <= -10000) {
            //    hardScore--;
            //}

            for(FlightOptaPlanner e : slotSequence.getFlights()) {
                if(!f.equals(e) && f.getSlot() != null && e.getSlot() != null && f.getSlot().equals(e.getSlot())) {
                    hardScore--;
                }
            }

			// sorted list of instants (needed to get positions of slots accurately)
			List<Instant> sortedSlots = new LinkedList<Instant>();
			for (SlotDTO s: slotSequence.getSlots()) {
				sortedSlots.add(s.getTime());
			}
			Collections.sort(sortedSlots);
            
			// returns position of slot in weight array
			int posOfSlot = sortedSlots.indexOf(f.getSlot().getTime());
			
			// adds weight at position of slot
			softScore += f.getWeightMap()[posOfSlot];
        }

        return HardSoftScore.of(hardScore, softScore);
    }
}
