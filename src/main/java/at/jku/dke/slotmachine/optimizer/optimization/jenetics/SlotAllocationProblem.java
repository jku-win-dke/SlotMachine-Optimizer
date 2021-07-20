package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import io.jenetics.EnumGene;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.Problem;
import io.jenetics.engine.RetryConstraint;
import io.jenetics.util.ISeq;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlotAllocationProblem implements Problem<Map<Flight, Slot>, EnumGene<Integer>, Integer>{
	private static final Logger logger = LogManager.getLogger();

	private final ISeq<Flight> flights;
	private final ISeq<Slot> availableSlots;

	private int fitnessFunctionApplications = 0;

	public SlotAllocationProblem(ISeq<Flight> flights, ISeq<Slot> availableSlots) {
		this.flights = flights;
		this.availableSlots = availableSlots;

		logger.debug("Compute weight map for each flight.");
		Slot[] slotArray = availableSlots.toArray(Slot[]::new);
		for(Flight f : flights) {
			f.computeWeightMap(slotArray);
		}
	}

	public int getFitnessFunctionApplications() {
		return fitnessFunctionApplications;
	}
	
    @Override
    public Function<Map<Flight, Slot>, Integer> fitness() {
        return slotAllocation -> {
			fitnessFunctionApplications++;

			return slotAllocation.keySet().stream()
					.map(f -> f.getWeight(slotAllocation.get(f)))
					.mapToInt(Integer::intValue)
					.sum();
		};
    }

    @Override
    public InvertibleCodec<Map<Flight, Slot>, EnumGene<Integer>> codec() {
        return Codecs.ofMapping(flights, availableSlots);
    }
    
    @Override
    public Optional<Constraint<EnumGene<Integer>, Integer>> constraint() {    	
		return Optional.of(
				RetryConstraint.of(
						codec(),
						flightSlotMap -> flightSlotMap.entrySet().stream()
								.allMatch(	//return true, if the following is true for all entries (that scheduledTime is before assigned time)
										entry -> entry.getKey().getScheduledTime().compareTo(entry.getValue().getTime()) <= 0
								)
				)
		);
    }

	public ISeq<Flight> getFlights() {
		return flights;
	}

	public ISeq<Slot> getAvailableSlots() {
		return availableSlots;
	}

}
