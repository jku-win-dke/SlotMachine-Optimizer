package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritization;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPrioritizationEasyScoreCalculator;
import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.SlotProblemFact;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedPrivacyEngine {
    private final FlightPrioritizationEasyScoreCalculator calculator;
    private Map<FlightPrioritization, Integer[]> flightPrioritizationOrderMap;

    public SimulatedPrivacyEngine(){
        this.flightPrioritizationOrderMap = new HashMap<>();
        this.calculator = new FlightPrioritizationEasyScoreCalculator();
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

    /*
        External-data-representation evaluation
     */
    public Map<HardSoftScore, Integer[][]> evaluateFlightOrderArrays(Integer[][] flightOrderArrays, FlightPrioritization currentSolution) {
        Map<HardSoftScore, Integer[][]> result = new HashMap<>();
        var evaluation = this.getInternalMapFromOrderArrays(flightOrderArrays, currentSolution);
        var optSortedFlightPrios = evaluation.entrySet().stream().findFirst();

        if(optSortedFlightPrios.isEmpty()) {
            result.put(HardSoftScore.of(0, 0), flightOrderArrays);
        }
        var sortedFlightPrios = optSortedFlightPrios.get().getValue();
        var sortedFlightOrderArrays = new Integer[sortedFlightPrios.size()][];
        int i = 0;
        for(var flightPrio : sortedFlightPrios){
            sortedFlightOrderArrays[i] = flightPrioritizationOrderMap.get(flightPrio);
            i++;
        }

        result.put(optSortedFlightPrios.get().getKey(), sortedFlightOrderArrays);
        return result;
    }

    private Map<HardSoftScore, List<FlightPrioritization>> getInternalMapFromOrderArrays(Integer[][] flightOrderArrays, FlightPrioritization currentSolution){
        this.flightPrioritizationOrderMap.clear();
        List<FlightPrioritization> flightPrioritizations = new ArrayList<>();

        FlightPrioritization flightPrioritization;
        FlightPlanningEntity flightPlanningEntity;
        List<FlightPlanningEntity> flightPlanningEntities;
        List<SlotProblemFact> slots = new ArrayList<>(currentSolution.getSlots());
        for(int row = 0; row < flightOrderArrays.length ; row++){
            flightPlanningEntities = new ArrayList<>();
            for(int column = 0; column < flightOrderArrays[row].length; column++){
                flightPlanningEntity = new FlightPlanningEntity(currentSolution.getFlights().get(flightOrderArrays[row][column]).getWrappedFlight());
                flightPlanningEntity.setSlot(currentSolution.getSlots().get(column));
                flightPlanningEntities.add(flightPlanningEntity);
            }
            flightPrioritization =  new FlightPrioritization(slots, flightPlanningEntities);
            flightPrioritizations.add(flightPrioritization);
            flightPrioritizationOrderMap.put(flightPrioritization, flightOrderArrays[row]);
        }

        return this.evaluateFlightPrioritizations(flightPrioritizations);
    }



    public Map<HardSoftScore, Integer[][]> evaluateFlightOrderArrays2(Integer[][] flightOrderArrays, InnerScoreDirector director) {
        Map<HardSoftScore, Integer[][]> result = new HashMap<>();
        var evalutaion = this.getInternalMapFromOrderArrays2(flightOrderArrays, director);
        var optSortedFlightPrios = evalutaion.entrySet().stream().findFirst();

        if(optSortedFlightPrios.isEmpty()) {
            result.put(HardSoftScore.of(0, 0), flightOrderArrays);
        }
        var sortedFlightPrios = optSortedFlightPrios.get().getValue();
        var sortedFlightOrderArrays = new Integer[sortedFlightPrios.size()][];
        int i = 0;
        for(var flightPrio : sortedFlightPrios){
            sortedFlightOrderArrays[i] = flightPrioritizationOrderMap.get(flightPrio);
            i++;
        }

        result.put(optSortedFlightPrios.get().getKey(), sortedFlightOrderArrays);
        return result;
    }

    private Map<HardSoftScore, List<FlightPrioritization>> getInternalMapFromOrderArrays2(Integer[][] flightOrderArrays, InnerScoreDirector director){
        this.flightPrioritizationOrderMap = new HashMap<>();
        List<FlightPrioritization> flightPrioritizations = new ArrayList<>();

        for(int row = 0; row < flightOrderArrays.length ; row++){
            var canidateSolution = (FlightPrioritization)director.cloneWorkingSolution();
            for(int col = 0; col < flightOrderArrays[row].length; col++){
                canidateSolution.getFlights().get(flightOrderArrays[row][col]).setSlot(canidateSolution.getSlots().get(col));
            }
            flightPrioritizations.add(canidateSolution);
            flightPrioritizationOrderMap.put(canidateSolution, flightOrderArrays[row]);
        }
        return this.evaluateFlightPrioritizations(flightPrioritizations);
    }


}
