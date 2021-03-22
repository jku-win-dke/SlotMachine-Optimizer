package at.jku.dke.slotmachine.optimizer.rest.dto;

import java.util.List;
import java.util.UUID;

public class FlightSequenceDTO {
    private UUID sessionId;

    private List<String> flightSequence;

    public FlightSequenceDTO(UUID sessionId, List<String> flightSequence) {
        this.sessionId = sessionId;
        this.flightSequence = flightSequence;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getFlightSequence() {
        return flightSequence;
    }

    public void setFlightSequence(List<String> flightSequence) {
        this.flightSequence = flightSequence;
    }
}
