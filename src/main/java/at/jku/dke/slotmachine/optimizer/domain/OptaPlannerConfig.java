package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.service.dto.OptaPlannerConfigDTO.EnvironmentMode;
import at.jku.dke.slotmachine.optimizer.service.dto.OptaPlannerConfigDTO.MoveThreadCount;

public class OptaPlannerConfig {

	// based on OptaPlannerConfigDTO
	
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
	private TerminationOptaPlanner termination;
	
	/**
	 * Sets the construction heuristic phase for OptaPlanner.
	 */
	private ConstructionHeuristicPhase constructionHeuristic;
	
	/**
	 * Sets the local search phase for OptaPlanner.
	 */
	private LocalSearchPhase localSearch;
	
	public OptaPlannerConfig(MoveThreadCount moveThreadCount, EnvironmentMode environmentMode, TerminationOptaPlanner termination,
			ConstructionHeuristicPhase constructionHeuristic, LocalSearchPhase localSearch) {
		super();
		this.moveThreadCount = moveThreadCount;
		this.environmentMode = environmentMode;
		this.termination = termination;
		this.constructionHeuristic = constructionHeuristic;
		this.localSearch = localSearch;
	}
	
	public OptaPlannerConfig() {
		// TODO constructor with default values
	}

	public MoveThreadCount getMoveThreadCount() {
		return moveThreadCount;
	}
	
	public String getMoveThreadCountString() {
		if (moveThreadCount == null) {
			return "NONE";
		}
		switch (moveThreadCount) {
			case AUTO: return "AUTO";
			case NONE: return "NONE";
			case ONE: return "1";
			case TWO: return "2";
			case THREE: return "3";
			case FOUR: return "4";
			case FIVE: return "5";
			case SIX: return "6";
			case SEVEN: return "7";
			case EIGHT: return "8";
			default: return "NONE";
		}
	}

	public void setMoveThreadCount(MoveThreadCount moveThreadCount) {
		this.moveThreadCount = moveThreadCount;
	}

	public EnvironmentMode getEnvironmentMode() {
		return environmentMode;
	}
	
	public org.optaplanner.core.config.solver.EnvironmentMode getEnvironmentModeSolver(){
		if (environmentMode == null) {
			return org.optaplanner.core.config.solver.EnvironmentMode.REPRODUCIBLE;
		}
		switch (environmentMode) {
			case FULL_ASSERT: return org.optaplanner.core.config.solver.EnvironmentMode.FULL_ASSERT;
			case NON_INTRUSIVE_FULL_ASSERT: return org.optaplanner.core.config.solver.EnvironmentMode.NON_INTRUSIVE_FULL_ASSERT;
			case FAST_ASSERT: return org.optaplanner.core.config.solver.EnvironmentMode.FAST_ASSERT;
			case REPRODUCIBLE: return org.optaplanner.core.config.solver.EnvironmentMode.REPRODUCIBLE;
			case NON_REPRODUCIBLE: return org.optaplanner.core.config.solver.EnvironmentMode.NON_REPRODUCIBLE;
			default: return org.optaplanner.core.config.solver.EnvironmentMode.REPRODUCIBLE;
		}
	}

	public void setEnvironmentMode(EnvironmentMode environmentMode) {
		this.environmentMode = environmentMode;
	}

	public TerminationOptaPlanner getTermination() {
		return termination;
	}

	public void setTermination(TerminationOptaPlanner termination) {
		this.termination = termination;
	}

	public ConstructionHeuristicPhase getConstructionHeuristic() {
		return constructionHeuristic;
	}

	public void setConstructionHeuristic(ConstructionHeuristicPhase constructionHeuristic) {
		this.constructionHeuristic = constructionHeuristic;
	}

	public LocalSearchPhase getLocalSearch() {
		return localSearch;
	}

	public void setLocalSearch(LocalSearchPhase localSearch) {
		this.localSearch = localSearch;
	}
}
