package at.jku.dke.slotmachine.optimizer.optimization;

public class LinearFitnessEstimator extends FitnessEstimator {
    @Override
    public double[] estimateFitnessDistribution(int populationSize, double maxFitness, double minFitness) {
        double distance = maxFitness - minFitness;
        double delta = distance / populationSize;

        double[] fitnessValues = new double[populationSize];

        for(int i = 0; i < populationSize; i++) {
            fitnessValues[i] = maxFitness - (i * delta);
        }

        return fitnessValues;
    }

    public static void main(String[] args) {
        LinearFitnessEstimator estimator = new LinearFitnessEstimator();

        double[] fitnessValues = estimator.estimateFitnessDistribution(100, 100000, 0);

        for(int i = 0; i < fitnessValues.length; i++) {
            System.out.println(fitnessValues[i]);
        }
    }
}
