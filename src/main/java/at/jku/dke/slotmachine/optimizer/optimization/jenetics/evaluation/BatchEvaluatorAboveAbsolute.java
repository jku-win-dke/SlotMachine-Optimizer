package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BatchEvaluatorAboveAbsolute extends BatchEvaluatorAbove{
    double threshold = 0.7;

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveAbsolute(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected double getThreshold(PopulationEvaluation evaluation) {
        return evaluation.maxFitness * threshold;
    }


}
