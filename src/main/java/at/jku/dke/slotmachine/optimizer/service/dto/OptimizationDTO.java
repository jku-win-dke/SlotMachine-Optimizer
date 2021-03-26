package at.jku.dke.slotmachine.optimizer.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OptimizationDTO {
    private UUID optId;
	private String[] initialFlightSequence;
	private FlightDTO[] flights;
	private SlotDTO[] slots;

	public UUID getOptId() {
		return optId;
	}

	public void setOptId(UUID optId) {
		this.optId = optId;
	}

	public String[] getInitialFlightSequence() {
		return initialFlightSequence;
	}

	public void setInitialFlightSequence(String[] initialFlightSequence) {
		this.initialFlightSequence = initialFlightSequence;
	}

	public FlightDTO[] getFlights() {
		return flights;
	}

	public void setFlights(FlightDTO[] flights) {
		this.flights = flights;
	}

	public SlotDTO[] getSlots() {
		return slots;
	}

	public void setSlots(SlotDTO[] slots) {
		this.slots = slots;
	}
}
