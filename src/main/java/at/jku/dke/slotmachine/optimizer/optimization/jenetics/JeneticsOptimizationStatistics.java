package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;

import java.time.Duration;

public class JeneticsOptimizationStatistics extends OptimizationStatistics {
    private long generations;
    private long solutionGeneration;

    public void setGenerations(long count) {
        this.generations = count;
    }

    public long getGenerations() {
        return this.generations;
    }

    public void setSolutionGeneration(long generation) {
        this.solutionGeneration = generation;
    }

    public long getSolutionGeneration() {
        return this.solutionGeneration;
    }
}
