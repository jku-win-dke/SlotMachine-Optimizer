package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import at.jku.dke.slotmachine.optimizer.service.dto.TerminationOptaPlannerDTO.TerminationComposition;
import at.jku.dke.slotmachine.optimizer.service.dto.TerminationOptaPlannerDTO.TerminationEnum;

/**
 * Is used for OptaPlannerConfig.
 * Contains information about which termination methods are used.
 */
public class TerminationOptaPlanner {

 	// class for Termination part of <solver></solver>
 	// with two TerminationEnums with two Values

	/**
	 * contains information which termination type is used
	 */
	private TerminationEnum termination1;
	/**
	 * contains information which termination type is used
	 */
	private TerminationEnum termination2;
	/**
	 * contains information how the connection of both termination
	 * types is
	 */
	private TerminationComposition termComp;
	
	// termination value (value, score, boolean) are for
	// the termination types, depending if the type
	// needs a numerical value (...Value), score (..Score)
	// or boolean value (...Boolean); the other values should 
	// be ignored for that termination type
	/**
	 * numerical value for termination type 1 (if applicable)
	 */
	private double terminationValue1;
	/**
	 * numerical value for termination type 2 (if applicable)
	 */
	private double terminationValue2;
	/**
	 * score value for termination type 1 (if applicable)
	 * (especially: hard-Score, soft-Score)
	 */
	private HardSoftScore terminationScore1;
	/**
	 * score value for termination type 2 (if applicable)
	 * (especially: hard-Score, soft-Score)
	 */
	private HardSoftScore terminationScore2;
	/**
	 * boolean value for termination type 1 (if applicable)
	 */
	private boolean terminationBoolean1;
	/**
	 * boolean value for termination type 2 (if applicable)
	 */
	private boolean terminationBoolean2;
	
	public TerminationOptaPlanner(TerminationEnum termination1, TerminationEnum termination2, TerminationComposition termComp,
			double terminationValue1, double terminationValue2, HardSoftScore terminationScore1,
			HardSoftScore terminationScore2, boolean terminationBoolean1, boolean terminationBoolean2) {
		super();
		this.termination1 = termination1;
		this.termination2 = termination2;
		this.termComp = termComp;
		this.terminationValue1 = terminationValue1;
		this.terminationValue2 = terminationValue2;
		this.terminationScore1 = terminationScore1;
		this.terminationScore2 = terminationScore2;
		this.terminationBoolean1 = terminationBoolean1;
		this.terminationBoolean2 = terminationBoolean2;
	}
	
	public TerminationOptaPlanner() {
		// constructor with default values
		this.termination1 = TerminationEnum.UNIMPROVEDSECONDSSPENTLIMIT;
		this.termination2 = null;
		this.termComp = null;
		this.terminationValue1 = 10;
		this.terminationValue2 = 0;
		this.terminationScore1 = null;
		this.terminationScore2 = null;
		this.terminationBoolean1 = false;
		this.terminationBoolean2 = false;
	}
	
	public TerminationEnum getTermination1() {
		return termination1;
	}
	public void setTermination1(TerminationEnum termination1) {
		this.termination1 = termination1;
	}
	public TerminationEnum getTermination2() {
		return termination2;
	}
	public void setTermination2(TerminationEnum termination2) {
		this.termination2 = termination2;
	}
	public TerminationComposition getTermComp() {
		return termComp;
	}
	public void setTermComp(TerminationComposition termComp) {
		this.termComp = termComp;
	}
	public double getTerminationValue1() {
		return terminationValue1;
	}
	public void setTerminationValue1(double terminationValue1) {
		this.terminationValue1 = terminationValue1;
	}
	public double getTerminationValue2() {
		return terminationValue2;
	}
	public void setTerminationValue2(double terminationValue2) {
		this.terminationValue2 = terminationValue2;
	}
	public HardSoftScore getTerminationScore1() {
		return terminationScore1;
	}
	public void setTerminationScore1(HardSoftScore terminationScore1) {
		this.terminationScore1 = terminationScore1;
	}
	public HardSoftScore getTerminationScore2() {
		return terminationScore2;
	}
	public void setTerminationScore2(HardSoftScore terminationScore2) {
		this.terminationScore2 = terminationScore2;
	}
	public boolean isTerminationBoolean1() {
		return terminationBoolean1;
	}
	public void setTerminationBoolean1(boolean terminationBoolean1) {
		this.terminationBoolean1 = terminationBoolean1;
	}
	public boolean isTerminationBoolean2() {
		return terminationBoolean2;
	}
	public void setTerminationBoolean2(boolean terminationBoolean2) {
		this.terminationBoolean2 = terminationBoolean2;
	}
}
