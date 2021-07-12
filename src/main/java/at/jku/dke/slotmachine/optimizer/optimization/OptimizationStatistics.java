package at.jku.dke.slotmachine.optimizer.optimization;

public class OptimizationStatistics {

    private int solutionFitness;
    private int fitnessFunctionInvocations;

    public int getFitnessFunctionInvocations() {
        return fitnessFunctionInvocations;
    }

    public void setFitnessFunctionInvocations(int fitnessFunctionInvocations) {
        this.fitnessFunctionInvocations = fitnessFunctionInvocations;
    }

    public int getSolutionFitness() {
        return solutionFitness;
    }

    public void setSolutionFitness(int solutionFitness) {
        this.solutionFitness = solutionFitness;
    }
}
