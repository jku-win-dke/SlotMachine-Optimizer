package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.service.dto.LocalSearchPhaseDTO.LocalSearchEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.LocalSearchPhaseDTO.SelectionOrderEnum;

/**
 * Is used for OptaPlannerConfig.
 * Contains information how the local search phase is structured.
 */
public class LocalSearchPhase {
	/**
	 * contains information which local search algorithm type
	 * is used (if applicable)
	 */
	private LocalSearchEnum localSearchEnum;
	
	// contain information about acceptor/forage (used for advanced
	// configuration of local search algorithm type(s))
	private Acceptor acceptor;
	private Forager forager;
	
	// unionMoveSelector
	private SelectionOrderEnum selectionOrder;
	
	/**
	 * contains information about the termination
	 */
	private TerminationOptaPlanner termination;

	public LocalSearchPhase(LocalSearchEnum localSearchEnum, Acceptor acceptor, Forager forager,
			SelectionOrderEnum selectionOrder, TerminationOptaPlanner termination) {
		super();
		this.localSearchEnum = localSearchEnum;
		this.acceptor = acceptor;
		this.forager = forager;
		this.selectionOrder = selectionOrder;
		this.termination = termination;
	}

	public LocalSearchPhase() {
		// TODO constructor with default values
	}

	public LocalSearchEnum getLocalSearchEnum() {
		return localSearchEnum;
	}

	public void setLocalSearchEnum(LocalSearchEnum localSearchEnum) {
		this.localSearchEnum = localSearchEnum;
	}

	public Acceptor getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(Acceptor acceptor) {
		this.acceptor = acceptor;
	}

	public Forager getForager() {
		return forager;
	}

	public void setForager(Forager forager) {
		this.forager = forager;
	}

	public SelectionOrderEnum getSelectionOrder() {
		return selectionOrder;
	}

	public void setSelectionOrder(SelectionOrderEnum selectionOrder) {
		this.selectionOrder = selectionOrder;
	}

	public TerminationOptaPlanner getTermination() {
		return termination;
	}

	public void setTermination(TerminationOptaPlanner termination) {
		this.termination = termination;
	}
}
