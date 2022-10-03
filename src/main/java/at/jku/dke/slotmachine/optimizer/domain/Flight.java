package at.jku.dke.slotmachine.optimizer.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flight implements Comparable<Flight> {
    private String flightId;
    private LocalDateTime scheduledTime;
    private int[] weights;
    private Map<Slot, Integer> weightMap;
    private Margins margins;

    public Flight(String flightId, LocalDateTime scheduledTime, int[] weights) {
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

        if (weights == null) {
        	// in SECRET mode no weights are stored in Flight and the
        	// weight map cannot be computed
        	return;
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

    public void setWeights(int[] weights) {
        this.weights = weights;
    }

    public Margins getMargins() { return margins; }

    public void setMargins(Margins margins) { this.margins = margins; }

    /**
     * Flights are sorted by their scheduled time. A flight with no scheduled time is always assumed to be after
     * a flight with a scheduled time. If two flights have no scheduled time, they have the same position; none is
     * before or after the other.
     */
    @Override
    public int compareTo(Flight flight) {
        int comparison;

        if(this.getScheduledTime() == null){
            if(flight.getScheduledTime() == null) {
                comparison = 0;
            } else {
                comparison = 1;
            }
        } else {
            comparison = this.getScheduledTime().compareTo(flight.getScheduledTime());
        }

        return comparison;
    }
}
