package at.jku.dke.slotmachine.optimizer.service.dto;

import java.time.Instant;
import java.util.UUID;


public class OptimizationResultMarginsDTO {
    private UUID optId;
    private String[] flightSequence;
    private MarginDTO[] margins;
    private int sumOfWeights;
    private Instant[] slots;
    private int fitnessApplications;

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

	public Instant[] getSlots() {
		return slots;
	}

	public void setSlots(Instant[] slots) {
		this.slots = slots;
	}

	public int getFitnessFunctionApplications() {
		return fitnessApplications;
	}

	public void setFitnessFunctionApplications(int fitnessFunctionApplications) {
		this.fitnessApplications = fitnessFunctionApplications;
	}
}
