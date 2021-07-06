package at.jku.dke.slotmachine.optimizer.domain;

import java.time.Instant;

public class Flight {
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;
    private Margins margins;

    public Flight(String flightId, Instant scheduledTime, int[] weightMap) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
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

    public int[] getWeightMap() {
        return weightMap;
    }

    public void setWeightMap(int[] weightMap) {
        this.weightMap = weightMap;
    }

    public Margins getMargins() { return margins; }

    public void setMargins(Margins margins) { this.margins = margins; }
}
