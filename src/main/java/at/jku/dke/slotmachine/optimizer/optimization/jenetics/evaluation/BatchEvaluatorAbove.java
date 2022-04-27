package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract super class for fitness-methods that estimate a population based on a threshold
 */
public abstract class BatchEvaluatorAbove extends BatchEvaluator {
    private static final Logger logger = LogManager.getLogger();

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAbove(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    /**
     * Estimates the population by assigning the maximum fitness to each evaluated phenotype.
     * All phenotypes that are not evaluated are assigned the minimum fitness.
     *
     * @param population the unevaluated population
     * @param evaluatedPopulation the evaluated population
     * @param fitnessEvolutionStep the evolution step of this generation
     * @param fitnessQuantilesPopulation the population mapped to fitness-quantiles
     * @param maxFitness the maximum fitness of the generation
     * @param minFitness the minimum fitness of the generation
     * @return the estimated population
     */
    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;
        //TODO: remove condition as no estimator required
        if(this.optimization.getFitnessEstimator() != null) {
            List<Genotype<EnumGene<Integer>>> evaluatedGenotypes =
                    evaluatedPopulation.stream().map(phenotype -> phenotype.genotype()).toList();

            logger.debug("Assign each solution returned by the Privacy Engine the maximum fitness: " + maxFitness);
            estimatedPopulationStream = population.stream()
                    .map(phenotype ->
                            evaluatedGenotypes.contains(phenotype.genotype())?
                                    phenotype.withFitness((int) maxFitness) :
                                    phenotype.withFitness((int) minFitness)
                    ).collect(Collectors.toList());

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

    /**
     * Evaluates all phenotypes according to the threshold
     *
     * @param population the unevaluated population
     * @param fitnessEvolutionStep the evolution step for this generation
     * @return the evaluated population
     */
    @Override
    protected PopulationEvaluation evaluatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep) {
        // TODO: adjust for non privacy-preserving mode
        PopulationEvaluation evaluation = evaluatePopulationOrder(population, fitnessEvolutionStep);
        double threshold = getThreshold(evaluation);
        evaluation.evaluatedPopulation = evaluation.evaluatedPopulation.stream().filter(phenotype -> phenotype.fitness() >= threshold).toList();
        return evaluation;
    }

    /**
     * Returns the threshold for the evaluation
     * @param evaluation the evaluated population
     * @return the threshold
     */
    protected abstract double getThreshold(PopulationEvaluation evaluation);

}