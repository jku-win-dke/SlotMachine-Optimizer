package at.jku.dke.slotmachine.optimizer.service.dto;

/**
 * Is used for OptaPlannerConfigDTO.
 * Contains information how the construction heuristic phase is structured.
 */
public class ConstructionHeuristicPhaseDTO {

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
	private TerminationDTO termination;
	
	/**
	 * Possible values for the construction heuristic type.
	 */
	public enum ConstructionEnum {
		FIRSTFIT, FIRSTFITDECREASING,
		WEAKESTFIT, WEAKESTFITDECREASING,
		STRONGESTFIT, STRONGESTFITDECREASING,
		CHEAPESTFIT
	}
	
	public ConstructionEnum getConstructionEnum() {
		return constructionEnum;
	}
	public void setConstructionEnum(ConstructionEnum constructionEnum) {
		this.constructionEnum = constructionEnum;
	}
	public TerminationDTO getTermination() {
		return termination;
	}
	public void setTermination(TerminationDTO termination) {
		this.termination = termination;
	}
}
