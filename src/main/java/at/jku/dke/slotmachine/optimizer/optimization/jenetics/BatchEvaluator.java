package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.privacyEngine.dto.FitnessQuantilesDTO;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

	private final JeneticsOptimization optimization; // used to register new solutions
    private final SlotAllocationProblem problem;

    /**
     *
     * @param problem the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluator(SlotAllocationProblem problem, JeneticsOptimization optimization) {
		this.problem = problem;
		this.optimization = optimization;
    }

    /**
     * Takes a population of (unevaluated) candidate solutions and returns a sequence of evaluated solutions.
     * @param population the population of candidate solutions
     * @return an evaluated population
     */
    @Override
    public ISeq<Phenotype<EnumGene<Integer>, Integer>> eval(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        logger.debug("Starting population evaluation ...");

        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;

        double maxFitness;
        double minFitness;

        FitnessEvolutionStep fitnessEvolutionStep = null;

        logger.debug("Number of distinct solutions in population: " + population.stream().distinct().count());

        if(this.optimization.isTraceFitnessEvolution()) {
            fitnessEvolutionStep = new FitnessEvolutionStep();

            logger.debug("Adding fitness evolution to statistics");
            this.optimization.getStatistics().getFitnessEvolution().add(fitnessEvolutionStep);

            Optional<Long> generation = population.stream().map(Phenotype::generation).max(Long::compareTo);

            if(generation.isPresent()) {
                fitnessEvolutionStep.setGeneration(generation.get().intValue());

                logger.debug("Tracing fitness evolution. Generation: " + fitnessEvolutionStep.getGeneration());
            }
        }

        if(this.optimization.getMode() == OptimizationMode.PRIVACY_PRESERVING) {
            logger.debug("Running in privacy-preserving mode: Evaluate the population using the Privacy Engine.");

            logger.debug("Convert population to format required by Privacy Engine.");
            Integer[][] input = this.convertPopulationToArray(population);

            if(this.optimization.getFitnessMethod() == FitnessMethod.FITNESS_RANGE_QUANTILES) {
                logger.debug("Invoke the Privacy Engine service to get fitness quantiles of population.");
                FitnessQuantilesDTO fitnessQuantiles =
                        this.optimization.getPrivacyEngineService().computeFitnessQuantiles(this.optimization, input);

                // TODO convert between Privacy Engine's return format and format required by Optimizer
                evaluatedPopulation = null;

                maxFitness = fitnessQuantiles.getMaximum();
            } else {
                logger.debug("Invoke the Privacy Engine service to evaluate population.");
                PopulationOrderDTO populationOrder =
                        this.optimization.getPrivacyEngineService().computePopulationOrder(this.optimization, input);

                int[] order = populationOrder.getOrder();

                logger.debug("Convert the population order received from the Privacy Engine to the format required by Jenetics.");
                evaluatedPopulation =
                        population.stream()
                                .sorted(Comparator.comparingInt(phenotype -> order[population.indexOf(phenotype)]))
                                .toList();

                maxFitness = populationOrder.getMaximum();
            }
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

            if(this.optimization.getFitnessMethod() == FitnessMethod.FITNESS_RANGE_QUANTILES) {
                double actualMinFitness = evaluatedPopulation.get(evaluatedPopulation.size()-1).fitness();

                double difference = maxFitness - actualMinFitness;

                double windowLength = difference / this.optimization.getFitnessPrecision();

                Map<Integer, List<Phenotype<EnumGene<Integer>, Integer>>> quantilePopulations = evaluatedPopulation.stream()
                        .collect(Collectors.groupingBy(phenotype -> (int) ((maxFitness - (double) phenotype.fitness()) / difference)));

                fitnessQuantilesPopulation = quantilePopulations.values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toMap(
                                        phenotype -> phenotype,
                                        phenotype -> {
                                            Optional<Integer> quantile = quantilePopulations.entrySet().stream()
                                                    .filter(entry -> entry.getValue().contains(phenotype))
                                                    .map(entry -> entry.getKey())
                                                    .findFirst();

                                            return quantile.get();
                                        }
                                )
                        );
            }
        }

        logger.debug("Actual maximum fitness of the population: " + maxFitness);

        minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness) * 0.0001);
        logger.debug("Estimated minimum fitness of the population: " + minFitness);

        Stream<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;

        if(this.optimization.getFitnessEstimator() != null ||
                (this.optimization.getFitnessMethod() != null && this.optimization.getFitnessMethod() != FitnessMethod.ACTUAL_VALUES)) {
            // per default, the estimated population size is the same as the population size
            int estimatedPopulationSize = population.size();

            if(this.optimization.getFitnessMethod() == FitnessMethod.ORDER_QUANTILES ||
                    this.optimization.getFitnessMethod() == FitnessMethod.FITNESS_RANGE_QUANTILES) {
                estimatedPopulationSize = this.optimization.getFitnessPrecision();
                logger.info("Estimated population size: " + estimatedPopulationSize);
            }

            if(this.optimization.getFitnessMethod() == FitnessMethod.ORDER ||
                    this.optimization.getFitnessMethod() == FitnessMethod.ORDER_QUANTILES) {
                logger.debug("Getting estimated fitness value from estimator: " + this.optimization.getFitnessEstimator().getClass());
                double[] estimatedFitnessValues =
                        this.optimization.getFitnessEstimator().estimateFitnessDistribution(estimatedPopulationSize, maxFitness, minFitness);

                logger.debug("Assign each solution in the population an estimated fitness value.");
                final int finalEstimatedPopulationSize = estimatedPopulationSize;

                estimatedPopulationStream = evaluatedPopulation.stream()
                        .map(phenotype -> phenotype.withFitness((int) estimatedFitnessValues[
                                (int)((double) (evaluatedPopulation.indexOf(phenotype)) / (double) (population.size()) * finalEstimatedPopulationSize)
                             ]));
            } else if(this.optimization.getFitnessMethod() == FitnessMethod.FITNESS_RANGE_QUANTILES) {
                // for this we need a change of the Privacy Engine interface

                logger.debug("Getting estimated fitness value from estimator: " + this.optimization.getFitnessEstimator().getClass());
                double[] estimatedFitnessValues =
                        this.optimization.getFitnessEstimator().estimateFitnessDistribution(estimatedPopulationSize, maxFitness, minFitness);

                // assign the estimated fitness of the phenotype's fitness quantile
                final Map<Phenotype<EnumGene<Integer>, Integer>, Integer> finalFitnessQuantilesPopulation = fitnessQuantilesPopulation;
                estimatedPopulationStream = evaluatedPopulation.stream()
                        .map(phenotype -> phenotype.withFitness(
                                (int) estimatedFitnessValues[finalFitnessQuantilesPopulation.get(phenotype)]
                             )
                         );
            } else if(this.optimization.getFitnessMethod() == FitnessMethod.ABOVE_ABSOLUTE_THRESHOLD ||
                        this.optimization.getFitnessMethod() == FitnessMethod.ABOVE_RELATIVE_THRESHOLD) {
                List<Genotype<EnumGene<Integer>>> evaluatedGenotypes =
                        evaluatedPopulation.stream().map(phenotype -> phenotype.genotype()).toList();

                logger.debug("Assign each solution returned by the Privacy Engine the maximum fitness: " + maxFitness);
                estimatedPopulationStream = population.stream()
                        .map(phenotype ->
                                evaluatedGenotypes.contains(phenotype.genotype())?
                                        phenotype.withFitness((int) maxFitness) :
                                        phenotype.withFitness((int) minFitness)
                        );
            }

            estimatedPopulation = estimatedPopulationStream
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

        if(fitnessEvolutionStep != null) {
            fitnessEvolutionStep.setEstimatedPopulation(
                    estimatedPopulation.stream()
                            .map(phenotype -> (double) phenotype.fitness())
                            .toArray(Double[]::new)
            );
            logger.debug("Tracing fitness evolution. Size of estimated population: " + fitnessEvolutionStep.getEstimatedPopulation().length);
        }

        if(maxFitness >= this.optimization.getMaximumFitness() && estimatedPopulation != null) {
            logger.debug("Best fitness of current generation better than current best fitness. Attaching intermediate result to the optimization run.");
            this.optimization.setResults(
                    estimatedPopulation.stream().distinct().map(phenotype -> this.problem.decode(phenotype.genotype())).toList()
            );

            // set the optimization's maximum fitness to this generation's maximum fitness
            this.optimization.setMaximumFitness(maxFitness);
        }

        logger.debug("Finished evaluation");

        logger.debug("Update statistics.");
        this.optimization.getStatistics().setFitnessFunctionInvocations(problem.getFitnessFunctionApplications());
        this.optimization.getStatistics().setResultFitness(this.optimization.getMaximumFitness());

        return ISeq.of(estimatedPopulation);
    }

    /**
     * Convert the population from the Jenetics native representation to the array format required by the
     * Privacy Engine.
     * @param population the population in Jenetics representation
     * @return the population in array format required by Privacy Engine
     */
    private Integer[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        return population.stream()
                  .map(phenotype -> this.problem.decode(phenotype.genotype()))
                  .map(map ->
                       // 1. Get a flight list from the mapping of flights to slots, where the flights are
                       // ordered by their assigned time slot.
                       // 2. Replace the flights by their position in the problem's sequence of flights.
                       map.entrySet().stream()
                          .sorted(Entry.comparingByValue())
                          .map(Entry::getKey)
                          .map(flight -> this.problem.getFlights().indexOf(flight))
                          .toArray(Integer[]::new)
                  ).toArray(Integer[][]::new);
    }
}
