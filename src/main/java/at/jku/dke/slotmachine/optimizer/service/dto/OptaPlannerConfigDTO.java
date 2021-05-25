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
	
	/**
	 * Sets the termination method/type for OptaPlanner.
	 */
	private TerminationOptaPlannerDTO termination;
	
	/**
	 * Sets the construction heuristic phase for OptaPlanner.
	 */
	private ConstructionHeuristicPhaseDTO constructionHeuristic;
	
	/**
	 * Sets the local search phase for OptaPlanner.
	 */
	private LocalSearchPhaseDTO localSearch;
	
	/**
	 * Possible values for move thread count.
	 */
	public enum MoveThreadCount {
		NONE, AUTO, ONE, TWO, THREE, FOUR, FIVE,
		SIX, SEVEN, EIGHT //more possible
	}
	/**
	 * Possible values for environment mode.
	 */
	public enum EnvironmentMode {
		FULL_ASSERT, NON_INTRUSIVE_FULL_ASSERT, 
		FAST_ASSERT, REPRODUCIBLE, NON_REPRODUCIBLE
	}	
	
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

	public TerminationOptaPlannerDTO getTermination() {
		return termination;
	}

	public void setTermination(TerminationOptaPlannerDTO termination) {
		this.termination = termination;
	}

	public ConstructionHeuristicPhaseDTO getConstructionHeuristic() {
		return constructionHeuristic;
	}

	public void setConstructionHeuristic(ConstructionHeuristicPhaseDTO constructionHeuristic) {
		this.constructionHeuristic = constructionHeuristic;
	}

	public LocalSearchPhaseDTO getLocalSearch() {
		return localSearch;
	}

	public void setLocalSearch(LocalSearchPhaseDTO localSearch) {
		this.localSearch = localSearch;
	}
}
