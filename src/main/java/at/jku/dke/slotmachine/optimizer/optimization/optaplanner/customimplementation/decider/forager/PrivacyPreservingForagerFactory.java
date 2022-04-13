package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.forager;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import com.ctc.wstx.util.StringUtil;
import org.optaplanner.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.stepcountinghillclimbing.StepCountingHillClimbingType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForagerFactory;
import org.optaplanner.core.impl.score.ScoreUtils;

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
    private final OptaplannerOptimizationStatistics statistics;
    private final LocalSearchAcceptorConfig acceptorConfig;
    private final HeuristicConfigPolicy configPolicy;

    public PrivacyPreservingForagerFactory(LocalSearchForagerConfig foragerConfig, String configurationName, OptaplannerOptimizationStatistics statistics, LocalSearchAcceptorConfig acceptorConfig, HeuristicConfigPolicy<Solution_> configPolicy) {
        super(foragerConfig);
        this.foragerConfig = foragerConfig;
        this.configurationName = configurationName;
        this.statistics = statistics;
        this.acceptorConfig = acceptorConfig;
        this.configPolicy = configPolicy;
    }

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig, String configurationName, OptaplannerOptimizationStatistics statistics, LocalSearchAcceptorConfig acceptorConfig, HeuristicConfigPolicy<Solution_> configPolicy) {
        return new PrivacyPreservingForagerFactory<>(foragerConfig, configurationName, statistics, acceptorConfig, configPolicy);
    }

    /**
     * Builds a forager
     * @return the forager
     */
    @Override
    public LocalSearchForager<Solution_> buildForager() {
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);
        int stepCountingHillClimbingSize = Objects.requireNonNullElse(acceptorConfig.getStepCountingHillClimbingSize(), -1);

        StepCountingHillClimbingType stepCountingHillClimbingType_ =
                Objects.requireNonNullElse(acceptorConfig.getStepCountingHillClimbingType(),
                        StepCountingHillClimbingType.STEP);
        LocalSearchForager<Solution_> forager;

        switch (configurationName){
            case "CUSTOM_STEP_COUNTING_HILL_CLIMBING":
                forager = new PrivacyPreservingStepCountingHillClimbingForager<>(acceptedCountLimit_,  statistics, stepCountingHillClimbingSize, stepCountingHillClimbingType_);
                break;
            case "CUSTOM_SIMULATED_ANNEALING":
                var startingTemperature = configPolicy.getScoreDefinition().parseScore(acceptorConfig.getSimulatedAnnealingStartingTemperature());
                forager = new PrivacyPreservingSimulatedAnnealingForager<>(acceptedCountLimit_, statistics, startingTemperature);
                break;
            default:
                forager = new PrivacyPreservingHillClimbingForager<>(acceptedCountLimit_,statistics);
        }
        return forager;
    }
}
