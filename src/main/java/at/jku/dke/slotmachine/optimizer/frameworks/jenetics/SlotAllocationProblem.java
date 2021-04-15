package at.jku.dke.slotmachine.optimizer.frameworks.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import io.jenetics.EnumGene;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.Problem;
import io.jenetics.engine.RetryConstraint;
import io.jenetics.util.ISeq;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.optimizer.service.dto.*;

public class SlotAllocationProblem implements Problem<Map<Flight, Slot>, EnumGene<Integer>, Integer>{

	private final ISeq<Flight> flights;
	private final ISeq<Slot> availableSlots;
	private static final Logger logger = LogManager.getLogger();
	private int fitnessIterations;
	
	public SlotAllocationProblem(ISeq<Flight> flights, ISeq<Slot> availableSlots) {
		this.flights = flights;
		this.availableSlots = availableSlots;
	}
	
    @Override
    public Function<Map<Flight, Slot>, Integer> fitness() {
        return new Function<Map<Flight, Slot>, Integer>() {

			@Override
			public Integer apply(Map<Flight, Slot> t) {
				if (getFitnessIterations() < 0) {					// used for logger
					setFitnessIterations(0);
				}
				setFitnessIterations(getFitnessIterations() + 1); 	// used for logger
				// sorted list of instants (needed to get positions of slots accurately)
				List<Instant> sortedSlots = new LinkedList<Instant>();
				for (Slot s: availableSlots) {
					sortedSlots.add(s.getTime());
				}
				Collections.sort(sortedSlots);
				
				// can be used to print which slot number is assigned to which time in sortedSlots
				int j = 0;
				for(Instant i: sortedSlots) {
					logger.debug("Slot " + j + ": " + i);
					j++;
				}
				
				//sum of all weights for given assigned slots, default value 0
				int sum = 0;

				logger.debug("printing weights from flights according to slots" +
							 " during fitness function calls:\n");

				// gets each flight and the weight from the assigned slot
				for(Map.Entry<Flight, Slot> entry: t.entrySet()) {
					Flight flight = entry.getKey();
					Slot slot = entry.getValue();
					logger.debug("flight: " + flight.getFlightId() + 
							": " + slot.getTime() + " -> slot: " +  
							sortedSlots.indexOf(slot.getTime()) + ": weight: " +
							flight.getWeightMap()[sortedSlots.indexOf(slot.getTime())]+"\n");
					
					// returns position of slot in weight array
					int posOfSlot = sortedSlots.indexOf(slot.getTime());
					
					// adds weight at position of slot
					sum = sum + flight.getWeightMap()[posOfSlot];
				}
				return sum;
			}
        };
    }

    @Override
    public InvertibleCodec<Map<Flight, Slot>, EnumGene<Integer>> codec() {
        return Codecs.ofMapping(flights, availableSlots);
    }

    @Override
    public Optional<Constraint<EnumGene<Integer>, Integer>> constraint() {
        Optional<Constraint<EnumGene<Integer>, Integer>> constraint = Optional.of(
                RetryConstraint.of(
                        codec(),
                        flightSlotMap -> {
                                boolean noFlightBeforeScheduledTime =
                                    flightSlotMap.entrySet()
                                                 .stream()
                                                 .noneMatch(entry ->
                                                            entry.getKey()
                                                                 .getScheduledTime()
                                                                 .isAfter(entry.getValue().getTime())
                                                 );

                                boolean noSlotBookedMultipleTimes =
                                    flightSlotMap.entrySet()
                                                 .stream()
                                                 .noneMatch(entry ->
                                                         flightSlotMap.entrySet().stream().anyMatch(
                                                             other -> entry != other  &&
                                                                      entry.getValue().equals(other.getValue()))
                                                 );

                                return noFlightBeforeScheduledTime && noSlotBookedMultipleTimes;
                        }
                )
        );

        return constraint;
    }

	public int getFitnessIterations() {
		return fitnessIterations;
	}
	public void setFitnessIterations(int i) {
		fitnessIterations = i;
	}
}
