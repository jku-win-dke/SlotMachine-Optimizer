package at.jku.dke.slotmachine.optimizer.service.dto;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

/**
 * Is used for OptaPlannerConfigDTO.
 * Contains information about which termination methods are used.
 */
public class TerminationDTO {

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
	
	/**
	 * Possible values for termination type:<br>
	 * 	- <code>MILLISECONDSSPENTLIMIT, SECONDSSPENTLIMIT, MINUTESSPENTLIMIT,
	 *	HOURSSPENTLIMIT, DAYSSPENTLIMIT</code>: terminates when the defined
	 *	amount of time has passed; requires numerical value<br>
	 *	- <code>UNIMPROVEDMILLISECONDSSPENTLIMIT, UNIMPROVEDSECONDSSPENTLIMIT, 
	 *	UNIMPROVEDMINUTESSPENTLIMIT, UNIMPROVEDHOURSSPENTLIMIT, 
	 *	UNIMPROVEDDAYSSPENTLIMIT</code>: terminates when the defined amount of
	 *	time has passed without improvements (of the solution); requires
	 *	numerical value<br>
	 *	- <code>BESTSCORELIMIT</code>: terminates when the defined score has 
	 *	been reached; requires Score value<br>
	 *	- <code>BESTSCOREFEASIBLE</code>: terminates when a feasible solution 
	 *	has been found; requires boolean value<br>
	 *	- <code>STEPCOUNTTERMINATION, UNIMPROVEDSTEPCOUNTTERMINATION</code>: 
	 *	terminates after the defined amount of steps have been reached (or 
	 *	after the defined amount of steps have been reached without an 
	 *	improvement); requires numerical value<br>
	 *	- <code>SCORECALCULATIONCOUNTLIMIT</code>: terminates after a defined
	 *	amount of score calculations have been done; requires numerical value
	 */
	public enum TerminationEnum {
		MILLISECONDSSPENTLIMIT, SECONDSSPENTLIMIT, MINUTESSPENTLIMIT,
		HOURSSPENTLIMIT, DAYSSPENTLIMIT,
		UNIMPROVEDMILLISECONDSSPENTLIMIT, UNIMPROVEDSECONDSSPENTLIMIT, 
		UNIMPROVEDMINUTESSPENTLIMIT, UNIMPROVEDHOURSSPENTLIMIT, 
		UNIMPROVEDDAYSSPENTLIMIT,	
		BESTSCORELIMIT,
		BESTSCOREFEASIBLE,
		STEPCOUNTTERMINATION, UNIMPROVEDSTEPCOUNTTERMINATION,
		SCORECALCULATIONCOUNTLIMIT
		/* UNIMPROVEDSCOREDIFFERENCETHRESHOLD, //not implemented currently */
	}
	/**
	 * Possible values to combine two termination types (AND, OR).
	 */
	public enum TerminationComposition {
		AND, OR
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
