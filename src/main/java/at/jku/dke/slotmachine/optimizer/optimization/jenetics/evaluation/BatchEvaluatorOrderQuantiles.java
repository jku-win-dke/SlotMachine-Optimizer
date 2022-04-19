package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;

/**
 * BatchEvaluator for the fitness-methode ORDER_QUANTILES
 */
public class BatchEvaluatorOrderQuantiles extends BatchEvaluatorOrder{
    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorOrderQuantiles(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    /**
     * Returns the estimated population size according to the fitness precision
     * @param population the unevaluated population
     * @return the size
     */
    @Override
    protected int getEstimatedPopulationSize(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        return this.optimization.getFitnessPrecision();
    }
}
