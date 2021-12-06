package at.jku.dke.slotmachine.optimizer.optimization;

public abstract class FitnessEstimator {
    public abstract int[] estimateFitnessDistribution(int populationSize, int maxFitness);
    public abstract int[] estimateFitnessDistribution(int populationSize, int maxFitness, int minFitness);
}
