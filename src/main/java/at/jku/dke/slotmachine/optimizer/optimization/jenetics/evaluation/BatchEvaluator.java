package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

/**
 * Abstract super-class of all batch evaluators
 */
public abstract class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();
    private static final int DEVALUATOR = -10000000;

    protected final JeneticsOptimization optimization; // used to register new solutions
    protected final SlotAllocationProblem problem;
    /**
     * If true, evaluation is only triggered for generations encountered for the second time, or if no duplicates are present in the population.
     */
    protected final boolean isDeduplicate;
    /**
     * If true, duplicate statistics are gathered, even if isDeduplicate is false.
     */
    protected final boolean trackDuplicates;
    /**
     * If true, the actual fitness values are used. Otherwise, the fitness is obfuscated for the GA.
     */
    protected final boolean useActualFitnessValues;

    /**
     * Used in NON_PRIVACY_PRESERVING mode when useActualFitnessValues is false, to verify if fitness has been improved in a given generation.
     */
    protected long actualMaxFitness;
    /**
     * Used in conjunction with useActualFitnessValues = false and holds the current increment of the obfuscated base fitness value.
     */
    protected long fitnessIncrement;

    /**
     * Fields for deduplicate and duplicate-statistics.
     */
    protected long latestUnevaluatedGeneration;
    protected long noGenerationsUnevaluated;
    protected long noInitialDuplicates;
    protected long noRemainingDuplicates;
    protected long noGenerations;
    protected long noGenerationsDuplicatesNotEliminated;
    protected long noGenerationsEvaluated;

    /**
     * Devaluation statistics
     */
    protected long noPhenotypes;
    protected long noInvalidPhenotypes;
    protected long noInvalidAssignments;

    /**
     *
     * @param problem the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluator(SlotAllocationProblem problem, JeneticsOptimization optimization) {
		this.problem = problem;
		this.optimization = optimization;
        this.isDeduplicate = optimization.getConfiguration().isDeduplicate();
        this.noGenerations = 0;
        this.noGenerationsUnevaluated = 0;
        this.noInitialDuplicates = 0;
        this.noRemainingDuplicates = 0;
        this.noGenerationsDuplicatesNotEliminated = 0;
        this.actualMaxFitness = Integer.MIN_VALUE;
        this.fitnessIncrement = 1;
        this.noPhenotypes = 0;
        this.noInvalidAssignments = 0;
        this.noInvalidPhenotypes = 0;

        // Configuration

        this.trackDuplicates = Boolean.parseBoolean(System.getenv("TRACK_DUPLICATES"));
        this.useActualFitnessValues = Boolean.parseBoolean(System.getenv("USE_ACTUAL_FITNESS"))
                || this.optimization.getFitnessMethod() == FitnessMethod.ACTUAL_VALUES;
        logger.info("Using actual fitness values: {}.", useActualFitnessValues);
    }

    /**
     * Takes a population of (unevaluated) candidate solutions and returns a sequence of evaluated solutions.
     * @param population the population of candidate solutions
     * @return an evaluated population
     */
    @Override
    public ISeq<Phenotype<EnumGene<Integer>, Integer>> eval(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        logger.debug("Starting population evaluation ...");
        this.noGenerations++;
        Optional<Long> generation = population.stream().map(Phenotype::generation).max(Long::compareTo);
        if(isDeduplicate){
            if(generation.get() != latestUnevaluatedGeneration || trackDuplicates){
                logger.debug("Checking for duplicates.");
                final Map<Genotype<EnumGene<Integer>>, Phenotype<EnumGene<Integer>, Integer>> elements =
                        population.stream()
                                .collect(toMap(
                                        Phenotype::genotype,
                                        Function.identity(),
                                        (a, b) -> a));

                if(elements.size() < population.size() && generation.get() != latestUnevaluatedGeneration){
                    logger.debug("Generation " + generation.get() + " contains duplicates and is encountered for the first time.");
                    logger.debug("Returning unevaluated population with dummy fitness-values.");
                    this.noGenerationsUnevaluated++;
                    this.noInitialDuplicates = noInitialDuplicates + population.size() - elements.size();

                    latestUnevaluatedGeneration = generation.get();
                    return ISeq.of(population
                            .stream()
                            .map(p -> p.withFitness(-1))
                            .collect(Collectors.toList()));
                }
                this.noRemainingDuplicates = noRemainingDuplicates + population.size() - elements.size();
                if(elements.size() < population.size()) this.noGenerationsDuplicatesNotEliminated++;
            }
        }
        noGenerationsEvaluated++;


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

        minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness) * 0.0001); // 0.0001 to avoid division by zero when calculating delta in linear estimator

        logger.debug("Estimated minimum fitness of the population: " + minFitness);

        estimatedPopulation = estimatePopulation(population, evaluatedPopulation, fitnessEvolutionStep, fitnessQuantilesPopulation, maxFitness, minFitness, evaluation.bestGenotype);

        if(fitnessEvolutionStep != null) {
            fitnessEvolutionStep.setEstimatedPopulation(
                    estimatedPopulation.stream()
                            .map(phenotype -> (double) phenotype.fitness())
                            .toArray(Double[]::new)
            );
            logger.debug("Size of estimated population: " + fitnessEvolutionStep.getEstimatedPopulation().length);
        }

        logger.debug("Devaluing invalid solutions.");
        estimatedPopulation = estimatedPopulation.stream().map(p -> {
            Map<Flight, Slot> phenotypeMap = this.problem.decode(p.genotype()); // decode phenotype
            this.noPhenotypes++;
            long invalidAssignments = // determine how many invalid assignments the phenotype has
                    phenotypeMap.entrySet().stream().filter(e ->
                            e.getKey().getScheduledTime() != null &&
                                    e.getKey().getScheduledTime().isAfter(e.getValue().getTime())).count();

            Phenotype<EnumGene<Integer>, Integer> phenotype = p;

            // if there are violations of the constraint, devalue the individual accordingly
            if(invalidAssignments > 0) {
                this.noInvalidPhenotypes++;
                this.noInvalidAssignments += invalidAssignments;
                phenotype = p.withFitness((int) invalidAssignments * DEVALUATOR);
            }

            return phenotype;
        }).collect(Collectors.toList());

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
     * @param bestGenotype the genotype with the highest fitness according to the evaluation
     * @return the estimated generation
     */
    protected abstract List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness, Genotype<EnumGene<Integer>> bestGenotype);

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
        List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        Genotype<EnumGene<Integer>> bestGenotype = null;
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
                    Arrays.stream(order).mapToObj(population::get)
                            .collect(Collectors.toList());

            maxFitness = populationOrder.getMaximum();
            logger.debug("Maximum fitness in generation according to Privacy Engine is " + maxFitness + ".");
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

                // Override max fitness used for estimation/optimization with dummy-value (first Evaluation (noGenerations == 1) is ignored by Jenetics)
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
        }

        PopulationEvaluation evaluation = new PopulationEvaluation();
        evaluation.evaluatedPopulation = evaluatedPopulation;
        evaluation.maxFitness = maxFitness;
        evaluation.bestGenotype = bestGenotype;
        return evaluation;
    }

    public void printLogs(){
        logger.info("--------------- Statistics Batch Evaluator --------------------");
        logger.info("Deduplication: " + this.isDeduplicate + ".");
        logger.info("Tracking duplicates " + this.trackDuplicates + ".");
        logger.info("Number of populations that entered evaluation: "+ this.noGenerations);
        logger.info("Number of populations that have been evaluated: " + this.noGenerationsEvaluated);
        logger.info("Number of populations that have been rejected because of duplicates: " + this.noGenerationsUnevaluated);
        logger.info("Number of initial duplicates encountered: " + this.noInitialDuplicates);
        logger.info("Number of remaining duplicates after deduplication: " + this.noRemainingDuplicates);
        logger.info("Number of phenotypes checked for validness: " + this.noPhenotypes);
        logger.info("Number of invalid phenotypes found: " + this.noInvalidPhenotypes);
        logger.info("Number of invalid assignments: " + this.noInvalidAssignments);
        logger.info("----------------------------------------------------------------");
    }

    /**
     * Convert the population from the Jenetics native representation to the array format required by the
     * Privacy Engine.
     * @param population the population in Jenetics representation
     * @return the population in array format required by Privacy Engine
     */
    public Integer[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        return population.asList().stream()
                  .map(phenotype -> this.problem.decode(phenotype.genotype()))
                  .map(map ->{
                              // 1. Get the slots ordered ascending by time
                              // 2. Replace the flights in the flight sequence by the index of their assigned slot
                              var orderedSlots = map.entrySet().stream().sorted(Entry.comparingByValue()).map(Entry::getValue).toList();
                              return this.problem.getFlights().stream().map(flight -> orderedSlots.indexOf(map.get(flight))).toArray(Integer[]::new);
                          }
                  ).toArray(Integer[][]::new);
    }


    /**
     * Represents the evaluation of a population
     */
    static class PopulationEvaluation{
        protected List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        protected Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation;
        protected Genotype<EnumGene<Integer>> bestGenotype;
        protected double maxFitness;
    }
}
