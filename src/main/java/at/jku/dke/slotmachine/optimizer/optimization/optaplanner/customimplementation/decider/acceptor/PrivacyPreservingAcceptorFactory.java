package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.acceptor;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.AssignmentProblemType;
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
    private AssignmentProblemType assignmentProblemType;

    public static <Solution_> AcceptorFactory<Solution_> create(LocalSearchAcceptorConfig acceptorConfig, AssignmentProblemType assignmentProblemType) {
        return new PrivacyPreservingAcceptorFactory<>(acceptorConfig, assignmentProblemType);
    }

    public PrivacyPreservingAcceptorFactory(LocalSearchAcceptorConfig acceptorConfig, AssignmentProblemType assignmentProblemType) {
        super(acceptorConfig);
        this.acceptorConfig = acceptorConfig;
        this.assignmentProblemType = assignmentProblemType;
    }

    @Override
    /**
     * Builds a privacy-preserving acceptor
     */
    public Acceptor<Solution_> buildAcceptor(HeuristicConfigPolicy<Solution_> configPolicy) {
        Acceptor<Solution_> acceptor = new PrivacyPreservingSwapMoveAcceptor<>();
        if(this.assignmentProblemType == AssignmentProblemType.UNBALANCED){
            acceptor = new PrivacyPreservingAcceptor<>();
        }
        return acceptor;
    }
}
