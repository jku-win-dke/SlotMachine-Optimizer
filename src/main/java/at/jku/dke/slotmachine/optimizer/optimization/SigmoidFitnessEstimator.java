package at.jku.dke.slotmachine.optimizer.optimization;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SigmoidFitnessEstimator extends FitnessEstimator {
    /**
     * f(i) = ((1/0,948683298) * (x/sqrt(1+x^2)) * (difference/2)) + minFitness + (difference/2)
     * @param populationSize
     * @param maxFitness
     * @param minFitness
     * @return
     */
    @Override
    public double[] estimateFitnessDistribution(int populationSize, double maxFitness, double minFitness) {
        double difference = maxFitness - minFitness;

        double[] fitnessValues = new double[populationSize];

        for(int i = 0; i < populationSize; i++) {
            double x = ((i + 1.0) * (6.0 / populationSize)) - 3.0;
            fitnessValues[i] = ((1.0 / 0.948683298) * (x / Math.sqrt(1 + x * x)) * (difference / 2.0)) + minFitness + (difference / 2.0);
        }


        return IntStream.range(1, fitnessValues.length + 1)
                        .mapToDouble(i -> fitnessValues[fitnessValues.length - i])
                        .toArray();
    }

    public static void main(String[] args) {
        SigmoidFitnessEstimator estimator = new SigmoidFitnessEstimator();

        double[] fitnessValues = estimator.estimateFitnessDistribution(100, 100000, 0);

        for(int i = 0; i < fitnessValues.length; i++) {
            System.out.println(fitnessValues[i]);
        }
    }
}
