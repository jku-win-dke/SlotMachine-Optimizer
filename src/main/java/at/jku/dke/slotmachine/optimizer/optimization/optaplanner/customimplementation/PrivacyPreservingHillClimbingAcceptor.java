package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;


import org.optaplanner.core.impl.localsearch.decider.acceptor.AbstractAcceptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

/**
 * Basic custom implementation of a {@link AbstractAcceptor} for privacy-preserving optimization
 * @param <Solution_>
 */
public class PrivacyPreservingHillClimbingAcceptor<Solution_> extends AbstractAcceptor<Solution_>{
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
        return moveScope.getMove().getPlanningEntities().size() == 2;
    }

}
