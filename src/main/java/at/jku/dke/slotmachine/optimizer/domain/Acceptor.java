package at.jku.dke.slotmachine.optimizer.domain;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import at.jku.dke.slotmachine.optimizer.service.dto.AcceptorDTO.AcceptorTypeEnum;
/**
 * Is used for LocalSearchPhase (OptaPlannerConfig).
 */
public class Acceptor {

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
	private HardSoftScore grDelWaterLevelIncrScore; //greatDelugeWaterLevelIncrementScore
	private int stepCountHillClimbSize; //stepCountingHillClimbingSize
	
	public Acceptor(AcceptorTypeEnum acceptorType, int entityTabuSize, double entityTabuRatio, int valueTabuSize,
			double valueTabuRatio, int moveTabuSize, int undoMoveTabuSize, HardSoftScore simulAnnealStartTemp,
			int lateAcceptanceSize, HardSoftScore grDelInitWaterLevel, double grDelWaterLevelIncrRatio,
			HardSoftScore grDelWaterLevelIncrScore, int stepCountHillClimbSize) {
		super();
		this.acceptorType = acceptorType;
		this.entityTabuSize = entityTabuSize;
		this.entityTabuRatio = entityTabuRatio;
		this.valueTabuSize = valueTabuSize;
		this.valueTabuRatio = valueTabuRatio;
		this.moveTabuSize = moveTabuSize;
		this.undoMoveTabuSize = undoMoveTabuSize;
		this.simulAnnealStartTemp = simulAnnealStartTemp;
		this.lateAcceptanceSize = lateAcceptanceSize;
		this.grDelInitWaterLevel = grDelInitWaterLevel;
		this.grDelWaterLevelIncrRatio = grDelWaterLevelIncrRatio;
		this.grDelWaterLevelIncrScore = grDelWaterLevelIncrScore;
		this.stepCountHillClimbSize = stepCountHillClimbSize;
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
	/**
	 * Returns simulated annealing starting temperature as a string
	 * in the correct format for HardSoftScore.
	 */
	public String getSimulAnnealStartTempString() {
		if (simulAnnealStartTemp == null) {
			return "0hard/100soft";
		}
		String scoreString;
		scoreString = simulAnnealStartTemp.getHardScore() + "";
		scoreString = scoreString + "hard/";
		scoreString = scoreString + simulAnnealStartTemp.getSoftScore();
		scoreString = scoreString + "soft";
		return scoreString;
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
	/**
	 * Returns great deluge initial water level as a string
	 * in the correct format for HardSoftScore.
	 */
	public String getDrDelInitWaterLevelString() {
		if (grDelInitWaterLevel == null) {
			return "0hard/100soft";
		}
		String scoreString;
		scoreString = grDelInitWaterLevel.getHardScore() + "";
		scoreString = scoreString + "hard/";
		scoreString = scoreString + grDelInitWaterLevel.getSoftScore();
		scoreString = scoreString + "soft";
		return scoreString;
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
	public HardSoftScore getGrDelWaterLevelIncrScore() {
		return grDelWaterLevelIncrScore;
	}
	public void setGrDelWaterLevelIncrScore(HardSoftScore grDelWaterLevelIncrScore) {
		this.grDelWaterLevelIncrScore = grDelWaterLevelIncrScore;
	}
	public int getStepCountHillClimbSize() {
		return stepCountHillClimbSize;
	}
	public void setStepCountHillClimbSize(int stepCountHillClimbSize) {
		this.stepCountHillClimbSize = stepCountHillClimbSize;
	}		
}
