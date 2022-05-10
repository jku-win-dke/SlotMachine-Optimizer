package at.jku.dke.slotmachine.optimizer.optimization;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.service.PrivacyEngineService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/***
 *
 */
public abstract class Optimization {
	private UUID optId;
    private Flight[] flights;
    private Slot[] slots;
	private List<Map<Flight, Slot>> results = null;
	private FitnessEstimator fitnessEstimator;

	private OptimizationMode mode = OptimizationMode.NON_PRIVACY_PRESERVING;
	private OptimizationStatus status = OptimizationStatus.CREATED;

	private String privacyEngineEndpoint = null;

	private PrivacyEngineService privacyEngineService;

	private FitnessMethod fitnessMethod = null;
	private int fitnessPrecision = Integer.MIN_VALUE;

	private double maximumFitness = Double.MIN_VALUE;
	private boolean traceFitnessEvolution = false;
	private String[] initialFlightSequence = null;

	public Optimization(Flight[] flights, Slot[] slots) {
		this.flights = flights;
		this.slots = slots;
	}

	/**
	 * Runs the optimization using the underlying framework, returning the optimized flight list.
	 * @return an optimized mapping from flight to slots
	 */
	public abstract Map<Flight, Slot> run();

	/**
	 * Get the current list of best results. This method will also return intermediate results
	 * of a running optimization.
	 * @return a list of mappings from flights to slots
	 */
	public Map<Flight, Slot>[] getResults(){
		Map<Flight, Slot>[] resultArray = null;

		if(this.results != null) {
			resultArray = results.toArray(Map[]::new);
		}

		return resultArray;
	}

	public void setResults(List<Map<Flight, Slot>> results) {
		this.results = results;
	}



	public Flight[] getFlights() {
		return flights;
	}

	public void setFlights(Flight[] flights) {
		this.flights = flights;
	}

	public Slot[] getSlots() {
		return slots;
	}

	public void setSlots(Slot[] slots) {
		this.slots = slots;
	}

	public UUID getOptId() {
		return optId;
	}

	public void setOptId(UUID optId) {
		this.optId = optId;
	}

	public abstract OptimizationConfiguration getDefaultConfiguration();

	public abstract OptimizationConfiguration getConfiguration();

	public abstract void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException;

	public abstract OptimizationStatistics getStatistics();

	public OptimizationStatus getStatus() {
		return status;
	}

	public void setStatus(OptimizationStatus status) {
		this.status = status;
	}

	public OptimizationMode getMode() {
		return mode;
	}

	public void setMode(OptimizationMode mode) {
		this.mode = mode;
	}

	public FitnessEstimator getFitnessEstimator() {
		return fitnessEstimator;
	}

	public void setFitnessEstimator(FitnessEstimator fitnessEstimator) {
		this.fitnessEstimator = fitnessEstimator;
	}

    public void setPrivacyEngineEndpoint(String privacyEngineEndpoint) {
		this.privacyEngineEndpoint = privacyEngineEndpoint;
	}

	public String getPrivacyEngineEndpoint() {
		return this.privacyEngineEndpoint;
	}

	public void setPrivacyEngineService(PrivacyEngineService privacyEngineService) {
		this.privacyEngineService = privacyEngineService;
	}

	public PrivacyEngineService getPrivacyEngineService() {
		return this.privacyEngineService;
	}

    public double getMaximumFitness() {
		return this.maximumFitness;
    }

	public void setMaximumFitness(double maxFitness) {
		this.maximumFitness = maxFitness;
	}


	public boolean isTraceFitnessEvolution() {
		return traceFitnessEvolution;
	}

	public void setTraceFitnessEvolution(boolean traceFitnessEvolution) {
		this.traceFitnessEvolution = traceFitnessEvolution;
	}


	public FitnessMethod getFitnessMethod() {
		return fitnessMethod;
	}

	public void setFitnessMethod(FitnessMethod fitnessMethod) {
		this.fitnessMethod = fitnessMethod;
	}

	public int getFitnessPrecision() {
		return fitnessPrecision;
	}

	public void setFitnessPrecision(int fitnessPrecision) {
		this.fitnessPrecision = fitnessPrecision;
	}

	public String[] getInitialFlightSequence() {
		return initialFlightSequence;
	}

	public void setInitialFlightSequence(String[] initialFlightSequence) {
		this.initialFlightSequence = initialFlightSequence;
	}
}
