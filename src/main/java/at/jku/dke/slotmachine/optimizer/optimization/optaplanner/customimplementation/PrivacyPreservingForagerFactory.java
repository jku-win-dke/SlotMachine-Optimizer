package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

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

    public PrivacyPreservingForagerFactory(LocalSearchForagerConfig foragerConfig) {
        super(foragerConfig);
        this.foragerConfig = foragerConfig;
    }

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig) {
        return new PrivacyPreservingForagerFactory<>(foragerConfig);
    }

    // TODO: Think about different foragers according to the search type

    /**
     * Builds a forager
     * @return the forager
     */
    @Override
    public LocalSearchForager<Solution_> buildForager() {
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);
        return new PrivacyPreservingDefaultForager<>(acceptedCountLimit_);
    }
}
