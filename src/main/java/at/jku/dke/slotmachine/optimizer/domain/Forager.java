package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.service.dto.ForagerDTO.FinalistPodiumTypeEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.ForagerDTO.PickEarlyTypeEnum;
/**
 * Is used for LocalSearchPhase (OptaPlannerConfig).
 */
public class Forager {
	
	// values, which are used to define Acceptor (LocalSearchPhase)
	// not all are needed for the different Local Search algorithm types
	private int acceptedCountLimit;
	private FinalistPodiumTypeEnum finalistPodiumType;
	private PickEarlyTypeEnum pickEarlyType;
	
	public Forager(int acceptedCountLimit, FinalistPodiumTypeEnum finalistPodiumType, PickEarlyTypeEnum pickEarlyType) {
		super();
		this.acceptedCountLimit = acceptedCountLimit;
		this.finalistPodiumType = finalistPodiumType;
		this.pickEarlyType = pickEarlyType;
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
