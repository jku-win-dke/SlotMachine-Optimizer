package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;
import org.optaplanner.core.impl.localsearch.decider.forager.privacypreserving.LocalSearchStatistics;

public class OptaplannerOptimizationStatistics extends OptimizationStatistics {
    private LocalSearchStatistics localSearchStatistics;

    public LocalSearchStatistics getLocalSearchStatistics() {
        return localSearchStatistics;
    }

    public void setLocalSearchStatistics(LocalSearchStatistics localSearchStatistics) {
        this.localSearchStatistics = localSearchStatistics;
    }
}
