package at.jku.dke.slotmachine.optimizer.service.dto;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

/**
 * Is used for LocalSearchPhaseDTO (OptaPlannerConfigDTO).
 */
public class AcceptorDTO {

	// values, which are used to define Acceptor (LocalSearchPhase)
	// not all are needed for the different Local Search algorithm types
	private AcceptorTypeEnum acceptorType;
	private int entityTabuSize;
	private double entityTabuRatio;
	private int valueTabuSize;
	private double valueTabuRatio;
	private int moveTabuSize;
	private int undoMoveTabuSize;
	private HardSoftScore simulAnnealStartTemp; //simulatedAnnealingStartingTemperature
	private int lateAcceptanceSize;
	private HardSoftScore grDelInitWaterLevel; //greatDelugeInitialWaterLevel
	private double grDelWaterLevelIncrRatio; //greatDelugeWaterLevelIncrementRatio
	private double grDelWaterLevelIncrScore; //greatDelugeWaterLevelIncrementScore
	private int stepCountHillClimbSize; //stepCountingHillClimbingSize
	
	/**
	 * Possible values for acceptor type.
	 */
	public enum AcceptorTypeEnum {
		HILLCLIMBING
	}

	
	public AcceptorTypeEnum getAcceptorType() {
		return acceptorType;
	}
	public void setAcceptorType(AcceptorTypeEnum acceptorType) {
		this.acceptorType = acceptorType;
	}
	public int getEntityTabuSize() {
		return entityTabuSize;
	}
	public void setEntityTabuSize(int entityTabuSize) {
		this.entityTabuSize = entityTabuSize;
	}
	public double getEntityTabuRatio() {
		return entityTabuRatio;
	}
	public void setEntityTabuRatio(double entityTabuRatio) {
		this.entityTabuRatio = entityTabuRatio;
	}
	public int getValueTabuSize() {
		return valueTabuSize;
	}
	public void setValueTabuSize(int valueTabuSize) {
		this.valueTabuSize = valueTabuSize;
	}
	public double getValueTabuRatio() {
		return valueTabuRatio;
	}
	public void setValueTabuRatio(double valueTabuRatio) {
		this.valueTabuRatio = valueTabuRatio;
	}
	public int getMoveTabuSize() {
		return moveTabuSize;
	}
	public void setMoveTabuSize(int moveTabuSize) {
		this.moveTabuSize = moveTabuSize;
	}
	public int getUndoMoveTabuSize() {
		return undoMoveTabuSize;
	}
	public void setUndoMoveTabuSize(int undoMoveTabuSize) {
		this.undoMoveTabuSize = undoMoveTabuSize;
	}
	public HardSoftScore getSimulAnnealStartTemp() {
		return simulAnnealStartTemp;
	}
	public void setSimulAnnealStartTemp(HardSoftScore simulAnnealStartTemp) {
		this.simulAnnealStartTemp = simulAnnealStartTemp;
	}
	public int getLateAcceptanceSize() {
		return lateAcceptanceSize;
	}
	public void setLateAcceptanceSize(int lateAcceptanceSize) {
		this.lateAcceptanceSize = lateAcceptanceSize;
	}
	public HardSoftScore getGrDelInitWaterLevel() {
		return grDelInitWaterLevel;
	}
	public void setGrDelInitWaterLevel(HardSoftScore grDelInitWaterLevel) {
		this.grDelInitWaterLevel = grDelInitWaterLevel;
	}
	public double getGrDelWaterLevelIncrRatio() {
		return grDelWaterLevelIncrRatio;
	}
	public void setGrDelWaterLevelIncrRatio(double grDelWaterLevelIncrRatio) {
		this.grDelWaterLevelIncrRatio = grDelWaterLevelIncrRatio;
	}
	public double getGrDelWaterLevelIncrScore() {
		return grDelWaterLevelIncrScore;
	}
	public void setGrDelWaterLevelIncrScore(double grDelWaterLevelIncrScore) {
		this.grDelWaterLevelIncrScore = grDelWaterLevelIncrScore;
	}
	public int getStepCountHillClimbSize() {
		return stepCountHillClimbSize;
	}
	public void setStepCountHillClimbSize(int stepCountHillClimbSize) {
		this.stepCountHillClimbSize = stepCountHillClimbSize;
	}		
}
