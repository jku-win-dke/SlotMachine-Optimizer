package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import org.optaplanner.core.config.localsearch.LocalSearchType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForagerFactory;

import java.util.Objects;

/**
 * Extension of the default {@link LocalSearchForagerFactory}
 * that builds foragers implementing evaluation by the privacy engine
 * @param <Solution_> the solution type
 */
public class PrivacyPreservingForagerFactory<Solution_> extends LocalSearchForagerFactory<Solution_> {
    private final LocalSearchForagerConfig foragerConfig;
    private final LocalSearchType searchType;

    public PrivacyPreservingForagerFactory(LocalSearchForagerConfig foragerConfig, LocalSearchType searchType) {
        super(foragerConfig);
        this.foragerConfig = foragerConfig;
        this.searchType = searchType;
    }

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig, LocalSearchType searchType) {
        return new PrivacyPreservingForagerFactory<>(foragerConfig, searchType);
    }

    /**
     * Builds a forager
     * @return the forager
     */
    @Override
    public LocalSearchForager<Solution_> buildForager() {
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);
        return new PrivacyPreservingStepCountingHillClimbingForager<>(acceptedCountLimit_);
        // return new PrivacyPreservingHillClimbingForager<>(acceptedCountLimit_);
    }
}
