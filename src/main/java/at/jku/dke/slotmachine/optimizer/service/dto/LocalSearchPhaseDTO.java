package at.jku.dke.slotmachine.optimizer.service.dto;

/**
 * Is used for OptaPlannerConfigDTO.
 * Contains information how the local search phase is structured.
 */
public class LocalSearchPhaseDTO {
	/**
	 * contains information which local search algorithm type
	 * is used (if applicable)
	 */
	private LocalSearchEnum localSearchEnum;
	
	// contain information about acceptor/forage (used for advanced
	// configuration of local search algorithm type(s))
	private AcceptorDTO acceptor;
	private ForagerDTO forager;
	
	// unionMoveSelector
	private SelectionOrderEnum selectionOrder;
	
	/**
	 * contains information about the termination
	 */
	private TerminationOptaPlannerDTO termination;
	
	/**
	 * Possible values for Local Search algorithm types.
	 */
	public enum LocalSearchEnum {
		HILLCLIMBING, TABUSEARCH, SIMULATEDANNEALING, 
		LATEACCEPTANCE, GREATDELUGE, VARIABLENEIGHBORHOODDESCENT
	}
	/**
	 * Possible values for Selection Order (unionMoveSelector).
	 */
	public enum SelectionOrderEnum {
		ORIGINAL
	}


	public LocalSearchEnum getLocalSearchEnum() {
		return localSearchEnum;
	}

	public void setLocalSearchEnum(LocalSearchEnum localSearchEnum) {
		this.localSearchEnum = localSearchEnum;
	}

	public AcceptorDTO getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(AcceptorDTO acceptor) {
		this.acceptor = acceptor;
	}

	public ForagerDTO getForager() {
		return forager;
	}

	public void setForager(ForagerDTO forager) {
		this.forager = forager;
	}

	public SelectionOrderEnum getSelectionOrder() {
		return selectionOrder;
	}

	public void setSelectionOrder(SelectionOrderEnum selectionOrder) {
		this.selectionOrder = selectionOrder;
	}

	public TerminationOptaPlannerDTO getTermination() {
		return termination;
	}

	public void setTermination(TerminationOptaPlannerDTO termination) {
		this.termination = termination;
	}
}
