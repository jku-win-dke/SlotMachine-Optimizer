package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForagerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Extension of the default {@link LocalSearchForagerFactory}
 * that builds foragers implementing evaluation by the privacy engine
 * @param <Solution_> the solution type
 */
public class PrivacyPreservingForagerFactory<Solution_> extends LocalSearchForagerFactory<Solution_> {
    private final LocalSearchForagerConfig foragerConfig;
    private final String configurationName;
    private final List<Solution_> intermediateResults;
    private final OptaplannerOptimizationStatistics statistics;

    public PrivacyPreservingForagerFactory(LocalSearchForagerConfig foragerConfig, String configurationName, List<Solution_> intermediateResults, OptaplannerOptimizationStatistics statistics) {
        super(foragerConfig);
        this.foragerConfig = foragerConfig;
        this.configurationName = configurationName;
        this.intermediateResults = intermediateResults;
        this.statistics = statistics;
    }

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig, String configurationName, List<Solution_> intermediateResults, OptaplannerOptimizationStatistics statistics) {
        return new PrivacyPreservingForagerFactory<>(foragerConfig, configurationName, intermediateResults, statistics);
    }

    /**
     * Builds a forager
     * @return the forager
     */
    @Override
    public LocalSearchForager<Solution_> buildForager() {
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);
        LocalSearchForager<Solution_> forager;
        switch (configurationName){
            case "CUSTOM_STEP_COUNTING_HILL_CLIMBING":
                forager = new PrivacyPreservingStepCountingHillClimbingForager<>(acceptedCountLimit_, intermediateResults, statistics);
                break;
            default:
                forager = new PrivacyPreservingHillClimbingForager<>(acceptedCountLimit_, intermediateResults, statistics);
        }
        return forager;
    }
}
