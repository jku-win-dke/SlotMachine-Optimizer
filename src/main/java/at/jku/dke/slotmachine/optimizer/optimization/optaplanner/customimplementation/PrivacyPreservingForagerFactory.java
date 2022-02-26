package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import org.optaplanner.core.config.localsearch.decider.forager.FinalistPodiumType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForagerFactory;
import org.optaplanner.core.impl.solver.scope.SolverScope;

import java.util.Objects;

/**
 * Extension of the default {@link LocalSearchForagerFactory}
 * that builds foragers implementing evaluation by the privacy engine
 * @param <Solution_> the solution type
 */
public class PrivacyPreservingForagerFactory<Solution_> extends LocalSearchForagerFactory<Solution_> {
    private final LocalSearchForagerConfig foragerConfig;
    private SolverScope<Solution_> solverScope;

    public PrivacyPreservingForagerFactory(LocalSearchForagerConfig foragerConfig, SolverScope<Solution_> solverScope) {
        super(foragerConfig);
        this.foragerConfig = foragerConfig;
        this.solverScope = solverScope;
    }

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig, SolverScope<Solution_> solverScope) {
        return new PrivacyPreservingForagerFactory<>(foragerConfig, solverScope);
    }

    // TODO: Think about different foragers according to the search type

    /**
     * Builds a forager
     * @return the forager
     */
    @Override
    public LocalSearchForager<Solution_> buildForager() {
        LocalSearchPickEarlyType pickEarlyType_ =
                Objects.requireNonNullElse(foragerConfig.getPickEarlyType(), LocalSearchPickEarlyType.NEVER);
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);

        // TODO: Replace the finalist podium with the privacy engine
        FinalistPodiumType finalistPodiumType_ =
                Objects.requireNonNullElse(foragerConfig.getFinalistPodiumType(), FinalistPodiumType.HIGHEST_SCORE);
        // Breaking ties randomly leads statistically to much better results
        boolean breakTieRandomly_ = Objects.requireNonNullElse(foragerConfig.getBreakTieRandomly(), true);

        return new PrivacyPreservingForager<>(finalistPodiumType_.buildFinalistPodium(),
                acceptedCountLimit_, breakTieRandomly_, solverScope);
    }
}
