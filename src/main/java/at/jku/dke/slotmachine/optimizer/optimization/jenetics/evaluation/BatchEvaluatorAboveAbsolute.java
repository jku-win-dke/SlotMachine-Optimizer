package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;

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

    @Override
    protected double getThreshold(PopulationEvaluation evaluation) {
        return evaluation.maxFitness * this.optimization.getFitnessPrecision() / 100.0;
    }


}
