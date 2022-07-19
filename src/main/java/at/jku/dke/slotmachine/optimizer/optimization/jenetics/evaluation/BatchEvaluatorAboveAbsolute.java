package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import io.jenetics.Phenotype;

import java.util.Comparator;

/**
 * BatchEvaluator for the fitness-method ABOVE_ABSOLUTE_THRESHOLD
 */
public class BatchEvaluatorAboveAbsolute extends BatchEvaluatorAbove{
    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveAbsolute(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    /**
     * Returns the threshold according to the fitness-precision and the maximum fitness.
     * @param evaluation the evaluated population
     * @return the threshold
     */
    @Override
    protected double getThreshold(PopulationEvaluation evaluation) {
        int maximum;
        var phenotype = evaluation.evaluatedPopulation.stream().max(Comparator.comparingInt(Phenotype::fitness));
        maximum = phenotype.isPresent() ? phenotype.get().fitness() : (int) evaluation.maxFitness;
        if(maximum < 0){
            return maximum * (1 + (1 - this.optimization.getFitnessPrecision() / 100.0));
        }
        return maximum * (this.optimization.getFitnessPrecision() / 100.0);
    }
}
