package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.*;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.localsearch.decider.forager.privacypreserving.NeighbourhoodEvaluator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simulates the PrivacyEngine for Optaplanner.
 * @param <Solution_> generic solution
 */
public class SimulatedPrivacyEngine<Solution_> implements NeighbourhoodEvaluator<Solution_> {
    private final FlightPrioritizationEasyScoreCalculator calculator;
    private final int aboveThresholdMinimumSize;
    public SimulatedPrivacyEngine(){
        this.calculator = new FlightPrioritizationEasyScoreCalculator();
        this.aboveThresholdMinimumSize = 3;
    }

    @Override
    public Map<Score, Solution_> getBestSolutionFromNeighbourhood(List<Solution_> candidates) {
        var order = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        var entry = order.entrySet().stream().findFirst();

        if(entry.isEmpty()) return null;

        var map = new HashMap<Score, Solution_>();
        map.put(entry.get().getKey(), (Solution_)entry.get().getValue().get(0));
        return map;
    }

    @Override
    public Map<Score, List<Solution_>> getCandidatesAboveThreshold(List<Solution_> candidates, double threshold) {
        var orderedFlightPrioMap = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        var highScore = orderedFlightPrioMap.entrySet().stream().findFirst().get().getKey();
        var ordering = orderedFlightPrioMap.entrySet().stream().findFirst().get().getValue();

        List<FlightPrioritization> flightPrioritizationsAboveThreshold;
        double thresholdDecrement = 0.001;
        do{
            var thresholdScore = highScore.getSoftScore() > 0 ? highScore.getSoftScore() * threshold : highScore.getSoftScore() * (1 + (1 - threshold));
            flightPrioritizationsAboveThreshold = ordering.stream()
                    .filter(flightPrio -> calculator.calculateScore(flightPrio).getSoftScore() > thresholdScore)
                    .collect(Collectors.toList());
            threshold = threshold - thresholdDecrement;
        }while(flightPrioritizationsAboveThreshold.size() < aboveThresholdMinimumSize);

        var averageScore = flightPrioritizationsAboveThreshold.stream()
                .mapToInt(flightPrio -> calculator.calculateScore(flightPrio).getSoftScore())
                .average()
                .getAsDouble();
        var solutionsAboveThrehold = new ArrayList<Solution_>();
        for(var r : flightPrioritizationsAboveThreshold ){
            solutionsAboveThrehold.add((Solution_)r);
        }
        var map = new HashMap<Score, List<Solution_>>();
        map.put(HardSoftScore.ofSoft((int)averageScore), solutionsAboveThrehold);
        return map;
    }

    @Override
    public Map<Score, List<Solution_>> getTopCandidatesAndAverageScore(List<Solution_> candidates, double threshold) {
        var orderedFlightPrioMap = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        var ordering = orderedFlightPrioMap.entrySet().stream().findFirst().get().getValue();

        long topBucketSize = Math.round(ordering.size() * threshold);
        var topFlightPrios = ordering.stream()
                .limit(topBucketSize)
                .collect(Collectors.toList());
        var averageScore = topFlightPrios.stream()
                .mapToInt(flightPrio -> calculator.calculateScore(flightPrio).getSoftScore())
                .average()
                .getAsDouble();

        var topSolutions = new ArrayList<Solution_>();
        for(var r : topFlightPrios ){
            topSolutions.add((Solution_)r);
        }


        var map = new HashMap<Score, List<Solution_>>();
        map.put(HardSoftScore.ofSoft((int)averageScore), topSolutions);
        return map;
    }

    /*
            Internal-data-representation evaluation
     */
    public Map<HardSoftScore, List<FlightPrioritization>> evaluateFlightPrioritizations(List<FlightPrioritization> candidates){
        var map = new HashMap<HardSoftScore, List<FlightPrioritization>>();
        List<FlightPrioritization> sortedList = candidates
                .stream()
                .sorted(Comparator.comparing(calculator::calculateScore))
                .collect(Collectors.toList());
        Collections.reverse(sortedList);

        map.put(calculator.calculateScore(sortedList.get(0)), sortedList);
        return map;
    }

}
