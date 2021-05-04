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
    
    public Optimization(List<Flight> flightList, List<Slot> slotList, Run optimization, UUID optId, JeneticsConfig jenConfig) {
		this.flightList = flightList;
		this.slotList = slotList;
		this.optimization = optimization;
		this.setOptId(optId);
		this.setJenConfig(jenConfig);
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

	

    
}