package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import at.jku.dke.slotmachine.privacyEngine.dto.AboveIndividualsDTO;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
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
     * @param bestGenotype the genotype with the highest fitness in this iteration
     * @return the estimated population
     */
    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness, Genotype<EnumGene<Integer>> bestGenotype) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation;

        List<Genotype<EnumGene<Integer>>> evaluatedGenotypes =
                evaluatedPopulation.stream().map(phenotype -> phenotype.genotype()).toList();

        logger.debug("Assign each solution returned by the Privacy Engine the maximum fitness: " + maxFitness);
        // Add all evaluated individuals to the estimated population with the max fitness
        estimatedPopulation = population.stream()
                .filter(phenotype -> evaluatedGenotypes.contains(phenotype.genotype()))
                .map(phenotype -> phenotype.withFitness((int)maxFitness))
                .collect(Collectors.toList());

        // Increase the fitness of the best genotype in the population if possible to improve selection process
        if(bestGenotype != null && maxFitness < this.optimization.getTheoreticalMaximumFitness()){
            logger.debug("Assigning higher fitness value to best genotype(s).");
            estimatedPopulation = estimatedPopulation.stream()
                    .map(phenotype -> phenotype.genotype().equals(bestGenotype) ? phenotype.withFitness( (int) maxFitness + 1) : phenotype)
                    .collect(Collectors.toList());
        }

        // Add estimated individuals to the collection until the size of the estimated population equals the size of the population (Jenetics requirement)
        while(estimatedPopulation.size() < population.size()){
            estimatedPopulation.addAll(estimatedPopulation);
        }

        estimatedPopulation = estimatedPopulation
                .stream()
                .limit(population.size())
                .sorted(Comparator.comparingInt(Phenotype::fitness))
                .sorted(Comparator.reverseOrder())
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
        return evaluatePopulationAbove(population, fitnessEvolutionStep);
    }

    /**
     * Takes the unevaluated population and returns all individuals exceeding a defined threshold
     * @param population the unevaluated population
     * @param fitnessEvolutionStep the evolution step of this generation
     * @return the evaluated population
     */
    protected PopulationEvaluation evaluatePopulationAbove(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep){
        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        Genotype<EnumGene<Integer>> bestGenotype = null;
        PopulationEvaluation evaluation = null;
        double maxFitness;

        if(this.optimization.getMode() == OptimizationMode.PRIVACY_PRESERVING) {
            logger.debug("Running in privacy-preserving mode: Evaluate the population using the Privacy Engine.");

            logger.debug("Convert population to format required by Privacy Engine.");
            Integer[][] input = this.convertPopulationToArray(population);

            logger.debug("Invoke the Privacy Engine service to get phenotypes exceeding threshold.");
            // TODO: distinguish ABOVE from TOP
            AboveIndividualsDTO individualsAbove =
                    this.optimization.getPrivacyEngineService().computeIndividualsAbove(this.optimization, input);

            // convert between Privacy Engine's return format and format required by Optimizer
            logger.debug("Convert returned population to format required by Jenetics.");
            logger.debug(" Using size of population as base for the maximum fitness in generation.");
            logger.debug("Returned index of best genotype is {}",  (individualsAbove.getHighest() != null ? individualsAbove.getHighest() : "NULL") + ".");
            logger.debug("Has max fitness improved: {}",  (individualsAbove.getBest() != null ? individualsAbove.getBest() : "NULL") + ".");


            bestGenotype = population.get(individualsAbove.getHighest()).genotype();
            maxFitness = population.size();

            if(Boolean.TRUE.equals(individualsAbove.getBest())){
                maxFitness += this.fitnessIncrement;
                this.fitnessIncrement++;
                logger.debug("Increased max fitness to {}{}",  maxFitness, ".");
            }

            evaluatedPopulation = Arrays.stream(individualsAbove.getIndices())
                    .map(population::get)
                    .toList();

            evaluation = new PopulationEvaluation();
            evaluation.evaluatedPopulation = evaluatedPopulation;
            evaluation.bestGenotype = bestGenotype;
            evaluation.maxFitness = maxFitness;
        }else{
            evaluation = evaluatePopulationOrder(population, fitnessEvolutionStep);
            double threshold = getThreshold(evaluation);
            evaluation.evaluatedPopulation = evaluation.evaluatedPopulation.stream().filter(phenotype -> phenotype.fitness() >= threshold).toList();
        }

        return evaluation;
    }
    /**
     * Returns the threshold for the evaluation
     * @param evaluation the evaluated population
     * @return the threshold
     */
    protected abstract double getThreshold(PopulationEvaluation evaluation);

}