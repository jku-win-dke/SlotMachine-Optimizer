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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FlightListPermutationProblem implements Problem<ISeq<Flight>, EnumGene<Flight>, Integer> {
    private static final Logger logger = LogManager.getLogger();

    private final ISeq<Flight> availableFlights;
    private final ISeq<Slot> availableSlots;

	private int fitnessIterations;

    public FlightListPermutationProblem(ISeq<Flight> availableFlights, ISeq<Slot> availableSlots) {
        this.availableFlights = availableFlights;

        // order the slots by their time (maybe not needed)
        this.availableSlots = ISeq.of(availableSlots.stream().sorted().toList());
    }

    @Override
    public Function<ISeq<Flight>, Integer> fitness() {
        return new Function<ISeq<Flight>, Integer>() {
            @Override
            public Integer apply(ISeq<Flight> flights) {
                int sum = 0;

                for(Flight f: flights) {
                    int pos = flights.indexOf(f);
                    int weight = f.getWeightMap()[pos];
                    sum += weight;
                }

                return sum;
            }
        };
    }

    @Override
    public InvertibleCodec<ISeq<Flight>, EnumGene<Flight>> codec() {
        return Codecs.ofPermutation(availableFlights);
    }
    
    @Override
    public Optional<Constraint<EnumGene<Flight>, Integer>> constraint() {
        Optional<Constraint<EnumGene<Flight>, Integer>> constraint = Optional.of(
                RetryConstraint.of(
                        codec(),
                        flightSlotMap -> {
                                boolean noFlightBeforeScheduledTime = true;
                                
                                /*for (Map.Entry<Flight, Slot> entry: flightSlotMap.entrySet()) {
                					Flight flight = entry.getKey();
                					Slot slot = entry.getValue();
                					if (flight.getScheduledTime().isAfter(slot.getTime())) {
                						noFlightBeforeScheduledTime = false;
                					}
                                }*/

                                /* not used due to the Codec-Mapping
                                boolean noSlotBookedMultipleTimes =
                                    flightSlotMap.entrySet()
                                                 .stream()
                                                 .noneMatch(entry ->
                                                         flightSlotMap.entrySet().stream().anyMatch(
                                                             other -> entry != other  &&
                                                                      entry.getValue().equals(other.getValue()))
                                                 );*/
                                
                                return (!noFlightBeforeScheduledTime);// && (!noSlotBookedMultipleTimes);
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
