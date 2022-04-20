package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import at.jku.dke.slotmachine.privacyEngine.dto.FitnessQuantilesDTO;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Abstract super-class of all Batch-Evaluators
 */
public abstract class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

	protected final JeneticsOptimization optimization; // used to register new solutions
    protected final SlotAllocationProblem problem;

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

        PopulationEvaluation evaluation = evaluatePopulation(population, fitnessEvolutionStep);
        evaluatedPopulation = evaluation.evaluatedPopulation;
        fitnessQuantilesPopulation = evaluation.fitnessQuantilesPopulation;
        maxFitness = evaluation.maxFitness;

        logger.debug("Actual maximum fitness of the population: " + maxFitness);

        minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness) * 0.0001);

        logger.debug("Estimated minimum fitness of the population: " + minFitness);

        estimatedPopulation = estimatePopulation(population, evaluatedPopulation, fitnessEvolutionStep, fitnessQuantilesPopulation, maxFitness, minFitness);

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
     * Takes the evaluated population and configuration and estimates missing fitness-values accordingly
     * @param population the unevaluated population
     * @param evaluatedPopulation the evaluated population
     * @param fitnessEvolutionStep the evolution step of this generation
     * @param fitnessQuantilesPopulation the population mapped to fitness-quantiles
     * @param maxFitness the maximum fitness of the generation
     * @param minFitness the minimum fitness of the generation
     * @return the estimated generation
     */
    protected abstract List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness);

    /**
     * Takes the unevaluated population and returns the evaluation according to the configuration
     * @param population the unevaluated population
     * @param fitnessEvolutionStep the evolution step for this generation
     * @return teh evaluated population
     */
    protected abstract PopulationEvaluation evaluatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep);

    /**
     * Takes the unevaluated population and returns the ordererd candidates and the maximum fitness
     * @param population the unevaluated population
     * @param fitnessEvolutionStep the evolution step of this generation
     * @return the ordered population
     */
    protected PopulationEvaluation evaluatePopulationOrder(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep){
        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        double maxFitness;

        if(this.optimization.getMode() == OptimizationMode.PRIVACY_PRESERVING) {
            logger.debug("Running in privacy-preserving mode: Evaluate the population using the Privacy Engine.");

            logger.debug("Convert population to format required by Privacy Engine.");
            Integer[][] input = this.convertPopulationToArray(population);

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
        }

        PopulationEvaluation evaluation = new PopulationEvaluation();
        evaluation.evaluatedPopulation = evaluatedPopulation;
        evaluation.maxFitness = maxFitness;
        return evaluation;
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

    /**
     * Convert the population from the Jenetics native representation to the array format required by the
     * Privacy Engine.
     * @param population the population in Jenetics representation
     * @return the population in array format required by Privacy Engine
     */
    protected Integer[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
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

    /**
     * Represents the evaluation of a population
     */
    static class PopulationEvaluation{
        protected List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        protected Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation;

        protected double maxFitness;
    }
}
