package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /**
     * Utility method that calculates a percentile
     * @param values the values
     * @param percentile the desired percentile
     * @return the percentile
     */
    protected static double percentile(List<Double> values, double percentile) {
        values = new ArrayList<>(values);
        Collections.sort(values);
        int index = (int) Math.ceil((percentile / 100) * values.size());
        return values.get(index);
    }
}
