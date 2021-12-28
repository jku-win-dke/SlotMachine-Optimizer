package at.jku.dke.slotmachine.optimizer.optimization;

import java.util.stream.IntStream;

public class LogarithmicFitnessEstimator extends FitnessEstimator {

    @Override
    /**
     * Use the logarithmic function f(i) = a * ln(b * i) to determine the fitness value of a solution at position i in
     * the population.
     */
    public double[] estimateFitnessDistribution(int populationSize, double maxFitness, double minFitness) {
        // f(i) = a * ln(b * i)

        //	a = (minFitness - maxFitness) / (ln(1/populationSize))
        //	b = e^c
        //	c = (0 - (minFitness * ln(populationSize))/(minFitness - maxFitness)

        double a = (minFitness - maxFitness) / (Math.log((double) (1 / (double) populationSize)));
        double c = (0 - (minFitness * Math.log(populationSize))) / (minFitness - maxFitness);
        double b = Math.pow(Math.E, c); //e^c

        double[] fitnessValues = new double[populationSize];

        for(int i = 0; i < populationSize; i++) {
            fitnessValues[i] = a * Math.log(b * (i+1));
        }

        return IntStream.range(1, fitnessValues.length)
                        .mapToDouble(i -> fitnessValues[fitnessValues.length - i])
                        .toArray();
    }

    public static void main(String[] args) {
        LogarithmicFitnessEstimator estimator = new LogarithmicFitnessEstimator();

        double[] fitnessValues = estimator.estimateFitnessDistribution(100, 100000, 0);

        for(int i = 0; i < fitnessValues.length; i++) {
            System.out.println(fitnessValues[i]);
        }
    }
}
