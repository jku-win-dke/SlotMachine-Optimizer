package at.jku.dke.slotmachine.optimizer.optimization;

public class FitnessEvolutionStep {
    private int generation;
    private Double[] evaluatedPopulation = null;
    private Double[] estimatedPopulation = null;

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public Double[] getEvaluatedPopulation() {
        return evaluatedPopulation;
    }

    public void setEvaluatedPopulation(Double[] evaluatedPopulation) {
        this.evaluatedPopulation = evaluatedPopulation;
    }

    public Double[] getEstimatedPopulation() {
        return estimatedPopulation;
    }

    public void setEstimatedPopulation(Double[] estimatedPopulation) {
        this.estimatedPopulation = estimatedPopulation;
    }
}
