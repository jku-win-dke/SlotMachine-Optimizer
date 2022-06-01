package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * @param bestGenotype
     * @return the estimated population
     */
    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness, Genotype<EnumGene<Integer>> bestGenotype) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream;

        List<Genotype<EnumGene<Integer>>> evaluatedGenotypes =
                evaluatedPopulation.stream().map(phenotype -> phenotype.genotype()).toList();

        logger.debug("Assign each solution returned by the Privacy Engine the maximum fitness: " + maxFitness);
        estimatedPopulation = population.stream()
                .filter(phenotype -> evaluatedGenotypes.contains(phenotype.genotype()))
                .map(phenotype -> phenotype.withFitness((int)maxFitness))
                .collect(Collectors.toList());

        if(!useActualFitnessValues && maxFitness < this.optimization.getTheoreticalMaximumFitness()){
            estimatedPopulation = estimatedPopulation.stream()
                    .map(phenotype -> phenotype.genotype().equals(bestGenotype) ? phenotype.withFitness( (int) maxFitness + 1) : phenotype)
                    .collect(Collectors.toList());
        }

        while(estimatedPopulation.size() < population.size()){
            estimatedPopulation.addAll(estimatedPopulation);
        }

        estimatedPopulation = estimatedPopulation
                .stream()
                .limit(population.size())
                .collect(Collectors.toList());

        logger.debug("Assigned estimated fitness values.");
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