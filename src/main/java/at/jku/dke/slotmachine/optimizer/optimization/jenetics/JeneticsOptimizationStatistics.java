package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;

import java.time.Duration;

public class JeneticsOptimizationStatistics extends OptimizationStatistics {
    private int generations;
    private int minFitness;
    private int maxFitness;
    private double meanFitness;
    private int generationMaxFitness;
    private int generatedIndividuals;
    private Duration overallExecution;
}
