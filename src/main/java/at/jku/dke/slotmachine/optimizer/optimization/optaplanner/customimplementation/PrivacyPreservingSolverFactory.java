package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.OptaplannerOptimizationStatistics;
import io.micrometer.core.instrument.Tags;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.monitoring.MonitoringConfig;
import org.optaplanner.core.config.solver.monitoring.SolverMetric;
import org.optaplanner.core.config.solver.random.RandomType;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.phase.Phase;
import org.optaplanner.core.impl.phase.PhaseFactory;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.DefaultSolverFactory;
import org.optaplanner.core.impl.solver.random.DefaultRandomFactory;
import org.optaplanner.core.impl.solver.random.RandomFactory;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecallerFactory;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.termination.BasicPlumbingTermination;
import org.optaplanner.core.impl.solver.termination.Termination;
import org.optaplanner.core.impl.solver.termination.TerminationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Cannot inherit form final default implementation

/**
 * Implementation of the SolverFactory facilitating privacy-preserving optimization
 * by setting the implementation of the {@link org.optaplanner.core.impl.phase.AbstractPhaseFactory}
 * for the local search phase to the custom {@link PrivacyPreservingLocalSearchPhaseFactory}
 * @param <Solution_>
 */
public class PrivacyPreservingSolverFactory<Solution_> implements SolverFactory<Solution_> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyPreservingSolverFactory.class);
    private static final long DEFAULT_RANDOM_SEED = 0L;

    private String configurationName;
    private final SolverConfig solverConfig;
    private final OptaplannerOptimizationStatistics statistics;
    private final AssignmentProblemType assignmentProblemType;

    /**
     * The default solver factory to use implemented methods that do not require changes for privacy-preserving optimization
     */
    private final DefaultSolverFactory<Solution_> defaultSolverFactory;

    public PrivacyPreservingSolverFactory(SolverConfig solverConfig, String configurationName, OptaplannerOptimizationStatistics statistics, AssignmentProblemType assignmentProblemType) {
        if (solverConfig == null) {
            throw new IllegalStateException("The solverConfig (" + solverConfig + ") cannot be null.");
        }
        this.solverConfig = solverConfig;
        this.defaultSolverFactory = new DefaultSolverFactory<>(solverConfig);
        this.configurationName = configurationName;
        this.statistics = statistics;
        this.assignmentProblemType = assignmentProblemType;
    }

    public InnerScoreDirectorFactory<Solution_, ?> getScoreDirectorFactory() {
        return buildScoreDirectorFactory(solverConfig.determineEnvironmentMode());
    }

    /**
     * Builds a {@link DefaultSolver} with the custom {@link PrivacyPreservingLocalSearchPhaseFactory}
     * that facilitates the privacy-preserving optimization regarding the local-search step
     * @return the solver
     */
    @Override
    public Solver<Solution_> buildSolver() {
        EnvironmentMode environmentMode_ = solverConfig.determineEnvironmentMode();
        boolean daemon_ = Objects.requireNonNullElse(solverConfig.getDaemon(), false);

        RandomFactory randomFactory = buildRandomFactory(environmentMode_);

        Integer moveThreadCount_ = new MoveThreadCountResolver().resolveMoveThreadCount(solverConfig.getMoveThreadCount());

        InnerScoreDirectorFactory<Solution_, ?> scoreDirectorFactory = buildScoreDirectorFactory(environmentMode_);
        boolean constraintMatchEnabledPreference = environmentMode_.isAsserted();
        SolverScope<Solution_> solverScope = new SolverScope<>();
        MonitoringConfig monitoringConfig = solverConfig.determineMetricConfig();
        solverScope.setMonitoringTags(Tags.empty());
        if (!monitoringConfig.getSolverMetricList().isEmpty()) {
            solverScope.setSolverMetricSet(EnumSet.copyOf(monitoringConfig.getSolverMetricList()));
        } else {
            solverScope.setSolverMetricSet(EnumSet.noneOf(SolverMetric.class));
        }
        solverScope.setScoreDirector(scoreDirectorFactory.buildScoreDirector(true, constraintMatchEnabledPreference));

        if ((solverScope.isMetricEnabled(SolverMetric.CONSTRAINT_MATCH_TOTAL_STEP_SCORE)
                || solverScope.isMetricEnabled(SolverMetric.CONSTRAINT_MATCH_TOTAL_BEST_SCORE)) &&
                !solverScope.getScoreDirector().isConstraintMatchEnabled()) {
            LOGGER.warn("The metrics [{}, {}] cannot function properly" +
                            " because ConstraintMatches are not supported on the ScoreDirector.",
                    SolverMetric.CONSTRAINT_MATCH_TOTAL_STEP_SCORE.getMeterId(),
                    SolverMetric.CONSTRAINT_MATCH_TOTAL_BEST_SCORE.getMeterId());
        }

        // TODO: Implement custom best solution recaller that saves more good solutions and does not attempt to calculateScore
        BestSolutionRecaller<Solution_> bestSolutionRecaller =
                BestSolutionRecallerFactory.create().buildBestSolutionRecaller(environmentMode_);
        HeuristicConfigPolicy<Solution_> configPolicy = new HeuristicConfigPolicy<>(environmentMode_,
                moveThreadCount_, solverConfig.getMoveThreadBufferSize(), solverConfig.getThreadFactoryClass(),
                scoreDirectorFactory);
        TerminationConfig terminationConfig_ = solverConfig.getTerminationConfig() == null
                ? new TerminationConfig()
                : solverConfig.getTerminationConfig();
        BasicPlumbingTermination<Solution_> basicPlumbingTermination = new BasicPlumbingTermination<>(daemon_);
        Termination<Solution_> termination = TerminationFactory.<Solution_> create(terminationConfig_)
                .buildTermination(configPolicy, basicPlumbingTermination);

        List<Phase<Solution_>> phaseList = buildPhaseList(configPolicy, bestSolutionRecaller, termination);

        return new DefaultSolver<>(environmentMode_, randomFactory, bestSolutionRecaller, basicPlumbingTermination,
                        termination, phaseList, solverScope,
                        moveThreadCount_ == null ? SolverConfig.MOVE_THREAD_COUNT_NONE : Integer.toString(moveThreadCount_));
    }

    /**
     * @param environmentMode never null
     * @return never null
     */
    public InnerScoreDirectorFactory<Solution_, ?> buildScoreDirectorFactory(EnvironmentMode environmentMode) {
        return defaultSolverFactory.buildScoreDirectorFactory(environmentMode);
    }

    /**
     * @param environmentMode never null
     * @return never null
     */
    public SolutionDescriptor<Solution_> buildSolutionDescriptor(EnvironmentMode environmentMode) {
      return defaultSolverFactory.buildSolutionDescriptor(environmentMode);
    }

    protected RandomFactory buildRandomFactory(EnvironmentMode environmentMode_) {
        RandomFactory randomFactory;
        if (solverConfig.getRandomFactoryClass() != null) {
            if (solverConfig.getRandomType() != null || solverConfig.getRandomSeed() != null) {
                throw new IllegalArgumentException(
                        "The solverConfig with randomFactoryClass (" + solverConfig.getRandomFactoryClass()
                                + ") has a non-null randomType (" + solverConfig.getRandomType()
                                + ") or a non-null randomSeed (" + solverConfig.getRandomSeed() + ").");
            }
            randomFactory = ConfigUtils.newInstance(solverConfig, "randomFactoryClass", solverConfig.getRandomFactoryClass());
        } else {
            RandomType randomType_ = Objects.requireNonNullElse(solverConfig.getRandomType(), RandomType.JDK);
            Long randomSeed_ = solverConfig.getRandomSeed();
            if (solverConfig.getRandomSeed() == null && environmentMode_ != EnvironmentMode.NON_REPRODUCIBLE) {
                randomSeed_ = DEFAULT_RANDOM_SEED;
            }
            randomFactory = new DefaultRandomFactory(randomType_, randomSeed_);
        }
        return randomFactory;
    }

    /**
     * Builds the custom phase list with the {@link PrivacyPreservingLocalSearchPhaseFactory} for the local-search-phase
     * @param configPolicy the config policy
     * @param bestSolutionRecaller the recaller of the best solution
     * @param termination the terminator
     * @return the phase list
     */
    protected List<Phase<Solution_>> buildPhaseList(HeuristicConfigPolicy<Solution_> configPolicy,
                                                    BestSolutionRecaller<Solution_> bestSolutionRecaller, Termination<Solution_> termination) {
        List<PhaseConfig> phaseConfigList_ = solverConfig.getPhaseConfigList();
        if (ConfigUtils.isEmptyCollection(phaseConfigList_)) {
            phaseConfigList_ = Arrays.asList(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());
        }
        List<Phase<Solution_>> phaseList = new ArrayList<>(phaseConfigList_.size());
        int phaseIndex = 0;
        // TODO: enable/disable construction heuristic considering performance
        for (PhaseConfig phaseConfig : phaseConfigList_) {
            PhaseFactory<Solution_> phaseFactory = null;
            // Setting Custom LocalSearchPhaseFactory for local search phase
            if (LocalSearchPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
                phaseFactory = new PrivacyPreservingLocalSearchPhaseFactory<>((LocalSearchPhaseConfig) phaseConfig, configurationName, statistics, assignmentProblemType);
            }
            if(phaseFactory != null){
                Phase<Solution_> phase =
                        phaseFactory.buildPhase(phaseIndex, configPolicy, bestSolutionRecaller, termination);
                phaseList.add(phase);
                phaseIndex++;
            }

        }
        return phaseList;
    }

    // Required for testability as final classes cannot be mocked.
    protected static class MoveThreadCountResolver {

        protected Integer resolveMoveThreadCount(String moveThreadCount) {
            int availableProcessorCount = getAvailableProcessors();
            Integer resolvedMoveThreadCount;
            if (moveThreadCount == null || moveThreadCount.equals(SolverConfig.MOVE_THREAD_COUNT_NONE)) {
                return null;
            } else if (moveThreadCount.equals(SolverConfig.MOVE_THREAD_COUNT_AUTO)) {
                // Leave one for the Operating System and 1 for the solver thread, take the rest
                resolvedMoveThreadCount = (availableProcessorCount - 2);
                // A moveThreadCount beyond 4 is currently typically slower
                // TODO remove limitation after fixing https://issues.redhat.com/browse/PLANNER-2449
                if (resolvedMoveThreadCount > 4) {
                    resolvedMoveThreadCount = 4;
                }
                if (resolvedMoveThreadCount <= 1) {
                    // Fall back to single threaded solving with no move threads.
                    // To deliberately enforce 1 moveThread, set the moveThreadCount explicitly to 1.
                    return null;
                }
            } else {
                resolvedMoveThreadCount = ConfigUtils.resolvePoolSize("moveThreadCount", moveThreadCount,
                        SolverConfig.MOVE_THREAD_COUNT_NONE, SolverConfig.MOVE_THREAD_COUNT_AUTO);
            }
            if (resolvedMoveThreadCount < 1) {
                throw new IllegalArgumentException("The moveThreadCount (" + moveThreadCount
                        + ") resulted in a resolvedMoveThreadCount (" + resolvedMoveThreadCount
                        + ") that is lower than 1.");
            }
            if (resolvedMoveThreadCount > availableProcessorCount) {
                LOGGER.warn("The resolvedMoveThreadCount ({}) is higher "
                                + "than the availableProcessorCount ({}), which is counter-efficient.",
                        resolvedMoveThreadCount, availableProcessorCount);
                // Still allow it, to reproduce issues of a high-end server machine on a low-end developer machine
            }
            return resolvedMoveThreadCount;
        }

        protected int getAvailableProcessors() {
            return Runtime.getRuntime().availableProcessors();
        }
    }


}
