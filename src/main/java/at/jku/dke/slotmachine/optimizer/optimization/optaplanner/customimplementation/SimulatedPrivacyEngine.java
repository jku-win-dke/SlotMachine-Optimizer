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

    /**
     * Takes a list of candidates, evaluates them and returns the best candidate along with the fitness of it.
     * @param candidates the candidates
     * @return the best candidate and its score
     */
    @Override
    public Map<Score, Solution_> getBestSolutionFromNeighbourhood(List<Solution_> candidates) {
        // Get ordered flight prios
        Map<HardSoftScore, List<FlightPrioritization>> orderedFlightPrioritizations = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        var entry = orderedFlightPrioritizations.entrySet().stream().findFirst();

        if(entry.isEmpty()) return Collections.emptyMap();

        // Return best and score
        var resultMap = new HashMap<Score, Solution_>();
        resultMap.put(entry.get().getKey(), (Solution_)entry.get().getValue().get(0));
        return resultMap;
    }

    /**
     * Returns all candidates above the threshold and their average score.
     * @param candidates the candidates
     * @param threshold the threshold
     * @return a map with the avg score and the candidates above the threshold.
     */
    @Override
    public Map<Score, List<Solution_>> getCandidatesAboveThreshold(List<Solution_> candidates, double threshold, Double terminationFitness) {
        // get ordered flight-prios
        Map<HardSoftScore, List<FlightPrioritization>> orderedFlightPrioMap = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        HardSoftScore highScore = orderedFlightPrioMap.entrySet().stream().findFirst().get().getKey();
        List<FlightPrioritization> orderedFlightPrioritizations = orderedFlightPrioMap.entrySet().stream().findFirst().get().getValue();

        List<FlightPrioritization> flightPrioritizationsAboveThreshold;
        double averageDoubleScore;

        if(highScore.getSoftScore() < terminationFitness){
            double thresholdDecrement = 0.001;
            do{
                double thresholdDoubleScore = highScore.getSoftScore() > 0 ? highScore.getSoftScore() * threshold : highScore.getSoftScore() * (1 + (1 - threshold));
                flightPrioritizationsAboveThreshold = orderedFlightPrioritizations.stream()
                        .filter(flightPrio -> calculator.calculateScore(flightPrio).getSoftScore() > thresholdDoubleScore)
                        .toList();
                threshold = threshold - thresholdDecrement;
            }while(flightPrioritizationsAboveThreshold.size() < aboveThresholdMinimumSize);

                averageDoubleScore = flightPrioritizationsAboveThreshold.stream()
                    .mapToInt(flightPrio -> calculator.calculateScore(flightPrio).getSoftScore())
                    .average()
                    .getAsDouble();
        }else{
            FlightPrioritization winner = orderedFlightPrioritizations.get(0);
            flightPrioritizationsAboveThreshold = new ArrayList<>();
            flightPrioritizationsAboveThreshold.add(winner);
            averageDoubleScore = highScore.getSoftScore();
        }

        List<Solution_> solutionsAboveThrehold = new ArrayList<>();
        for(var r : flightPrioritizationsAboveThreshold ){
            solutionsAboveThrehold.add((Solution_)r);
        }
        var resultMap = new HashMap<Score, List<Solution_>>();
        resultMap.put(HardSoftScore.ofSoft((int)averageDoubleScore), solutionsAboveThrehold);
        return resultMap;
    }

    @Override
    public Map<Score, List<Solution_>> getTopCandidatesAndAverageScore(List<Solution_> candidates, double threshold, Double terminationFitness) {
        var orderedFlightPrioMap = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        var ordering = orderedFlightPrioMap.entrySet().stream().findFirst().get().getValue();
        HardSoftScore highScore = orderedFlightPrioMap.entrySet().stream().findFirst().get().getKey();

        double averageScore;
        List<FlightPrioritization> topFlightPrios;

        if(highScore.getSoftScore() < terminationFitness){
            long topBucketSize = Math.round(ordering.size() * threshold);
            topFlightPrios = ordering.stream()
                    .limit(topBucketSize)
                    .collect(Collectors.toList());
            averageScore = topFlightPrios.stream()
                    .mapToInt(flightPrio -> calculator.calculateScore(flightPrio).getSoftScore())
                    .average()
                    .getAsDouble();

        }else{
            topFlightPrios = new ArrayList<>();
            topFlightPrios.add(ordering.get(0));
            averageScore = highScore.getSoftScore();

        }
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
    private Map<HardSoftScore, List<FlightPrioritization>> evaluateFlightPrioritizations(List<FlightPrioritization> candidates){
        var map = new HashMap<HardSoftScore, List<FlightPrioritization>>();
        List<FlightPrioritization> sortedList = candidates
                .stream()
                .sorted(Comparator.comparing(calculator::calculateScore))
                .collect(Collectors.toList());
        Collections.reverse(sortedList);

        // TODO: switch to returning scores for all solutions to avoid redundandt score calculation
        /*
        Map<FlightPrioritization, Score> evaluatedFlightPrios = candidates.stream()
                        .collect(Collectors.toMap(Function.identity(), calculator::calculateScore));

        evaluatedFlightPrios.entrySet().stream()
                .max((f1, f2)-> evaluatedFlightPrios.get(f1).compareTo(evaluatedFlightPrios.get(f2)))
                        .get();
         */

        map.put(calculator.calculateScore(sortedList.get(0)), sortedList);
        return map;
    }

}
