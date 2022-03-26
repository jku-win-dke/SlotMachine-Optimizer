package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.acceptor;

import org.optaplanner.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.AcceptorFactory;

/**
 * The custom implementation of the {@link AcceptorFactory} that builds acceptors
 * which are able to comply with the constraints of the privacy engine, most of all
 * that no computation of fitness is allowed
 * @param <Solution_>
 */
public class PrivacyPreservingAcceptorFactory<Solution_> extends AcceptorFactory<Solution_>{
    LocalSearchAcceptorConfig acceptorConfig;

    public static <Solution_> AcceptorFactory<Solution_> create(LocalSearchAcceptorConfig acceptorConfig) {
        return new PrivacyPreservingAcceptorFactory<>(acceptorConfig);
    }

    public PrivacyPreservingAcceptorFactory(LocalSearchAcceptorConfig acceptorConfig) {
        super(acceptorConfig);
        this.acceptorConfig = acceptorConfig;
    }

    // TODO: Think about different acceptors according to the local search type of the config
    @Override
    /**
     * Builds a privacy-preserving acceptor
     */
    public Acceptor<Solution_> buildAcceptor(HeuristicConfigPolicy<Solution_> configPolicy) {
        return new PrivacyPreservingSwapMoveAcceptor<>();
    }

}
