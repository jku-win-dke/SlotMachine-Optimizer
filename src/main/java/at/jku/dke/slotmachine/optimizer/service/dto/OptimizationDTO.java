package at.jku.dke.slotmachine.optimizer.service.dto;

import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.UUID;

public class OptimizationDTO {
    private UUID optId;
	private FlightDTO[] flights;
	private SlotDTO[] slots;

	@Nullable
	private String[] initialFlightSequence;

	private String optimizationFramework;

	private Map<String,Object> parameters;

	private MarginsDTO[] margins;


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

	public String getOptimizationFramework() {
		return optimizationFramework;
	}

	public void setOptimizationFramework(String optimizationFramework) {
		this.optimizationFramework = optimizationFramework;
	}

	public MarginsDTO[] getMargins() {
		return margins;
	}

	public void setMargins(MarginsDTO[] margins) {
		this.margins = margins;
	}


	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
