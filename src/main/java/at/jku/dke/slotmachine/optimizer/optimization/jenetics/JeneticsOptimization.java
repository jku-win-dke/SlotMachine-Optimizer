package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation.BatchEvaluator;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation.BatchEvaluatorFactory;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JeneticsOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private JeneticsOptimizationConfiguration configuration = null;
    private JeneticsOptimizationStatistics statistics;
    private final SlotAllocationProblem problem;


    public JeneticsOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);

        this.statistics = new JeneticsOptimizationStatistics();

        this.problem = new SlotAllocationProblem(
                ISeq.of(this.getFlights()),
                ISeq.of(this.getSlots())
        );
        logger.info("Slot allocation problem initialized.");

//        logger.info("Weights:");
//        for(var flight : flights){
//            logger.info("\tFlight: {}", flight.getFlightId());
//            StringBuilder sb = new StringBuilder();
//            sb.append("\t").append("[");
//            for(var weight : flight.getWeights()){
//                sb.append(weight).append(", ");
//            }
//            sb.append("]");
//            logger.info(sb.toString());
//            for(var slot : getSlots()){
//                logger.info("\t\tSlot: {}. Weight: {}.", slot.getTime(), flight.getWeight(slot));
//            }
//        }
//
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

        logger.info("Initial population consists of " + initialPopulation.length() + " individuals.");
        logger.info("Initial population consists of " + initialPopulation.stream().distinct().toList().size() + " distinct individuals.");

        logger.info("Build the genetic algorithm engine.");

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

        logger.info("Result fitness after optimization: {}.", result.bestFitness());
        logger.info("Removing invalid solutions from result generation");
        logger.info("Result population contains {} invalid solutions.", result.invalidCount());
        AtomicInteger invalidPhenotypeCount = new AtomicInteger();
        List<Phenotype<EnumGene<Integer>, Integer>> validSolutions = result.population()
                .stream()
                .filter(phenotype -> {
                    Map<Flight, Slot> decodedGenotype = problem.decode(phenotype.genotype());
                    for(Map.Entry<Flight, Slot> entry : decodedGenotype.entrySet()){
                        if(entry.getKey().getScheduledTime() != null && entry.getKey().getScheduledTime().isAfter(entry.getValue().getTime())){
                            invalidPhenotypeCount.getAndIncrement();
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
        if(validSolutions.isEmpty()){
            logger.warn("There are no valid solutions left.");
            logger.warn("Optimization will return an invalid solution.");
            validSolutions.add(result.bestPhenotype());
        }

        logger.info("Removed {} invalid solutions.", invalidPhenotypeCount.get());
        logger.info("Result has {} remaining solutions.", validSolutions.size());
        result = EvolutionResult.of(
                Optimize.MAXIMUM,
                ISeq.of(validSolutions),
                result.generation(),
                result.totalGenerations(),
                result.durations(),
                invalidPhenotypeCount.get(),
                result.invalidCount() - invalidPhenotypeCount.get(),
                result.alterCount()
        );
        logger.info("Result fitness after invalid solutions have been removed: {}.", result.bestFitness());

        BatchEvaluator batchEvaluator = (BatchEvaluator) evaluator;
        if(this.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING ||
           this.getMode() == OptimizationMode.DEMONSTRATION ||
           this.getMode() == OptimizationMode.BENCHMARKING) {
            logger.info("Running in non-privacy-preserving mode.");
            logger.info("Evaluating result population with exact fitness values.");
            var evaluatedResultGeneration = result.population()
                    .stream()
                    .map(phenotype -> phenotype.withFitness(problem.fitness(phenotype.genotype())))
                    .sorted(Comparator.comparingInt(Phenotype::fitness))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            logger.info("Setting evaluated population as result.");
            result = EvolutionResult.of(
                    Optimize.MAXIMUM,
                    ISeq.of(evaluatedResultGeneration),
                    result.generation(),
                    result.totalGenerations(),
                    result.durations(),
                    result.killCount(),
                    result.invalidCount(),
                    result.alterCount()
            );

            logger.info("Setting fitness values of distinct, evaluated population.");
            var distinctIndividualFitnessValues = result.population()
                    .stream()
                    .map(Phenotype::genotype)
                    .distinct()
                    .map(problem::fitness)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            this.setFitnessValuesResults(distinctIndividualFitnessValues);
        }else {
            if(getFitnessMethod() != FitnessMethod.ACTUAL_VALUES){
                logger.debug("Running in privacy-preserving mode. Evaluating the last generation with actual values.");
                var seq = Seq.of(result.population());
                Integer[] fitnessValues = this.getPrivacyEngineService().computeActualFitnessValues(this, batchEvaluator.convertPopulationToArray(seq));

                EvolutionResult<EnumGene<Integer>, Integer> finalResult = result;
                var evaluatedResultGeneration = IntStream
                        .range(0, fitnessValues.length)
                        .mapToObj(i -> finalResult.population().get(i).withFitness(fitnessValues[i]))
                        .collect(Collectors.toList());

                logger.info("Setting evaluated population as new result population.");
                result = EvolutionResult.of(
                        Optimize.MAXIMUM,
                        ISeq.of(evaluatedResultGeneration),
                        result.generation(),
                        result.totalGenerations(),
                        result.durations(),
                        result.killCount(),
                        result.invalidCount(),
                        result.alterCount()
                );
            }
            logger.info("Setting fitness values of distinct, evaluated population.");
            var fitnessValueResults = result.population()
                    .stream()
                    .filter(distinctByAttribute(Phenotype::genotype))
                    .map(Phenotype::fitness)
                    .sorted(Comparator.reverseOrder())
                    .toList();

            this.setFitnessValuesResults(fitnessValueResults);
        }

//        StringBuilder sb = new StringBuilder();
//        sb.append("Calculated fitness values of result generation: ").append("\n\t");
//        for(int i : this.getFitnessValuesResults()){
//            sb.append(i + ", ");
//        }
//        sb.append(".\n");
//        logger.info(sb.toString());


        Map<Flight, Slot> resultMap = problem.decode(result.bestPhenotype().genotype());

        if(logger.isDebugEnabled()) {
            logger.debug("Checking if solution is valid ...");

            int invalidCount = 0;
            for (Flight f : resultMap.keySet()) {
                if (f.getScheduledTime() != null && f.getScheduledTime().isAfter(resultMap.get(f).getTime())) {
                    invalidCount++;
                    logger.debug("Flight " + f.getFlightId() + " with scheduled time " + f.getScheduledTime() +" at Slot " + resultMap.get(f).getTime());
                }

                if(invalidCount > 0) {
                    logger.debug("Solution is invalid. Number of invalid assignments: " + invalidCount);
                } else {
                    logger.debug("Solution is valid.");
                }
            }
        }

        logger.info("Statistics: \n" + statistics);
        logger.info("Printing statistics from BatchEvaluator");
        batchEvaluator.printLogs();


        int resultFitness;
        if(this.getMode() == OptimizationMode.PRIVACY_PRESERVING){
           resultFitness = result.bestPhenotype().fitness(); // no exact fitness value may be available with PRIVACY_PRESERVING
        }else{
            resultFitness = problem.fitness(result.bestPhenotype().genotype()); // exact fitness value can be calculated
        }

        logger.info("Setting statistics for this optimization."); // already initialized in constructor
        this.getStatistics().setTimeFinished(LocalDateTime.now());
        this.getStatistics().setResultFitness(resultFitness);
        this.getStatistics().setIterations((int) statistics.altered().count());
        this.getStatistics().setFitnessFunctionInvocations(problem.getFitnessFunctionApplications());
        this.getStatistics().setSolutionGeneration(result.bestPhenotype().generation());
        if(resultFitness > this.getMaximumFitness()) this.setMaximumFitness(resultFitness);

        logger.info("Fitness of best solution: " + this.getStatistics().getResultFitness());
        logger.info("Number of generations: " + this.getStatistics().getIterations());
        logger.info("Number of fitness function invocations: " + this.getStatistics().getFitnessFunctionInvocations());
        logger.info("Generation of best solution: " + this.getStatistics().getSolutionGeneration());

        // set the results
        List<Map<Flight, Slot>> resultList =
                result.population().stream()
                        .sorted(Comparator.comparingInt(Phenotype::fitness))
                        .sorted(Comparator.reverseOrder())
                        .map(Phenotype::genotype)
                        .distinct()
                        .map(problem::decode)
                        .toList();

        logger.info("Saving {} distinct results.", resultList.size());
        this.setResults(resultList);

//        sb = new StringBuilder();
//        sb.append("Fitness values of result generation: ").append("\n\t");
//        for(var pheno : result.population().stream()
//                .filter(distinctByAttribute(Phenotype::genotype))
//                .sorted(Comparator.comparingInt(Phenotype::fitness))
//                .sorted(Comparator.reverseOrder())
//                .toList()){
//            sb.append(pheno.fitness()).append(" ,");
//        }
//        sb.append("\n");
//        logger.info(sb.toString());

        logger.info("Converting result population to the format required by the PE.");
        Integer[][] resultListConverted = batchEvaluator.convertPopulationToArray(ISeq.of(result.population().stream()
                .filter(distinctByAttribute(Phenotype::genotype))
                .sorted(Comparator.comparingInt(Phenotype::fitness))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList())));

        this.setConvertedResults(resultListConverted);

        // return only the best result

//        sb = new StringBuilder();
//        sb.append("Best sequence (mapping): ").append("\n");
//        for(var entry : resultMap.entrySet()
//                .stream()
//                .sorted(Comparator.comparing((Map.Entry<Flight, Slot> e) -> e.getValue().getTime())).toList()){
//            sb.append("\t").append("Slot: ").append(entry.getValue().getTime()).append(". Flight: ").append(entry.getKey().getFlightId()).append(".").append("\n");
//        }
//        logger.info(sb.toString());
//
//        sb = new StringBuilder();
//        sb.append("Flight sequenc in problem: [").append("\n");
//        for(var flight : this.problem.getFlights()){
//            sb.append(flight.getFlightId()).append(", ");
//        }
//        sb.append("]");
//        logger.info(sb.toString());

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

    @Override
    public int computeInitialFitness() {
        logger.info("Calculating fitness of initial flight sequence.");
        Map<Flight, Slot> initialAllocation = new HashMap<>();

        int initialFitness = Integer.MIN_VALUE;

        List<Flight> initialFlightSequence =
                Arrays.stream(Objects.requireNonNullElse(this.getInitialFlightSequence(), new String[]{""}))
                        .map(flightId -> {
                            // return flight with same id (should find flight)
                            for(Flight flight : this.getFlights()) {
                                if(flight.getFlightId().equals(flightId)) {
                                    return flight;
                                }
                            }
                            // return null (shouldn't happen)
                            return null;
                        })
                        .collect(Collectors.toList());

        for(int i = 0; i < initialFlightSequence.size(); i++){
            if(initialFlightSequence.get(i) != null) {
                initialAllocation.put(initialFlightSequence.get(i), this.getSlots()[i]);
            }
        }

        long initialFlightSequenceNotNullCount = Arrays.stream(this.getFlights()).filter(Objects::nonNull).count();

        if(initialAllocation.size() < initialFlightSequenceNotNullCount){
            logger.info("Could not calculate initial fitness as not all initial flight IDs have been mapped to a slot.");
        }else{
            initialFitness = problem.fitness(initialAllocation);
            logger.info("Initial fitness: {}.", initialFitness);
        }

        return initialFitness;
    }


    public SlotAllocationProblem getProblem() {
        return problem;
    }

    /**
     * Get a Predicate that returns whether it has seen the elements' key according to the keyExtractor
     * @param keyExtractor extracts the key from T for filtering
     * @param <T> generic type
     * @return a stateful filter
     */
    public static <T> Predicate<T> distinctByAttribute(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
