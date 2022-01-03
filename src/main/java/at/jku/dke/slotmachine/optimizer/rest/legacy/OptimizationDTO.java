package at.jku.dke.slotmachine.optimizer.rest.legacy;

import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationModeEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatusEnum;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class OptimizationDTO {
    private UUID optId;

	public UUID getOptId() {
		return optId;
	}

	public void setOptId(UUID optId) {
		this.optId = optId;
	}

	private FlightDTO[] flights;
	private SlotDTO[] slots;

	@Nullable
	private String[] initialFlightSequence;

	@Nullable
	private MarginsDTO[] margins;

	@Nullable
	private Map<String,Object> parameters;

	@Nullable
	private String optimizationFramework; // JENETICS, OPTAPLANNER, HUNGARIAN

	@Nullable
	private String fitnessEstimator;

	@Nullable
	private OptimizationModeEnum optimizationMode; // -- PRIVACY_PRESERVING; NON_PRIVACY_PRESERVING

	@Nullable
	private OptimizationStatusEnum optimizationStatus; // -- CREATED, INITIALIZED, RUNNING, DONE

	@Nullable
	private String privacyEngineEndpoint;

	@Nullable
	private Instant timestamp;

	public MarginsDTO[] getMargins() {
		return margins;
	}

	public void setMargins(MarginsDTO[] margins) {
		this.margins = margins;
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

	public OptimizationModeEnum getOptimizationMode() {
		return optimizationMode;
	}

	public void setOptimizationMode(OptimizationModeEnum optimizationMode) {
		this.optimizationMode = optimizationMode;
	}

	public OptimizationStatusEnum getOptimizationStatus() {
		return optimizationStatus;
	}

	public void setOptimizationStatus(OptimizationStatusEnum optimizationStatus) {
		this.optimizationStatus = optimizationStatus;
	}

	@Nullable
	public String getPrivacyEngineEndpoint() {
		return privacyEngineEndpoint;
	}

	public void setPrivacyEngineEndpoint(@Nullable String privacyEngineEndpoint) {
		this.privacyEngineEndpoint = privacyEngineEndpoint;
	}


	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Nullable
	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(@Nullable Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getFitnessEstimator() {
		return fitnessEstimator;
	}

	public void setFitnessEstimator(String fitnessEstimator) {
		this.fitnessEstimator = fitnessEstimator;
	}
}
