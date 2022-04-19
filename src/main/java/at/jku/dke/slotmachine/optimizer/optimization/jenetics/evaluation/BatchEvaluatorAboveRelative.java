package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;

public class BatchEvaluatorAboveRelative extends BatchEvaluatorAbove{
    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveRelative(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected double getThreshold(PopulationEvaluation evaluation) {
        return percentile(evaluation.evaluatedPopulation.stream().map(ph -> (double) ph.fitness()).toList(), (100 - this.optimization.getFitnessPrecision()));
    }
}
