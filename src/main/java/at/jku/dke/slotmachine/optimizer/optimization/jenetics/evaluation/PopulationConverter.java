package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class to convert a population to the format required by the PE
 */
public class PopulationConverter {
    private PopulationConverter(){
        // utility class should not be initialized
    }

    /**
     * Convert the population from the Jenetics native representation to the array format required by the
     * Privacy Engine.
     * @param population the population in Jenetics representation
     * @return the population in array format required by Privacy Engine
     */
    public static Integer[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population, SlotAllocationProblem problem) {
        return population.stream()
                .map(phenotype -> problem.decode(phenotype.genotype()))
                .map(map ->
                        // 1. Get a flight list from the mapping of flights to slots, where the flights are
                        // ordered by their assigned time slot.
                        // 2. Replace the flights by their position in the problem's sequence of flights.
                        map.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .map(flight -> problem.getFlights().indexOf(flight))
                                .toArray(Integer[]::new)
                ).toArray(Integer[][]::new);
    }


    /**
     * Get a Predicate that returns whether it has seen the elements' key according to the keyExtractor
     * @param keyExtractor extracts the key from T for filtering
     * @param <T> generic type
     * @return a stateful filter
     */
    public static <T> Predicate<T> distinctByAttribute(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

}
