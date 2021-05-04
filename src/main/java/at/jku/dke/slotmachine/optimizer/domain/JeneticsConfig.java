package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.service.dto.JeneticConfigDTO.*;

public class JeneticsConfig {

	/**
	 * contains information about which alterer(s) are used (crossover, mutator)
	 */
	private Alterer[] alterer;
	/**
	 * contains additional information about the attributes of the alterer(s),
	 * with the possibility to contain more than one attribute per alterer
	 * 
	 * (e.g. alterer[0] = PARTIALLYMATCHEDCROSSOVER; alterAttributes[0][0] = 0.35;)
	 */
	private double[][] altererAttributes;
	/**
	 * contains information about which offspring selector is used
	 */
	private Selector offspringSelector;
	/**
	 * contains attributes from the offspring selector with the possibility to contain
	 * more than one attribute
	 */
	private double[] offspringSelectorAttributes;
	/**
	 * contains information about which survivor selector is used
	 */
	private Selector survivorSelector;
	/**
	 * contains attributes from the survivor selector with the possibility to contain
	 * more than one attribute
	 */
	private double[] survivorSelectorAttributes;
	/**
	 * which fraction of the offspring is used for future generations
	 */
	private double offspringFraction;
	/**
	 * how old each phenotype is allowed to be
	 */
	private int maximalPhenotypeAge;
	/**
	 * population size for the generations
	 */
	private int populationSize;
	/**
	 * contains information about which termination methods are used, maximum supported
	 * size is 3
	 */
	private Termination[] termination;
	/**
	 * contains attributes from the termination method with the possibility to contain
	 * more than one attribute
	 */
	private double[][] terminationAttributes;
	
	public JeneticsConfig(Alterer[] alterer, double[][] altererAttributes, Selector offspringSelector,
			double[] offspringSelectorAttributes, Selector survivorSelector, double[] survivorSelectorAttributes,
			double offspringFraction, int maximalPhenotypeAge, int populationSize, Termination[] termination,
			double[][] terminationAttributes) {
		super();
		this.alterer = alterer;
		this.altererAttributes = altererAttributes;
		this.offspringSelector = offspringSelector;
		this.offspringSelectorAttributes = offspringSelectorAttributes;
		this.survivorSelector = survivorSelector;
		this.survivorSelectorAttributes = survivorSelectorAttributes;
		this.offspringFraction = offspringFraction;
		this.maximalPhenotypeAge = maximalPhenotypeAge;
		this.populationSize = populationSize;
		this.termination = termination;
		this.terminationAttributes = terminationAttributes;
	}

	public JeneticsConfig() {
		super();
		this.alterer = new Alterer[2];
		this.alterer[0] = Alterer.SWAPMUTATOR;
		this.alterer[1] = Alterer.PARTIALLYMATCHEDCROSSOVER;
		this.altererAttributes = new double[2][2];
		this.altererAttributes[0][0] = 0.2;
		this.altererAttributes[1][0] = 0.35;
		
		// TODO constructor with default values
		
	}

	public Alterer[] getAlterer() {
		return alterer;
	}

	public void setAlterer(Alterer[] alterer) {
		this.alterer = alterer;
	}

	public double[][] getAltererAttributes() {
		return altererAttributes;
	}

	public void setAltererAttributes(double[][] altererAttributes) {
		this.altererAttributes = altererAttributes;
	}

	public Selector getOffspringSelector() {
		return offspringSelector;
	}

	public void setOffspringSelector(Selector offspringSelector) {
		this.offspringSelector = offspringSelector;
	}

	public double[] getOffspringSelectorAttributes() {
		return offspringSelectorAttributes;
	}

	public void setOffspringSelectorAttributes(double[] offspringSelectorAttributes) {
		this.offspringSelectorAttributes = offspringSelectorAttributes;
	}

	public Selector getSurvivorSelector() {
		return survivorSelector;
	}

	public void setSurvivorSelector(Selector survivorSelector) {
		this.survivorSelector = survivorSelector;
	}

	public double[] getSurvivorSelectorAttributes() {
		return survivorSelectorAttributes;
	}

	public void setSurvivorSelectorAttributes(double[] survivorSelectorAttributes) {
		this.survivorSelectorAttributes = survivorSelectorAttributes;
	}

	public double getOffspringFraction() {
		return offspringFraction;
	}

	public void setOffspringFraction(double offspringFraction) {
		this.offspringFraction = offspringFraction;
	}

	public int getMaximalPhenotypeAge() {
		return maximalPhenotypeAge;
	}

	public void setMaximalPhenotypeAge(int maximalPhenotypeAge) {
		this.maximalPhenotypeAge = maximalPhenotypeAge;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public Termination[] getTermination() {
		return termination;
	}

	public void setTermination(Termination[] termination) {
		this.termination = termination;
	}

	public double[][] getTerminationAttributes() {
		return terminationAttributes;
	}

	public void setTerminationAttributes(double[][] terminationAttributes) {
		this.terminationAttributes = terminationAttributes;
	}

}
