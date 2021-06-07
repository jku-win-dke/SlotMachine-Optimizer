package at.jku.dke.slotmachine.optimizer.service.dto;

public class JeneticConfigDTO {

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
	
	public enum Alterer {
		PARTIALLYMATCHEDCROSSOVER, SWAPMUTATOR
		/*//not implemented currently, other possible Alterers are:
		, COMBINEALTERER, GAUSSIANMUTATOR, INTERMEDIATECROSSOVER, 
		LINECROSSOVER, MEANALTERER, MULTIPOINTCROSSOVER, PARTIALALTERER,
		SINGLEPOINTCROSSOVER, UNIFORMCROSSOVER
		*/
	}
	
	public enum Selector {
		TOURNAMENTSELECTOR, BOLTZMANNSELECTOR, ELITESELECTOR, 
		EXPONENTIALRANKSELECTOR, LINEARRANKSELECTOR, 
		ROULETTEWHEELSELECTOR, STOCHASTICUNIVERSALSELECTOR,
		TRUNCATIONSELECTOR
		/*//not implemented currently, other possible Selectors are:
		, MONTECARLOSELECTOR, ELITESELECTOR, PROBABILITYSELECTOR
		*/
	}
	
	public enum Termination {
		WORSTFITNESS, BYFITNESSTHRESHOLD, BYSTEADYFITNESS, BYFIXEDGENERATION,
		BYEXECUTIONTIME, BYPOPULATIONCONVERGENCE, BYFITNESSCONVERGENCE
		/*//not implemented currently, other possible Selectors are:
		, BYGENECONVERGENCE
		*/
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

	public double[] getOffspringSelectorAttributes() {
		return offspringSelectorAttributes;
	}

	public void setOffspringSelectorAttributes(double[] selectorAttributes) {
		this.offspringSelectorAttributes = selectorAttributes;
	}

	public Selector getOffspringSelector() {
		return offspringSelector;
	}

	public void setOffspringSelector(Selector selector) {
		this.offspringSelector = selector;
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
