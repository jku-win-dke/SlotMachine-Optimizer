package at.jku.dke.slotmachine.optimizer.service.dto;

import at.jku.dke.slotmachine.optimizer.domain.Flight;

import java.time.Instant;

public class FlightDTO {
    private String flightId;
    private Instant scheduledTime;
    private int[] weightMap;

    public FlightDTO(String flightId, Instant scheduledTime, int[] weightMap) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
    }

    public FlightDTO() { }

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
}
