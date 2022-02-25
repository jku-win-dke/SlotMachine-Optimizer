package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;


import org.optaplanner.core.impl.localsearch.decider.acceptor.AbstractAcceptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

public class PrivacyPreservingHillClimbingAcceptor<Solution_> extends AbstractAcceptor<Solution_>{
// ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isAccepted(LocalSearchMoveScope<Solution_> moveScope) {
        return true;
    }

}
