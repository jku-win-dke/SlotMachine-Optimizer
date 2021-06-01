package at.jku.dke.slotmachine.optimizer.service.dto;

import java.util.UUID;


public class OptimizationResultDTO {
    private UUID optId;
    private String[] flightSequence;
    
    public OptimizationResultDTO(UUID optId, String[] flightSequence) {
		super();
		this.optId = optId;
		this.flightSequence = flightSequence;
	}

	public UUID getOptId() {
        return optId;
    }

    public void setOptId(UUID optId) {
        this.optId = optId;
    }

    public String[] getFlightSequence() {
        return flightSequence;
    }

    public void setFlightSequence(String[] flightSequence) {
        this.flightSequence = flightSequence;
    }
}
