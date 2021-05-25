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

	public void setMoveThreadCount(MoveThreadCount moveThreadCount) {
		this.moveThreadCount = moveThreadCount;
	}

	public EnvironmentMode getEnvironmentMode() {
		return environmentMode;
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
