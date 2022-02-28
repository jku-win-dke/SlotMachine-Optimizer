package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritizationEasyScoreCalculator;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedPrivacyEngine {
    private final FlightPrioritizationEasyScoreCalculator calculator;

    public SimulatedPrivacyEngine(){
        this.calculator = new FlightPrioritizationEasyScoreCalculator();
    }

    public Map<HardSoftScore, List<FlightPrioritization>> evaluateCandidates(List<FlightPrioritization> candidates){
        var map = new HashMap<HardSoftScore, List<FlightPrioritization>>();
        List<FlightPrioritization> sortedList = candidates.stream().sorted(Comparator.comparing(calculator::calculateScore)).collect(Collectors.toList());
        Collections.reverse(sortedList);

        map.put(calculator.calculateScore(sortedList.get(0)), sortedList);
        return map;
    }


}
