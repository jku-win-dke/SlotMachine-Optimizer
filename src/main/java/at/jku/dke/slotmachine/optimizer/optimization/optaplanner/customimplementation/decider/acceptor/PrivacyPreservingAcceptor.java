package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.acceptor;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.localsearch.decider.acceptor.AbstractAcceptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

import java.util.Collection;
import java.util.Collections;

public class PrivacyPreservingAcceptor<Solution_> extends AbstractAcceptor<Solution_> {
// ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * Checks if a move suggested by the {@link org.optaplanner.core.impl.localsearch.decider.LocalSearchDecider}
     * is accepted. Currently all moves are accepted because fitness calculation of suggested solutions
     * does not comply with the constraints of the privacy engine
     * @param moveScope the suggested move/candidate in a given search step
     * @return true
     */
    @Override
    public boolean isAccepted(LocalSearchMoveScope<Solution_> moveScope) {
        return PrivacyPreservingHardScoreComparator.validateHardConstraints((Collection<FlightPlanningEntity>) moveScope.getMove().getPlanningEntities());
    }
}
