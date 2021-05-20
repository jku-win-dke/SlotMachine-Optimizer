package at.jku.dke.slotmachine.optimizer.service.dto;

public class OptaPlannerConfigDTO {

	/**
	 * Related to {@code<moveThreadCount>AUTO</moveThreadCount>}, with
	 * AUTO (OptaPlanner decides how many threads run in parallel), 
	 * NONE (use single threaded code), ONE (use single threaded code),
	 * TWO (2 move threads run in parallel), THREE (3 move threads 
	 * run in parallel), ..., EIGHT (8 move threads run in parallel)
	 * as the possible value.
	 * <br>
	 * It is important to note, that a move thread count of 4 needs
	 * almost 5 CPU cores, as the fifth core is used not by the move
	 * threads but by the solver itself. If more than available CPU cores
	 * are selected the score calculation speed will slow down.
	 */
	private MoveThreadCount moveThreadCount;
	
	/**
	 * Sets the environment mode for OptaPlanner.
	 */
	private EnvironmentMode environmentMode;
	
	/*private Termination termination;
	
	private ConstructionHeuristicPhase constructionHeuristic;
	
	private LocalSearchPhase localSearch;*/
	
	// Enums, Classes
	public enum MoveThreadCount {
		NONE, AUTO, ONE, TWO, THREE, FOUR, FIVE,
		SIX, SEVEN, EIGHT //more possible
	}

	public enum EnvironmentMode {
		FULL_ASSERT, NON_INTRUSIVE_FULL_ASSERT, 
		FAST_ASSERT, REPRODUCIBLE, NON_REPRODUCIBLE
	}
	
	/*public class Termination {
	 	// inner class for Termination part of <solver></solver>
	 	// with two TerminationEnums with two Values

		private TerminationEnum termination1;
		private TerminationEnum termination2;
		private TerminationComposition termComp
		
		public enum TerminationEnum {
			MILLISECONDSSPENTLIMIT, SECONDSSPENTLIMIT, MINUTESSPENTLIMIT,
			HOURSSPENTLIMIT, DAYSSPENTLIMIT,
			UNIMPROVEDMILLISECONDSSPENTLIMIT, UNIMPROVEDSECONDSSPENTLIMIT, 
			UNIMPROVEDMINUTESSPENTLIMIT, UNIMPROVEDHOURSSPENTLIMIT, 
			UNIMPROVEDDAYSSPENTLIMIT,
			UNIMPROVEDSCOREDIFFERENCETHRESHOLD,
			BESTSCORELIMIT,
			BESTSCOREFEASIBLE,
			STEPCOUNTTERMINATION, UNIMPROVEDSTEPCOUNTTERMINATION,
			SCORECALCULATIONCOUNTLIMIT
		}
		public enum TerminationComposition {
			AND, OR
		}
	}
	
	public class ConstructionHeuristicPhase {
		//
		//
		//
		//
		//
	}
	*/

	public MoveThreadCount getMoveThreadCount() {
		return moveThreadCount;
	}


	public void setMoveThreadCount(MoveThreadCount moveThreadCount) {
		this.moveThreadCount = moveThreadCount;
	}


	public EnvironmentMode getEnvironmentMode() {
		return environmentMode;
	}


	public void setEnvironmentMode(EnvironmentMode environmentMode) {
		this.environmentMode = environmentMode;
	}
	
	
}
