package at.jku.dke.slotmachine.optimizer.service.dto;

import java.util.UUID;


public class OptimizationResultMarginsDTO {
    private UUID optId;
    private String[] flightSequence;
    private MarginDTO[] margins;
    private int sumOfWeights;

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
    
	public MarginDTO[] getMargins() {
		return margins;
	}

	public void setMargins(MarginDTO[] margins) {
		this.margins = margins;
	}

	public int getSumOfWeights() {
		return sumOfWeights;
	}

	public void setSumOfWeights(int sumOfWeights) {
		this.sumOfWeights = sumOfWeights;
	}
}
