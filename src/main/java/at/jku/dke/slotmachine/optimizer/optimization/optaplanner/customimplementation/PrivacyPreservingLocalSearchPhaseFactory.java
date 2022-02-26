package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchType;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorType;
import org.optaplanner.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.composite.UnionMoveSelectorFactory;
import org.optaplanner.core.impl.localsearch.DefaultLocalSearchPhase;
import org.optaplanner.core.impl.localsearch.LocalSearchPhase;
import org.optaplanner.core.impl.localsearch.decider.LocalSearchDecider;
import org.optaplanner.core.impl.localsearch.decider.MultiThreadedLocalSearchDecider;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.phase.AbstractPhaseFactory;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.termination.Termination;
import org.optaplanner.core.impl.solver.thread.ChildThreadType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * Custom implementation of the {@link AbstractPhaseFactory} that builds the local search phase
 * for the privacy-preserving optimization.
 * @param <Solution_> the Solution of the optimization
 */
public class PrivacyPreservingLocalSearchPhaseFactory<Solution_> extends AbstractPhaseFactory<Solution_, LocalSearchPhaseConfig> {

    public PrivacyPreservingLocalSearchPhaseFactory(LocalSearchPhaseConfig phaseConfig) {
        super(phaseConfig);
    }

    /**
     * Entry point building the privacy-preserving local search pase
     * @param phaseIndex the index of the search phase
     * @param solverConfigPolicy the config policy of the solver
     * @param bestSolutionRecaller the recaller of the best solution
     * @param solverTermination the terminator
     * @return the search phase
     */
    @Override
    public LocalSearchPhase<Solution_> buildPhase(int phaseIndex, HeuristicConfigPolicy<Solution_> solverConfigPolicy,
                                                  BestSolutionRecaller<Solution_> bestSolutionRecaller, Termination<Solution_> solverTermination) {
        HeuristicConfigPolicy<Solution_> phaseConfigPolicy = solverConfigPolicy.createPhaseConfigPolicy();
        DefaultLocalSearchPhase<Solution_> phase =
                new DefaultLocalSearchPhase<>(phaseIndex, solverConfigPolicy.getLogIndentation(),
                        buildPhaseTermination(phaseConfigPolicy, solverTermination));
        phase.setDecider(buildDecider(phaseConfigPolicy,
                phase.getPhaseTermination()));
        EnvironmentMode environmentMode = phaseConfigPolicy.getEnvironmentMode();
        if (environmentMode.isNonIntrusiveFullAsserted()) {
            phase.setAssertStepScoreFromScratch(true);
        }
        if (environmentMode.isIntrusiveFastAsserted()) {
            phase.setAssertExpectedStepScore(true);
            phase.setAssertShadowVariablesAreNotStaleAfterStep(true);
        }
        return phase;
    }

    /**
     * Builds the cusotm decider with the custom acceptor and forager, and default move selector.
     * @param configPolicy the config policy
     * @param termination the terminator
     * @return the decider that executes the optimization
     */
    private LocalSearchDecider<Solution_> buildDecider(HeuristicConfigPolicy<Solution_> configPolicy,
                                                       Termination<Solution_> termination) {
        MoveSelector<Solution_> moveSelector = buildMoveSelector(configPolicy);
        Acceptor<Solution_> acceptor = buildAcceptor(configPolicy);
        LocalSearchForager<Solution_> forager = buildForager(configPolicy);
        if (moveSelector.isNeverEnding() && !forager.supportsNeverEndingMoveSelector()) {
            throw new IllegalStateException("The moveSelector (" + moveSelector
                    + ") has neverEnding (" + moveSelector.isNeverEnding()
                    + "), but the forager (" + forager
                    + ") does not support it.\n"
                    + "Maybe configure the <forager> with an <acceptedCountLimit>.");
        }
        Integer moveThreadCount = configPolicy.getMoveThreadCount();
        EnvironmentMode environmentMode = configPolicy.getEnvironmentMode();
        LocalSearchDecider<Solution_> decider;
        if (moveThreadCount == null) { // custom decider
            decider = new PrivacyPreservingLocalSearchDecider<>(configPolicy.getLogIndentation(), termination, moveSelector, acceptor, forager);
        } else {
            Integer moveThreadBufferSize = configPolicy.getMoveThreadBufferSize();
            if (moveThreadBufferSize == null) {
                // TODO Verify this is a good default by more meticulous benchmarking on multiple machines and JDK's
                // If it's too low, move threads will need to wait on the buffer, which hurts performance
                // If it's too high, more moves are selected that aren't foraged
                moveThreadBufferSize = 10;
            }
            ThreadFactory threadFactory = configPolicy.buildThreadFactory(ChildThreadType.MOVE_THREAD);
            int selectedMoveBufferSize = moveThreadCount * moveThreadBufferSize;
            MultiThreadedLocalSearchDecider<Solution_> multiThreadedDecider = new MultiThreadedLocalSearchDecider<>(
                    configPolicy.getLogIndentation(), termination, moveSelector, acceptor, forager,
                    threadFactory, moveThreadCount, selectedMoveBufferSize);
            if (environmentMode.isNonIntrusiveFullAsserted()) {
                multiThreadedDecider.setAssertStepScoreFromScratch(true);
            }
            if (environmentMode.isIntrusiveFastAsserted()) {
                multiThreadedDecider.setAssertExpectedStepScore(true);
                multiThreadedDecider.setAssertShadowVariablesAreNotStaleAfterStep(true);
            }
            decider = multiThreadedDecider;
        }
        if (environmentMode.isNonIntrusiveFullAsserted()) {
            decider.setAssertMoveScoreFromScratch(true);
        }
        if (environmentMode.isIntrusiveFastAsserted()) {
            decider.setAssertExpectedUndoMoveScore(true);
        }
        return decider;
    }

    /**
     * Builds a custom acceptor for the privacy-preserving optimization
     * @param configPolicy the configuration policy
     * @return the acceptor
     */
    protected Acceptor<Solution_> buildAcceptor(HeuristicConfigPolicy<Solution_> configPolicy) {
        LocalSearchAcceptorConfig acceptorConfig_;
        if (phaseConfig.getAcceptorConfig() != null) {
            if (phaseConfig.getLocalSearchType() != null) {
                throw new IllegalArgumentException("The localSearchType (" + phaseConfig.getLocalSearchType()
                        + ") must not be configured if the acceptorConfig (" + phaseConfig.getAcceptorConfig()
                        + ") is explicitly configured.");
            }
            acceptorConfig_ = phaseConfig.getAcceptorConfig();
        } else {
            LocalSearchType localSearchType_ =
                    Objects.requireNonNullElse(phaseConfig.getLocalSearchType(), LocalSearchType.LATE_ACCEPTANCE);
            acceptorConfig_ = new PrivacyPreservingLocalSearchAcceptorConfig();
            switch (localSearchType_) {
                case HILL_CLIMBING:
                case VARIABLE_NEIGHBORHOOD_DESCENT:
                    acceptorConfig_.setAcceptorTypeList(Collections.singletonList(AcceptorType.HILL_CLIMBING));
                    break;
                case TABU_SEARCH:
                    acceptorConfig_.setAcceptorTypeList(Collections.singletonList(AcceptorType.ENTITY_TABU));
                    break;
                case SIMULATED_ANNEALING:
                    acceptorConfig_.setAcceptorTypeList(Collections.singletonList(AcceptorType.SIMULATED_ANNEALING));
                    break;
                case LATE_ACCEPTANCE:
                    acceptorConfig_.setAcceptorTypeList(Collections.singletonList(AcceptorType.LATE_ACCEPTANCE));
                    break;
                case GREAT_DELUGE:
                    acceptorConfig_.setAcceptorTypeList(Collections.singletonList(AcceptorType.GREAT_DELUGE));
                    break;
                default:
                    throw new IllegalStateException("The localSearchType (" + localSearchType_
                            + ") is not implemented.");
            }
        }
        // custom acceptor
        return PrivacyPreservingAcceptorFactory.<Solution_> create(acceptorConfig_).buildAcceptor(configPolicy);
    }

    /**
     * Builds a custom forager for privacy-preserving optimization
     * @param configPolicy the config policy
     * @return the forager
     */
    protected LocalSearchForager<Solution_> buildForager(HeuristicConfigPolicy<Solution_> configPolicy) {
        LocalSearchForagerConfig foragerConfig_;
        if (phaseConfig.getForagerConfig() != null) { // Use config specified in xml
            foragerConfig_ = phaseConfig.getForagerConfig();
        } else { // use custom values
            LocalSearchType localSearchType_ =
                    Objects.requireNonNullElse(phaseConfig.getLocalSearchType(), LocalSearchType.LATE_ACCEPTANCE);
            foragerConfig_ = new PrivacyPreservingLocalSearchForagerConfig();

            switch (localSearchType_) {
                case HILL_CLIMBING, SIMULATED_ANNEALING, LATE_ACCEPTANCE, GREAT_DELUGE:
                    // Setting accepted count limit to 50 because privacy engine requires more solutions for evaluation
                    foragerConfig_.setAcceptedCountLimit(50);
                    break;
                case TABU_SEARCH:
                    // Slow stepping algorithm
                    foragerConfig_.setAcceptedCountLimit(1000);
                    break;
                case VARIABLE_NEIGHBORHOOD_DESCENT:
                    foragerConfig_.setPickEarlyType(LocalSearchPickEarlyType.FIRST_LAST_STEP_SCORE_IMPROVING);
                    break;
                default:
                    throw new IllegalStateException("The localSearchType (" + localSearchType_
                            + ") is not implemented.");
            }
        }
        // custom forager
        var factory = PrivacyPreservingForagerFactory.<Solution_> create(foragerConfig_);
        return factory.buildForager();

    }

    /**
     * Builds the move selector. Implementation identical
     * to implementation of the {@link org.optaplanner.core.impl.localsearch.DefaultLocalSearchPhaseFactory}
     * @param configPolicy the config policy
     * @return the move selector
     */
    protected MoveSelector<Solution_> buildMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy) {
        MoveSelector<Solution_> moveSelector;
        SelectionCacheType defaultCacheType = SelectionCacheType.JUST_IN_TIME;
        SelectionOrder defaultSelectionOrder;
        if (phaseConfig.getLocalSearchType() == LocalSearchType.VARIABLE_NEIGHBORHOOD_DESCENT) {
            defaultSelectionOrder = SelectionOrder.ORIGINAL;
        } else {
            defaultSelectionOrder = SelectionOrder.RANDOM;
        }
        if (phaseConfig.getMoveSelectorConfig() == null) {
            // Default to changeMoveSelector and swapMoveSelector
            // TODO: only allow swap moves (strangely enough, this hinders performance although changemoves are not even considered)
            UnionMoveSelectorConfig unionMoveSelectorConfig = new UnionMoveSelectorConfig();
            unionMoveSelectorConfig.setMoveSelectorConfigList(Arrays.asList(new ChangeMoveSelectorConfig(),
                    new SwapMoveSelectorConfig()));
            moveSelector = new UnionMoveSelectorFactory<Solution_>(unionMoveSelectorConfig)
                    .buildMoveSelector(configPolicy, defaultCacheType, defaultSelectionOrder);
        } else {
            moveSelector = MoveSelectorFactory.<Solution_> create(phaseConfig.getMoveSelectorConfig())
                    .buildMoveSelector(configPolicy, defaultCacheType, defaultSelectionOrder);
        }
        return moveSelector;
    }
}
