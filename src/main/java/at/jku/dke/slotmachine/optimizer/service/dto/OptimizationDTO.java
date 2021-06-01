package at.jku.dke.slotmachine.optimizer.service.dto;

import java.util.UUID;

public class OptimizationDTO {
    private UUID optId;
	private String[] initialFlightSequence;
	private FlightDTO[] flights;
	private SlotDTO[] slots;
	private OptimizationFramework optimizationFramework;
	private JeneticConfigDTO jenConfig;
	private OptaPlannerConfigDTO optaPlannerConfig;
	private MarginDTO[] margins;

	public enum OptimizationFramework {
		JENETICS, OPTAPLANNER, BENCHMARK, BENCHMARKOPTAPLANNER
	}

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

	public OptimizationFramework getOptimizationFramework() {
		return optimizationFramework;
	}

	public void setOptimizationFramework(OptimizationFramework optimizationFramework) {
		this.optimizationFramework = optimizationFramework;
	}

	public JeneticConfigDTO getJenConfig() {
		return jenConfig;
	}

	public void setJenConfig(JeneticConfigDTO jenConfig) {
		this.jenConfig = jenConfig;
	}

	public OptaPlannerConfigDTO getOptaPlannerConfig() {
		return optaPlannerConfig;
	}

	public void setOptaPlannerConfig(OptaPlannerConfigDTO optaPlannerConfig) {
		this.optaPlannerConfig = optaPlannerConfig;
	}

	public MarginDTO[] getMargins() {
		return margins;
	}

	public void setMargins(MarginDTO[] margins) {
		this.margins = margins;
	}
	
}
