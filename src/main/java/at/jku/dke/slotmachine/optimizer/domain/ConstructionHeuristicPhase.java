package at.jku.dke.slotmachine.optimizer.domain;

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
		// TODO constructor with default values
	}

	public ConstructionEnum getConstructionEnum() {
		return constructionEnum;
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
