package at.jku.dke.slotmachine.optimizer.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flight implements Comparable<Flight> {
    private String flightId;
    private LocalDateTime scheduledTime;
    private double[] weights;
    private Map<Slot, Double> weightMap;
    private Margins margins;

    public Flight(String flightId, LocalDateTime scheduledTime, double[] weights) {
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

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
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

    public double getWeight(Slot s) {
        double weight = Double.MIN_VALUE;

        if(weightMap != null && weightMap.containsKey(s)) {
            weight = weightMap.get(s);
        }

        return weight;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeightMap(double[] weights) {
        this.weights = weights;
    }

    public Margins getMargins() { return margins; }

    public void setMargins(Margins margins) { this.margins = margins; }

    @Override
    public int compareTo(Flight flight) {
        return this.getScheduledTime().compareTo(flight.getScheduledTime());
    }
}
