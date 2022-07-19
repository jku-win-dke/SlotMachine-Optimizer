package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;

/**
 * Utility class with the factory-method returning the {@link BatchEvaluator} according to the {@link FitnessMethod}.
 */
public class BatchEvaluatorFactory {
    private BatchEvaluatorFactory(){
        // utility class
    }

    /**
     * Factory method that returns the {@link BatchEvaluator} according to the {@link FitnessMethod}.
     * @param fitnessMethod the fitness method
     * @param problem the problem definition
     * @param optimization the optimization
     * @return the evaluator
     */
    public static BatchEvaluator getEvaluator(FitnessMethod fitnessMethod, SlotAllocationProblem problem, JeneticsOptimization optimization){
        return switch(fitnessMethod){
            case ORDER_QUANTILES -> new BatchEvaluatorOrderQuantiles(problem, optimization);
            case FITNESS_RANGE_QUANTILES -> new BatchEvaluatorFitnessRangeQuantiles(problem, optimization);
            case ABOVE_ABSOLUTE_THRESHOLD -> new BatchEvaluatorAboveAbsolute(problem, optimization);
            case ABOVE_RELATIVE_THRESHOLD -> new BatchEvaluatorAboveRelative(problem, optimization);
            case ACTUAL_VALUES -> new BatchEvaluatorActualValues(problem, optimization);
            default -> new BatchEvaluatorOrder(problem, optimization);
        };
    }
}
