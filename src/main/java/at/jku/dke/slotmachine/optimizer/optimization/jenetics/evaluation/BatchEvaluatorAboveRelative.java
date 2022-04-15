package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BatchEvaluatorAboveRelative extends BatchEvaluatorAbove{
    private final double relativeThreshold = 0.3;

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveRelative(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected double getThreshold(PopulationEvaluation evaluation) {
        return percentile(evaluation.evaluatedPopulation.stream().map(ph -> (double) ph.fitness()).toList(), (1 - relativeThreshold) * 100);
    }
}
