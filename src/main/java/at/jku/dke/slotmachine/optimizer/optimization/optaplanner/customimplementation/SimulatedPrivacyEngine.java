package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.*;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedPrivacyEngine<Solution_> implements NeighbourhoodEvaluator<Solution_> {
    private final FlightPrioritizationEasyScoreCalculator calculator;

    public SimulatedPrivacyEngine(){
        this.calculator = new FlightPrioritizationEasyScoreCalculator();
    }

    @Override
    public Map<HardSoftScore, List<Solution_>> evaluateNeighbourhood(List<Solution_> candidates) {
        var result = evaluateFlightPrioritizations((List<FlightPrioritization>)candidates);
        var solutions = new ArrayList<Solution_>();
        for(var r : result.entrySet().stream().findFirst().get().getValue()){
            solutions.add((Solution_)r);
        }
        var map = new HashMap<HardSoftScore, List<Solution_>>();
        map.put(result.entrySet().stream().findFirst().get().getKey(), solutions);
        return map;
    }

    /*
            Internal-data-representation evaluation
     */
    public Map<HardSoftScore, List<FlightPrioritization>> evaluateFlightPrioritizations(List<FlightPrioritization> candidates){
        var map = new HashMap<HardSoftScore, List<FlightPrioritization>>();
        List<FlightPrioritization> sortedList = candidates.stream().sorted(Comparator.comparing(calculator::calculateScore)).collect(Collectors.toList());
        Collections.reverse(sortedList);

        map.put(calculator.calculateScore(sortedList.get(0)), sortedList);
        return map;
    }
}
