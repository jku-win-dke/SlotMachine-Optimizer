package at.jku.dke.slotmachine.optimizer.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flight implements Comparable<Flight> {
    private String flightId;
    private Instant scheduledTime;
    private int[] weights;
    private Map<Slot, Integer> weightMap;
    private Margins margins;

    public Flight(String flightId, Instant scheduledTime, int[] weights) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weights = weights;
    }
    
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void computeWeightMap(Slot[] slots) {
        if(weightMap != null) {
            weightMap.clear();
        } else {
            weightMap = new HashMap<>();
        }

        // sort the slots by their time
        List<Slot> slotList = Arrays.stream(slots).sorted().toList();

        // for each slot in the sorted slot list get the weight from the weights array
        for(Slot s : slotList){
            weightMap.put(s, weights[slotList.indexOf(s)]);
        }
    }

    public int getWeight(Slot s) {
        int weight = Integer.MIN_VALUE;

        if(weightMap != null && weightMap.containsKey(s)) {
            weight = weightMap.get(s);
        }

        return weight;
    }

    public int[] getWeights() {
        return weights;
    }

    public void setWeightMap(int[] weights) {
        this.weights = weights;
    }

    public Margins getMargins() { return margins; }

    public void setMargins(Margins margins) { this.margins = margins; }

    @Override
    public int compareTo(Flight flight) {
        return this.getScheduledTime().compareTo(flight.getScheduledTime());
    }
}
