package at.jku.dke.slotmachine.optimizer.rest.legacy;

import org.springframework.lang.Nullable;

import java.time.Instant;

public class FlightDTO {
    private String flightId;

    private Instant scheduledTime;

    @Nullable
    private int[] weightMap; // List of weights for slots; only used in mode NON_PRIVACY_PRESERVING

    public FlightDTO(String flightId, Instant scheduledTime, int[] weightMap) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.weightMap = weightMap;
    }

    public FlightDTO(String flightId, Instant scheduledTime) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
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
