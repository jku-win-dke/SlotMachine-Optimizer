package at.jku.dke.slotmachine.optimizer.service.dto;

/**
 * Is used for LocalSearchPhaseDTO (OptaPlannerConfigDTO).
 */
public class ForagerDTO {
	
	// values, which are used to define Acceptor (LocalSearchPhase)
	// not all are needed for the different Local Search algorithm types
	private int acceptedCountLimit;
	private FinalistPodiumTypeEnum finalistPodiumType;
	private PickEarlyTypeEnum pickEarlyType;

	/**
	 * Possible values for finalist podium type.
	 */
	public enum FinalistPodiumTypeEnum {
		STRATEGICOSCILLATION, HIGHESTSCORE, 
		STRATEGICOSCILLATIONBYLEVEL, 
		STRATEGICOSCILLATIONBYLEVELONBESTSCORE
	}
	/**
	 * Possible values for pick early type.
	 */
	public enum PickEarlyTypeEnum {
		FIRSTLASTSTEPSCOREIMPROVING
	}
	
	public int getAcceptedCountLimit() {
		return acceptedCountLimit;
	}
	public void setAcceptedCountLimit(int acceptedCountLimit) {
		this.acceptedCountLimit = acceptedCountLimit;
	}
	public FinalistPodiumTypeEnum getFinalistPodiumType() {
		return finalistPodiumType;
	}
	public void setFinalistPodiumType(FinalistPodiumTypeEnum finalistPodiumType) {
		this.finalistPodiumType = finalistPodiumType;
	}
	public PickEarlyTypeEnum getPickEarlyType() {
		return pickEarlyType;
	}
	public void setPickEarlyType(PickEarlyTypeEnum pickEarlyType) {
		this.pickEarlyType = pickEarlyType;
	}
}
