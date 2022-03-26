package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.localsearch.decider.LocalSearchDecider;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.solver.termination.Termination;

public class PrivacyPreservingLocalSearchDecider<Solution_> extends LocalSearchDecider<Solution_> {
    public PrivacyPreservingLocalSearchDecider(String logIndentation, Termination<Solution_> termination, MoveSelector<Solution_> moveSelector, Acceptor<Solution_> acceptor, LocalSearchForager<Solution_> forager) {
        super(logIndentation, termination, moveSelector, acceptor, forager);
    }

    @Override
    protected <Score_ extends Score<Score_>> void doMove(LocalSearchMoveScope<Solution_> moveScope) {
        boolean accepted = acceptor.isAccepted(moveScope);
        moveScope.setAccepted(accepted);
        forager.addMove(moveScope);
    }
}
