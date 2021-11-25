package at.jku.dke.slotmachine.optimizer.optimization;

public class OptimizationStatistics {

    private double solutionFitness = Integer.MIN_VALUE;
    private int fitnessFunctionInvocations = Integer.MIN_VALUE;

    public int getFitnessFunctionInvocations() {
        return fitnessFunctionInvocations;
    }

    public void setFitnessFunctionInvocations(int fitnessFunctionInvocations) {
        this.fitnessFunctionInvocations = fitnessFunctionInvocations;
    }

    public double getSolutionFitness() {
        return solutionFitness;
    }

    public void setSolutionFitness(double solutionFitness) {
        this.solutionFitness = solutionFitness;
    }
}
