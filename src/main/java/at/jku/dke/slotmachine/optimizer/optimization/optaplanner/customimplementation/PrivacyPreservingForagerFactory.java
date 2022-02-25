package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import org.optaplanner.core.config.localsearch.decider.forager.FinalistPodiumType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForagerFactory;

import java.util.Objects;

public class PrivacyPreservingForagerFactory<Solution_> extends LocalSearchForagerFactory<Solution_> {
    private final LocalSearchForagerConfig foragerConfig;

    public PrivacyPreservingForagerFactory(LocalSearchForagerConfig foragerConfig) {
        super(foragerConfig);
        this.foragerConfig = foragerConfig;
    }

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig) {
        return new PrivacyPreservingForagerFactory<>(foragerConfig);
    }

    @Override
    public LocalSearchForager<Solution_> buildForager() {
        LocalSearchPickEarlyType pickEarlyType_ =
                Objects.requireNonNullElse(foragerConfig.getPickEarlyType(), LocalSearchPickEarlyType.NEVER);
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);
        FinalistPodiumType finalistPodiumType_ =
                Objects.requireNonNullElse(foragerConfig.getFinalistPodiumType(), FinalistPodiumType.HIGHEST_SCORE);
        // Breaking ties randomly leads statistically to much better results
        boolean breakTieRandomly_ = Objects.requireNonNullElse(foragerConfig.getBreakTieRandomly(), true);
        return new PrivacyPreservingForager<>(finalistPodiumType_.buildFinalistPodium(), pickEarlyType_,
                acceptedCountLimit_, breakTieRandomly_);
    }
}
