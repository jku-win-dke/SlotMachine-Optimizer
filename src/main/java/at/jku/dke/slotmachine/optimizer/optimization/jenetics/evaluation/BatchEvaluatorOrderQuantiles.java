package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class BatchEvaluatorOrderQuantiles extends BatchEvaluator{
    private static final Logger logger = LogManager.getLogger();

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorOrderQuantiles(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream;

        if(this.optimization.getFitnessEstimator() != null) {
            int estimatedPopulationSize = this.optimization.getFitnessPrecision();
            logger.debug("Estimated population size: " + estimatedPopulationSize);

            logger.debug("Getting estimated fitness value from estimator: " + this.optimization.getFitnessEstimator().getClass());
            double[] estimatedFitnessValues =
                    this.optimization.getFitnessEstimator().estimateFitnessDistribution(estimatedPopulationSize, maxFitness, minFitness);


            // Ignore
            List<Double> evaluatedFitnessValues = evaluatedPopulation.stream().map(p -> (double )p.fitness()).toList();
            double[] percentiles = new double[this.optimization.getFitnessPrecision()];
            for(int i = 0; i < percentiles.length; i++){
                percentiles[i] = percentile(evaluatedFitnessValues, (100.0 / this.optimization.getFitnessPrecision()) * (i+1));
            }
            //

            logger.debug("Assign each solution in the population an estimated fitness value.");
            final int finalEstimatedPopulationSize = estimatedPopulationSize;

            estimatedPopulationStream = evaluatedPopulation.stream()
                    .map(phenotype -> phenotype.withFitness((int) estimatedFitnessValues[
                            (int)((double) (evaluatedPopulation.indexOf(phenotype)) / (double) (population.size()) * finalEstimatedPopulationSize)
                            ])).collect(Collectors.toList());


            estimatedPopulation = estimatedPopulationStream.stream()
                    .sorted(Comparator.comparingInt(Phenotype::fitness))
                    .sorted(Comparator.reverseOrder())
                    .toList();

            logger.debug("Assigned estimated fitness values.");
        } else {
            logger.debug("No estimator specified. Using exact fitness (if available).");

            if(this.optimization.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING){
                logger.debug("Running in non-privacy-preserving mode. Exact fitness values available.");
                estimatedPopulation = evaluatedPopulation;
            }
        }
        return  estimatedPopulation;
    }

    @Override
    protected PopulationEvaluation evaluatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep) {
        return evaluatePopulationOrder(population, fitnessEvolutionStep);
    }
}
