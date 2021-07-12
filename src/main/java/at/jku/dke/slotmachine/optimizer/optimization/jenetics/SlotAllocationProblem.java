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

	public SlotAllocationProblem(ISeq<Flight> flights, ISeq<Slot> availableSlots) {
		this.flights = flights;
		this.availableSlots = availableSlots;

		// for each flight compute the weight map
		Slot[] slotArray = availableSlots.toArray(Slot[]::new);
		for(Flight f : flights) {
			f.computeWeightMap(slotArray);
		}
	}
	
    @Override
    public Function<Map<Flight, Slot>, Integer> fitness() {
        return new Function<Map<Flight, Slot>, Integer>() {
			@Override
			public Integer apply(Map<Flight, Slot> slotAllocation) {
				int sum = slotAllocation.keySet().stream()
						.map(f -> f.getWeight(slotAllocation.get(f)))
						.mapToInt(Integer::intValue)
						.sum();

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
                                boolean noFlightBeforeScheduledTime = true;
                                
                                for (Map.Entry<Flight, Slot> entry: flightSlotMap.entrySet()) {
                					Flight flight = entry.getKey();
                					Slot slot = entry.getValue();
                					if (flight.getScheduledTime().isAfter(slot.getTime())) {
                						noFlightBeforeScheduledTime = false;
                					}
                                }
                                
                                return (!noFlightBeforeScheduledTime);
                        }
                )
        );
        
        return constraint;
    }

}
