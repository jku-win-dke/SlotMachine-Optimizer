package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;

import at.jku.dke.slotmachine.optimizer.service.dto.ConstructionHeuristicPhaseDTO.ConstructionEnum;

/**
 * Is used for OptaPlannerConfig.
 * Contains information how the construction heuristic phase is structured.
 */
public class ConstructionHeuristicPhase {

	// class for Construction Heuristic Phase of
	// <solver></solver> with the type of construction
	// heuristic and (optionally) additional termination
	// aspects (usually construction heuristic terminate
	// automatically, as they find a first initial
	// solution)
	
	/**
	 * contains information which construction heuristic
	 * type is used
	 */
	private ConstructionEnum constructionEnum;
	
	/**
	 * contains information about the termination (if applicable)
	 */
	private TerminationOptaPlanner termination;
	
	public ConstructionHeuristicPhase(ConstructionEnum constructionEnum, TerminationOptaPlanner termination) {
		super();
		this.constructionEnum = constructionEnum;
		this.termination = termination;
	}
	
	public ConstructionHeuristicPhase() {
		// default values, should usually not be used
		this.constructionEnum = ConstructionEnum.FIRSTFIT;
		this.termination = new TerminationOptaPlanner();
		
	}

	public ConstructionEnum getConstructionEnum() {
		return constructionEnum;
	}
	public ConstructionHeuristicType getConstructionHeuristicType() {
		if (constructionEnum == null) {
			return ConstructionHeuristicType.FIRST_FIT;
		}
		switch (constructionEnum) {
			case FIRSTFIT: return ConstructionHeuristicType.FIRST_FIT;
			case FIRSTFITDECREASING: return ConstructionHeuristicType.FIRST_FIT_DECREASING;
			case WEAKESTFIT: return ConstructionHeuristicType.WEAKEST_FIT;
			case WEAKESTFITDECREASING: return ConstructionHeuristicType.WEAKEST_FIT_DECREASING;
			case STRONGESTFIT: return ConstructionHeuristicType.STRONGEST_FIT;
			case STRONGESTFITDECREASING: return ConstructionHeuristicType.STRONGEST_FIT_DECREASING;
			case CHEAPESTINSERTION: return ConstructionHeuristicType.CHEAPEST_INSERTION;
			default: return ConstructionHeuristicType.FIRST_FIT;
		}
	}
	public void setConstructionEnum(ConstructionEnum constructionEnum) {
		this.constructionEnum = constructionEnum;
	}
	public TerminationOptaPlanner getTermination() {
		return termination;
	}
	public void setTermination(TerminationOptaPlanner termination) {
		this.termination = termination;
	}
}
