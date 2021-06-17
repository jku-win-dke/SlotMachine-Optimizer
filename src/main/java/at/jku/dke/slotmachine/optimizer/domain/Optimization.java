package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import java.util.List;
import java.util.UUID;

/***
 *
 */
public class Optimization {
	private UUID optId;
    private List<Flight> flightList;
    private List<Slot> slotList;
    private Run optimization; // specify the application used to run the optimization.
    private JeneticsConfig jenConfig; // specially for jenetics configuration
    private OptaPlannerConfig optaPlannerConfig; // specially for OptaPlanner configuration
    private Margin[] margins;
    private int fitnessApplications; // to count how often fitness function / score calculation is used
    
    public Optimization(List<Flight> flightList, List<Slot> slotList, Run optimization, UUID optId, JeneticsConfig jenConfig, 
    		OptaPlannerConfig optaPlannerConfig, Margin[] margins) {
		this.flightList = flightList;
		this.slotList = slotList;
		this.optimization = optimization;
		this.setOptId(optId);
		this.setJenConfig(jenConfig);
		this.setOptaPlannerConfig(optaPlannerConfig);
		this.setMargins(margins);
	}

	public List<Flight> getFlightList() {
		return flightList;
	}

	public void setFlightList(List<Flight> flightList) {
		this.flightList = flightList;
	}

	public List<Slot> getSlotList() {
		return slotList;
	}

	public void setSlotList(List<Slot> slotList) {
		this.slotList = slotList;
	}

	public Run getOptimization() {
		return optimization;
	}

	public void setOptimization(Run optimization) {
		this.optimization = optimization;
	}

	public UUID getOptId() {
		return optId;
	}

	public void setOptId(UUID optId) {
		this.optId = optId;
	}

	public JeneticsConfig getJenConfig() {
		return jenConfig;
	}

	public void setJenConfig(JeneticsConfig jenConfig) {
		this.jenConfig = jenConfig;
	}

	public OptaPlannerConfig getOptaPlannerConfig() {
		return optaPlannerConfig;
	}

	public void setOptaPlannerConfig(OptaPlannerConfig optaPlannerConfig) {
		this.optaPlannerConfig = optaPlannerConfig;
	}

	public Margin[] getMargins() {
		return margins;
	}

	public void setMargins(Margin[] margins) {
		this.margins = margins;
	}

	public int getFitnessApplications() {
		return fitnessApplications;
	}

	public void setFitnessApplications(int fitnessApplications) {
		this.fitnessApplications = fitnessApplications;
	}
    
}
