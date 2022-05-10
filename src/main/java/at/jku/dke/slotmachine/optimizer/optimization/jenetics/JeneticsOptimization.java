package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation.BatchEvaluator;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation.BatchEvaluatorFactory;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JeneticsOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private JeneticsOptimizationConfiguration configuration = null;
    private JeneticsOptimizationStatistics statistics;
    private final SlotAllocationProblem problem;

    private List<Integer> fitnessValuesResults;

    public JeneticsOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);

        this.statistics = new JeneticsOptimizationStatistics();

        this.problem = new SlotAllocationProblem(
                ISeq.of(this.getFlights()),
                ISeq.of(this.getSlots())
        );
        logger.info("Slot allocation problem initialized.");
    }

    @Override
    public Map<Flight, Slot> run() {
        int populationSize;
        Mutator<EnumGene<Integer>, Integer> mutator;
        Crossover<EnumGene<Integer>, Integer> crossover;
        Selector<EnumGene<Integer>, Integer> offspringSelector;
        Selector<EnumGene<Integer>, Integer> survivorsSelector;
        int maximalPhenotypeAge;
        double offspringFraction;
        ISeq<Genotype<EnumGene<Integer>>> initialPopulation;
        Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>[] terminationConditions;

        if(this.getConfiguration() != null) {
            populationSize = this.getConfiguration().getPopulationSize();
            if (populationSize < 0) {
                populationSize = this.getDefaultConfiguration().getPopulationSize();
            }

            mutator = this.getConfiguration().getMutator();
            if (mutator == null) {
                mutator = this.getDefaultConfiguration().getMutator();
            }

            crossover = this.getConfiguration().getCrossover();
            if (crossover == null) {
                crossover = this.getDefaultConfiguration().getCrossover();
            }

            offspringSelector = this.getConfiguration().getOffspringSelector();
            if (offspringSelector == null) {
                offspringSelector = this.getDefaultConfiguration().getOffspringSelector();
            }

            survivorsSelector = this.getConfiguration().getSurvivorsSelector();
            if (survivorsSelector == null) {
                survivorsSelector = this.getDefaultConfiguration().getSurvivorsSelector();
            }

            maximalPhenotypeAge = this.getConfiguration().getMaximalPhenotypeAge();
            if (maximalPhenotypeAge < 0) {
                maximalPhenotypeAge = this.getDefaultConfiguration().getMaximalPhenotypeAge();
            }

            offspringFraction = this.getConfiguration().getOffspringFraction();
            if (offspringFraction < 0) {
                offspringFraction = this.getDefaultConfiguration().getOffspringFraction();
            }

            initialPopulation = this.getConfiguration().getInitialPopulation(problem, populationSize);
            if(initialPopulation == null) {
                initialPopulation = this.getDefaultConfiguration().getInitialPopulation(problem, populationSize);
            }

            terminationConditions = this.getConfiguration().getTerminationConditions();
            if(terminationConditions == null) {
                terminationConditions = this.getDefaultConfiguration().getTerminationConditions();
            }
        } else {
            populationSize = this.getDefaultConfiguration().getPopulationSize();
            mutator = this.getDefaultConfiguration().getMutator();
            crossover = this.getDefaultConfiguration().getCrossover();
            offspringSelector = this.getDefaultConfiguration().getOffspringSelector();
            survivorsSelector = this.getDefaultConfiguration().getSurvivorsSelector();
            maximalPhenotypeAge = this.getDefaultConfiguration().getMaximalPhenotypeAge();
            offspringFraction = this.getDefaultConfiguration().getOffspringFraction();
            initialPopulation = this.getDefaultConfiguration().getInitialPopulation(problem, populationSize);
            terminationConditions = this.getDefaultConfiguration().getTerminationConditions();
        }

        if(this.statistics.getFitnessEvolution() != null)  {
            this.statistics.getFitnessEvolution().clear();
            logger.info("Cleared fitness evolution.");
        }

        int initialFitness = -1;
        if(this.getInitialFlightSequence() != null && this.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING){
            logger.info("Running in non-privacy-preserving mode.");
            logger.info("Calculating initial fitness based on initial flight sequence.");
            Map<Flight, Slot> initialFlightSequenceMap = new HashMap<>();
            for(int i = 0; i < getInitialFlightSequence().length; i++){
                Flight flight = null;
                for(int j = 0; j < getFlights().length; j++){
                    if(getFlights()[j].getFlightId().equals(getInitialFlightSequence()[i])) flight = getFlights()[j];
                }
                Slot slot = getSlots()[i];
                initialFlightSequenceMap.put(flight, slot);

            }
            initialFitness = problem.fitness(initialFlightSequenceMap);
        }
        this.getStatistics().setInitialFitness(initialFitness);

        logger.info("Initial population consists of " + initialPopulation.length() + " individuals.");
        logger.info("Initial population consists of " + initialPopulation.stream().distinct().toList().size() + " distinct individuals.");

        logger.info("Build the genetic algorithm engine.");

        // Default deduplicate for SRDS experiments
        //this.getConfiguration().setDeduplicate(true);
        //this.getConfiguration().setDeduplicateMaxRetries(10);

        Evaluator evaluator = BatchEvaluatorFactory.getEvaluator(getFitnessMethod(), problem, this);

        Engine.Builder<EnumGene<Integer>, Integer> builder;

        builder = new Engine.Builder<>(evaluator, problem.codec().encoding());

        // builder = Engine.builder(problem);


        if(this.getConfiguration().isDeduplicate()){
            int maxRetries = this.getConfiguration().getDeduplicateMaxRetries();

            if (maxRetries > 0) {
                logger.debug("The engine should deduplicate the population; maxRetries: " + maxRetries);
                builder = builder.interceptor(EvolutionResult.toUniquePopulation(maxRetries));
            } else {
                logger.debug("The engine should deduplicate the population");
                builder = builder.interceptor(EvolutionResult.toUniquePopulation());
            }
        }

        Engine<EnumGene<Integer>, Integer> engine = builder
                .optimize(Optimize.MAXIMUM)
                .populationSize(populationSize)
                .alterers(mutator, crossover)
                .offspringSelector(offspringSelector)
                .survivorsSelector(survivorsSelector)
                .maximalPhenotypeAge(maximalPhenotypeAge)
                .offspringFraction(offspringFraction)
                .constraint(problem.constraint().isPresent()?problem.constraint().get():null)
                .build();

        logger.info("Engine population size: " + engine.populationSize());

        EvolutionStatistics <Integer, ?> statistics = EvolutionStatistics.ofNumber();

        logger.info("Running optimization using Jenetics framework as slot allocation problem ...");

        EvolutionStream<EnumGene<Integer>, Integer> stream = engine.stream(initialPopulation);

        for(Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>> terminationCondition: terminationConditions) {
            stream = stream.limit(terminationCondition);
        }

        logger.info("Current thread: " + Thread.currentThread());

        // add a termination condition that truncates the result if the current thread was interrupted
        stream = stream.limit(result -> !Thread.currentThread().isInterrupted());

        this.getStatistics().setTimeStarted(LocalDateTime.now()); // set the begin time in the statistics

        EvolutionResult<EnumGene<Integer>, Integer> result = stream
                .peek(statistics)
                .collect(EvolutionResult.toBestEvolutionResult());


        logger.info("Finished optimization");

        logger.info(Thread.currentThread() + " was interrupted: " + Thread.currentThread().isInterrupted());

        if(this.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING){
            logger.info("Running in non-privacy-preserving mode.");
            logger.info("Evaluating result population with exact fitness values.");
            var evaluatedResultGeneration = result.population()
                    .stream()
                    .map(phenotype -> phenotype.withFitness(problem.fitness(phenotype.genotype())))
                    .sorted(Comparator.comparingInt(Phenotype::fitness))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            logger.info("Setting evaluated population as result.");
            result = EvolutionResult.of(Optimize.MAXIMUM,
                    ISeq.of(evaluatedResultGeneration),
                    result.generation(),
                    result.totalGenerations(),
                    result.durations(),
                    result.killCount(),
                    result.invalidCount(),
                    result.alterCount());

            logger.info("Setting fitness values of distinct, evaluated population.");
            var distinctIndividualFitnessValues = result.population()
                    .stream()
                    .map(Phenotype::genotype)
                    .distinct()
                    .map(problem::fitness)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            this.setFitnessValuesResults(distinctIndividualFitnessValues);
        }

        Map<Flight, Slot> resultMap = problem.decode(result.bestPhenotype().genotype());

        logger.info("Statistics: \n" + statistics);
        logger.info("Printing statistics from BatchEvaluator");
        if(evaluator instanceof BatchEvaluator batchEvaluator) batchEvaluator.printLogs();

        logger.info("Setting statistics for this optimization."); // already initialized in constructor
        this.getStatistics().setTimeFinished(LocalDateTime.now());
        this.getStatistics().setResultFitness(problem.fitness(result.bestPhenotype().genotype()));
        this.getStatistics().setIterations((int) statistics.altered().count());
        this.getStatistics().setFitnessFunctionInvocations(problem.getFitnessFunctionApplications());
        this.getStatistics().setSolutionGeneration(result.bestPhenotype().generation());
        //this.getStatistics().setInitialFitness();

        logger.info("Fitness of best solution: " + this.getStatistics().getResultFitness());
        logger.info("Number of generations: " + this.getStatistics().getIterations());
        logger.info("Number of fitness function invocations: " + this.getStatistics().getFitnessFunctionInvocations());
        logger.info("Generation of best solution: " + this.getStatistics().getSolutionGeneration());

        // set the results
        List<Map<Flight, Slot>> resultList =
                result.population().stream()
                        .sorted(Comparator.comparingInt(Phenotype::fitness))
                        .sorted(Comparator.reverseOrder())
                        .map(phenotype -> phenotype.genotype())
                        .distinct()
                        .map(genotype -> problem.decode(genotype))
                        .toList();



        this.setResults(resultList);

        // return only the best result
        return resultMap;
    }

    @Override
    public JeneticsOptimizationConfiguration getDefaultConfiguration() {
        JeneticsOptimizationConfiguration defaultConfiguration = new JeneticsOptimizationConfiguration();

        Map<String, Object> terminationConditionParameters = new HashMap<>();
        terminationConditionParameters.put("BY_EXECUTION_TIME", 60);

        defaultConfiguration.setMaximalPhenotypeAge(70);
        defaultConfiguration.setPopulationSize(50);
        defaultConfiguration.setOffspringFraction(0.6);
        defaultConfiguration.setMutator("SWAP_MUTATOR");
        defaultConfiguration.setMutatorAlterProbability(0.2);
        defaultConfiguration.setCrossover("PARTIALLY_MATCHED_CROSSOVER");
        defaultConfiguration.setCrossoverAlterProbability(0.35);
        defaultConfiguration.setSurvivorsSelector("TOURNAMENT_SELECTOR");
        defaultConfiguration.setOffspringSelector("TOURNAMENT_SELECTOR");
        defaultConfiguration.setTerminationConditions(terminationConditionParameters);
        defaultConfiguration.setDeduplicate(false);

        return defaultConfiguration;
    }

    @Override
    public JeneticsOptimizationConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        JeneticsOptimizationConfiguration newConfiguration = new JeneticsOptimizationConfiguration();

        Object maximalPhenotypeAge = parameters.get("maximalPhenotypeAge");
        Object populationSize = parameters.get("populationSize");
        Object offspringFraction = parameters.get("offspringFraction");
        Object mutator = parameters.get("mutator");
        Object mutatorAlterProbability = parameters.get("mutatorAlterProbability");
        Object crossover = parameters.get("crossover");
        Object crossoverAlterProbability = parameters.get("crossoverAlterProbability");
        Object offspringSelector = parameters.get("offspringSelector");
        Object offspringSelectorParameter = parameters.get("offspringSelectorParameter");
        Object survivorsSelector = parameters.get("survivorsSelector");
        Object survivorsSelectorParameter = parameters.get("survivorsSelectorParameter");
        Object terminationConditions = parameters.get("terminationConditions");
        Object deduplicate = parameters.get("deduplicate");
        Object deduplicateMaxRetries = parameters.get("deduplicateMaxRetries");

        // set the parameters
        try {
            if (maximalPhenotypeAge != null) {
                newConfiguration.setMaximalPhenotypeAge((int) maximalPhenotypeAge);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("maximalPhenotypeAge", Integer.class);
        }

        try {
            if (populationSize != null) {
                newConfiguration.setPopulationSize((int) populationSize);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("populationSize", Integer.class);
        }

        try {
            if (offspringSelector != null) {
                newConfiguration.setOffspringSelector((String) offspringSelector);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("offspringSelector", String.class);
        }

        try {
            if (offspringSelectorParameter != null) {
                newConfiguration.setOffspringSelectorParameter((Number) offspringSelectorParameter);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("offspringSelectorParameter", Number.class);
        }

        try {
            if (offspringFraction != null) {
                newConfiguration.setOffspringFraction((double) offspringFraction);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("offspringFraction", Double.class);
        }

        try {
            if (mutator != null) {
                newConfiguration.setMutator((String) mutator);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("mutator", String.class);
        }

        try {
            if (mutatorAlterProbability != null) {
                newConfiguration.setMutatorAlterProbability((double) mutatorAlterProbability);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("mutatorAlterProbability", Double.class);
        }

        try {
            if (crossover != null) {
                newConfiguration.setCrossover((String) crossover);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("crossover", String.class);
        }

        try {
            if (crossoverAlterProbability != null) {
                newConfiguration.setCrossoverAlterProbability((double) crossoverAlterProbability);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("crossoverAlterProbability", Double.class);
        }

        try {
            if (terminationConditions != null) {
                // if cast throws error, the error is caught
                @SuppressWarnings("unchecked")
                Map<String,Object> terminationConditionsMap = (Map<String,Object>) terminationConditions;
                newConfiguration.setTerminationConditions(terminationConditionsMap);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("terminationConditions", Map.class);
        }

        try {
            if(survivorsSelector != null) {
                newConfiguration.setSurvivorsSelector((String) survivorsSelector);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("survivorsSelector", String.class);
        }

        try {
            if (survivorsSelectorParameter != null) {
                logger.info("Submitted survivors selector parameter: " + survivorsSelectorParameter);
                newConfiguration.setSurvivorsSelectorParameter((Number) survivorsSelectorParameter);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("survivorsSelectorParameter", Number.class);
        }


        try {
            if (deduplicate != null) {
                newConfiguration.setDeduplicate((boolean) deduplicate);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("deduplicate", Boolean.class);
        }

        try {
            if (deduplicateMaxRetries != null) {
                newConfiguration.setDeduplicateMaxRetries((int) deduplicateMaxRetries);
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("deduplicateMaxRetries", Integer.class);
        }


        // replace the configuration if no error was thrown
        this.configuration = newConfiguration;
    }

    @Override
    public JeneticsOptimizationStatistics getStatistics() {
        return this.statistics;
    }


    public SlotAllocationProblem getProblem() {
        return problem;
    }

    public List<Integer> getFitnessValuesResults() {
        return fitnessValuesResults;
    }

    public void setFitnessValuesResults(List<Integer> fitnessValuesResults) {
        this.fitnessValuesResults = fitnessValuesResults;
    }
}
