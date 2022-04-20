package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;

/**
 * Utility class with the factory-method returning the BatchEvaluator according to the fitness-method
 */
public class BatchEvaluatorFactory {
    private BatchEvaluatorFactory(){
        // utility class
    }
    public static BatchEvaluator getEvaluator(FitnessMethod fitnessMethod, SlotAllocationProblem problem, JeneticsOptimization optimization){
        return switch(fitnessMethod){
            case ORDER_QUANTILES -> new BatchEvaluatorOrderQuantiles(problem, optimization);
            case FITNESS_RANGE_QUANTILES -> new BatchEvaluatorFitnessRangeQuantiles(problem, optimization);
            case ABOVE_ABSOLUTE_THRESHOLD -> new BatchEvaluatorAboveAbsolute(problem, optimization);
            case ABOVE_RELATIVE_THRESHOLD -> new BatchEvaluatorAboveRelative(problem, optimization);
            default -> new BatchEvaluatorOrder(problem, optimization);
        };
    }
}