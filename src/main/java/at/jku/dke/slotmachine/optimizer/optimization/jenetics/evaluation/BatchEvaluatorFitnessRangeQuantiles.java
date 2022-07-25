package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import at.jku.dke.slotmachine.privacyEngine.dto.FitnessQuantilesDTO;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BatchEvaluator for the fitness-method FITNESS_RANGE_QUANTILES
 */
public class BatchEvaluatorFitnessRangeQuantiles extends BatchEvaluator{
    private static final Logger logger = LogManager.getLogger();

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorFitnessRangeQuantiles(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness, Genotype<EnumGene<Integer>> bestGenotype) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;

        if(this.optimization.getFitnessEstimator() != null) {
            int estimatedPopulationSize = this.optimization.getFitnessPrecision();
            logger.debug("Estimated population size: " + estimatedPopulationSize);

            // for this we probably need a change of the Privacy Engine interface when running in privacy-preserving mode
            logger.debug("Getting estimated fitness value from estimator: " + this.optimization.getFitnessEstimator().getClass());
            double[] estimatedFitnessValues =
                    this.optimization.getFitnessEstimator().estimateFitnessDistribution(estimatedPopulationSize, maxFitness, minFitness);

            logger.debug("Assign the estimated fitness of the phenotype's fitness quantile");
            final Map<Phenotype<EnumGene<Integer>, Integer>, Integer> finalFitnessQuantilesPopulation = fitnessQuantilesPopulation;
            estimatedPopulationStream = evaluatedPopulation.stream()
                    .map(phenotype -> {
                                int fitness = (int) estimatedFitnessValues[finalFitnessQuantilesPopulation.get(phenotype)];
                                return phenotype.withFitness(fitness);
                            }
                    ).collect(Collectors.toList());
            logger.debug("Assigned the fitness quantiles");

            estimatedPopulation = estimatedPopulationStream.stream()
                    .sorted(Comparator.comparingInt(Phenotype::fitness))
                    .sorted(Comparator.reverseOrder())
                    .toList();

            if(!useActualFitnessValues && maxFitness < this.optimization.getTheoreticalMaximumFitness()){
                estimatedPopulation = estimatedPopulation.stream()
                        .map(phenotype -> phenotype.genotype().equals(bestGenotype) ? phenotype.withFitness( (int) maxFitness + 1) : phenotype)
                        .sorted(Comparator.comparingInt(Phenotype::fitness))
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
            }

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
        return evaluatePopulationFitnessQuantiles(population, fitnessEvolutionStep);
    }


    /**
     * Takes the unevaluated population and assigns them to fitness-range-quantiles according to the fitness-precision
     * @param population the unevaluated population
     * @param fitnessEvolutionStep the evolution step of this generation
     * @return the mapping of the candidates to the fitness-range-quantiles
     */
    protected PopulationEvaluation evaluatePopulationFitnessQuantiles(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep){
        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation = null;
        Genotype<EnumGene<Integer>> bestGenotype = null;
        double maxFitness;

        if(this.optimization.getMode() == OptimizationMode.PRIVACY_PRESERVING) {
            logger.debug("Running in privacy-preserving mode: Evaluate the population using the Privacy Engine.");

            logger.debug("Convert population to format required by Privacy Engine.");
            Integer[][] input = this.convertPopulationToArray(population);

            logger.debug("Invoke the Privacy Engine service to get fitness quantiles of population.");
            FitnessQuantilesDTO fitnessQuantiles =
                    this.optimization.getPrivacyEngineService().computeFitnessQuantiles(this.optimization, input);

            // TODO convert between Privacy Engine's return format and format required by Optimizer
            evaluatedPopulation = null;

            maxFitness = fitnessQuantiles.getMaximum();

        } else {
            logger.debug("Running in non-privacy-preserving mode: Evaluate the population using the submitted weights.");
            evaluatedPopulation =
                    population.stream()
                            .map(phenotype -> phenotype.withFitness(problem.fitness(phenotype.genotype())))
                            .sorted(Comparator.comparingInt(Phenotype::fitness))
                            .sorted(Comparator.reverseOrder())
                            .toList();

            maxFitness = evaluatedPopulation.get(0).fitness();
            bestGenotype = evaluatedPopulation.get(0).genotype();

            if(!useActualFitnessValues && maxFitness < this.optimization.getTheoreticalMaximumFitness()){
                maxFitness = population.size();

                // Check if fitness has improved compared to current maximum.
                boolean isMaxFitnessIncreased = evaluatedPopulation.get(0).fitness() > actualMaxFitness;

                // Override max fitness used for estimation/optimization with dummy-value.
                if(isMaxFitnessIncreased && noGenerations > 1){
                    actualMaxFitness = evaluatedPopulation.get(0).fitness();

                    // Add increment to dummy maxFitness to indicate improvement.
                    maxFitness = maxFitness + fitnessIncrement;
                    fitnessIncrement ++;
                }
            }

            logger.debug("Actual minimum fitness of the population: " + evaluatedPopulation.get(evaluatedPopulation.size() - 1).fitness());

            if(fitnessEvolutionStep != null) {
                fitnessEvolutionStep.setEvaluatedPopulation(
                        evaluatedPopulation.stream().map(phenotype -> (double) phenotype.fitness()).toArray(Double[]::new)
                );
                logger.debug("Tracing fitness evolution. Size of evaluated population: " + fitnessEvolutionStep.getEvaluatedPopulation().length);
            }

            double actualMinFitness = evaluatedPopulation.get(evaluatedPopulation.size()-1).fitness();

            double difference = evaluatedPopulation.get(0).fitness() - actualMinFitness;

            double windowLength = (difference / this.optimization.getFitnessPrecision()) + 0.01;

            logger.debug("Diff: " + difference + ", windowLength: " + windowLength);

            Map<Integer, List<Phenotype<EnumGene<Integer>, Integer>>> quantilePopulations = evaluatedPopulation.stream()
                    .collect(Collectors.groupingBy(phenotype -> (int) ((evaluatedPopulation.get(0).fitness() - (double) phenotype.fitness()) / windowLength)));

            logger.debug("Map phenotype to quantile");
            fitnessQuantilesPopulation = new HashMap<>();

            for(int quantile : quantilePopulations.keySet()) {
                List<Phenotype<EnumGene<Integer>, Integer>> quantilePopulation = quantilePopulations.get(quantile);

                for(Phenotype<EnumGene<Integer>, Integer> phenotype : quantilePopulation) {
                    fitnessQuantilesPopulation.put(phenotype, quantile);
                }
            }

            logger.debug("Mapped phenotypes to quantile");
        }

        PopulationEvaluation evaluation = new PopulationEvaluation();
        evaluation.evaluatedPopulation = evaluatedPopulation;
        evaluation.fitnessQuantilesPopulation = fitnessQuantilesPopulation;
        evaluation.bestGenotype = bestGenotype;
        evaluation.maxFitness = maxFitness;
        return evaluation;
    }
}
