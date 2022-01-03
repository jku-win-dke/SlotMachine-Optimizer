package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;

public class JeneticsOptimizationStatistics extends OptimizationStatistics {
    private long solutionGeneration;

    public void setSolutionGeneration(long generation) {
        this.solutionGeneration = generation;
    }

    public long getSolutionGeneration() {
        return this.solutionGeneration;
    }
}
