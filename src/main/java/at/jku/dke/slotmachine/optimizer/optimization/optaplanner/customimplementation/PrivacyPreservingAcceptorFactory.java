package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import org.optaplanner.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.AcceptorFactory;


public class PrivacyPreservingAcceptorFactory<Solution_> extends AcceptorFactory<Solution_>{
    LocalSearchAcceptorConfig acceptorConfig;

    public static <Solution_> AcceptorFactory<Solution_> create(LocalSearchAcceptorConfig acceptorConfig) {
        return new PrivacyPreservingAcceptorFactory<>(acceptorConfig);
    }

    public PrivacyPreservingAcceptorFactory(LocalSearchAcceptorConfig acceptorConfig) {
        super(acceptorConfig);
        this.acceptorConfig = acceptorConfig;
    }

    @Override
    public Acceptor<Solution_> buildAcceptor(HeuristicConfigPolicy<Solution_> configPolicy) {
        return new PrivacyPreservingHillClimbingAcceptor<>();
    }

}
