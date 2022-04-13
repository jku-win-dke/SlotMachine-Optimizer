package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import at.jku.dke.slotmachine.privacyEngine.dto.FitnessQuantilesDTO;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
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

public class BatchEvaluatorFRQ extends BatchEvaluator{
    private static final Logger logger = LogManager.getLogger();

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorFRQ(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;

        if(this.optimization.getFitnessEstimator() != null) {
            // per default, the estimated population size is the same as the population size
            int estimatedPopulationSize = population.size();

            estimatedPopulationSize = this.optimization.getFitnessPrecision();
            logger.debug("Estimated population size: " + estimatedPopulationSize);

            // for this we need a change of the Privacy Engine interface

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
        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation = null;

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

            logger.debug("Actual minimum fitness of the population: " + evaluatedPopulation.get(evaluatedPopulation.size() - 1).fitness());

            if(fitnessEvolutionStep != null) {
                fitnessEvolutionStep.setEvaluatedPopulation(
                        evaluatedPopulation.stream().map(phenotype -> (double) phenotype.fitness()).toArray(Double[]::new)
                );
                logger.debug("Tracing fitness evolution. Size of evaluated population: " + fitnessEvolutionStep.getEvaluatedPopulation().length);
            }

            double actualMinFitness = evaluatedPopulation.get(evaluatedPopulation.size()-1).fitness();

            double difference = maxFitness - actualMinFitness;

            double windowLength = (difference / this.optimization.getFitnessPrecision()) + 0.01;

            logger.debug("Diff: " + difference + ", windowLength: " + windowLength);

            Map<Integer, List<Phenotype<EnumGene<Integer>, Integer>>> quantilePopulations = evaluatedPopulation.stream()
                    .collect(Collectors.groupingBy(phenotype -> (int) ((maxFitness - (double) phenotype.fitness()) / windowLength)));

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
        evaluation.maxFitness = maxFitness;
        return evaluation;
    }
}
