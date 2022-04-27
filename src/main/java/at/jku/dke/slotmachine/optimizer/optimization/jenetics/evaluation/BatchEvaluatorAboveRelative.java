package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;

/**
 * BatchEvaluator for the fitness-method ABOVE_RELATIVE_THRESHOLD
 */
public class BatchEvaluatorAboveRelative extends BatchEvaluatorAbove{
    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveRelative(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    /**
     * Returns the threshold for the evaluation as percentile of the fitness-values according to the configured fitness-precision
     * @param evaluation the evaluated population
     * @return the percentile
     */
    @Override
    protected double getThreshold(PopulationEvaluation evaluation) {
        return percentile(evaluation.evaluatedPopulation.stream().map(ph -> (double) ph.fitness()).toList(), (100 - this.optimization.getFitnessPrecision()));
    }
}
