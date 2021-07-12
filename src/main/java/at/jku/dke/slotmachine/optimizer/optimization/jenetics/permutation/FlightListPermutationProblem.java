package at.jku.dke.slotmachine.optimizer.optimization.jenetics.permutation;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import io.jenetics.EnumGene;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class FlightListPermutationProblem implements Problem<ISeq<Flight>, EnumGene<Flight>, Integer> {
    private static final Logger logger = LogManager.getLogger();

    private final ISeq<Flight> availableFlights;
    private final ISeq<Slot> availableSlots;

    public FlightListPermutationProblem(ISeq<Flight> availableFlights, ISeq<Slot> availableSlots) {
        this.availableFlights = availableFlights;

        // order the slots by their time
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
                    int weight = f.getWeights()[pos];
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
}
