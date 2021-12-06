package at.jku.dke.slotmachine.optimizer.optimization;

public class LinearFitnessEstimator extends FitnessEstimator {
    @Override
    public int[] estimateFitnessDistribution(int populationSize, int maxFitness) {
        return new int[0];
    }

    @Override
    public int[] estimateFitnessDistribution(int populationSize, int maxFitness, int minFitness) {
        return new int[0];
    }
}
