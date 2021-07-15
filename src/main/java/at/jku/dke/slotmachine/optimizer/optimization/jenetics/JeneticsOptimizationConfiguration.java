package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import io.jenetics.*;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

public class JeneticsOptimizationConfiguration extends OptimizationConfiguration {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Returns the maximal phenotype age, or Integer.MIN_VALUE if the parameter is not set.
     * @return the maximal phenotype age
     */
    public int getMaximalPhenotypeAge() {
        return this.getIntegerParameter("maximalPhenotypeAge");
    }

    public void setMaximalPhenotypeAge(int maximalPhenotypeAge) {
        this.setParameter("maximalPhenotypeAge", maximalPhenotypeAge);
    }


    public int getPopulationSize() {
        return this.getIntegerParameter("populationSize");
    }

    public void setPopulationSize(int populationSize) {
        this.setParameter("populationSize", populationSize);
    }

    public Mutator<EnumGene<Integer>, Integer> getMutator() {
        String mutatorType = this.getStringParameter("mutator");

        Mutator<EnumGene<Integer>, Integer> mutator = null;

        logger.info("-- Mutator --");

        if(mutatorType != null) {
            switch (mutatorType) {
                case "SWAP_MUTATOR":
                    double alterProbability = this.getMutatorAlterProbability();

                    if(alterProbability >= 0) {
                        logger.info("Use swap mutator with alter probability: " + alterProbability);
                        mutator = new SwapMutator<>(alterProbability);
                    } else {
                        logger.info("Use swap mutator with default alter probability.");
                        mutator = new SwapMutator<>();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mutatorType);
            }
        }

        return mutator;
    }

    private double getMutatorAlterProbability() {
        return this.getDoubleParameter("mutatorAlterProbability");
    }


    public Crossover<EnumGene<Integer>, Integer> getCrossover() {
        String crossoverType = this.getStringParameter("crossover");

        Crossover<EnumGene<Integer>, Integer> crossover = null;

        double alterProbability = this.getCrossoverAlterProbability();

        logger.info("-- Crossover --");

        if(crossoverType != null) {
            switch (crossoverType) {
                case "PARTIALLY_MATCHED_CROSSOVER":
                    if(alterProbability >= 0) {
                        logger.info("Use partially matched crossover with " + alterProbability + " alter probability.");
                        crossover = new PartiallyMatchedCrossover<>(alterProbability);
                    } else {
                        logger.info("No alter probability for partially matched crossover.");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + crossoverType);
            }
        }

        return crossover;
    }

    private double getCrossoverAlterProbability() {
        return this.getDoubleParameter("crossoverAlterProbability");
    }

    public Selector<EnumGene<Integer>, Integer> getOffspringSelector() {
        String selectorType = this.getStringParameter("offspringSelector");

        Number selectorParameter = this.getOffspringSelectorParameter();

        logger.info("-- Offspring Selector --");
        return this.getSelector(selectorType, selectorParameter);
    }

    private Number getOffspringSelectorParameter() {
        return this.getNumberParameter("offspringSelectorParameter");
    }

    public Selector<EnumGene<Integer>, Integer> getSurvivorsSelector() {
        String selectorType = this.getStringParameter("survivorsSelector");

        Number selectorParameter = this.getSurvivorsSelectorParameter();

        logger.info("-- Survivors Selector --");
        logger.info("Found survivors selector parameter: " + selectorParameter);
        return this.getSelector(selectorType, selectorParameter);
    }

    private Number getSurvivorsSelectorParameter() {
        return this.getNumberParameter("survivorsSelectorParameter");
    }

    public Selector<EnumGene<Integer>, Integer> getSelector(String selectorType, Number selectorParameter) {
        Selector<EnumGene<Integer>, Integer> selector = null;

        if(selectorType != null) {
            switch(selectorType) {
                case "BOLTZMANN_SELECTOR":
                    if(selectorParameter != null) {
                        selector = new BoltzmannSelector<>(selectorParameter.doubleValue());
                    } else {
                        selector = new BoltzmannSelector<>();
                    }
                    break;
                case "EXPONENTIAL_RANK_SELECTOR":
                    if(selectorParameter != null) {
                        selector = new ExponentialRankSelector<>(selectorParameter.doubleValue());
                    } else {
                        selector = new ExponentialRankSelector<>();
                    }
                    break;
                case "LINEAR_RANK_SELECTOR":
                    if(selectorParameter != null) {
                        selector = new LinearRankSelector<>(selectorParameter.doubleValue());
                    } else {
                        selector = new LinearRankSelector<>();
                    }
                    break;
                case "ROULETTE_WHEEL_SELECTOR":
                    selector = new RouletteWheelSelector<>();
                    break;
                case "STOCHASTIC_UNIVERSAL_SELECTOR":
                    selector = new StochasticUniversalSelector<>();
                    break;
                case "TOURNAMENT_SELECTOR":
                    if(selectorParameter != null) {
                        logger.info("Using tournament selector with parameter " + selectorParameter.intValue() + ".");
                        selector = new TournamentSelector<>(selectorParameter.intValue());
                    } else {
                        logger.info("Using tournament selector with default parameter.");
                        selector = new TournamentSelector<>();
                    }
                    break;
                case "TRUNCATION_SELECTOR":
                    if(selectorParameter != null) {
                        selector = new TruncationSelector<>(selectorParameter.intValue());
                    } else {
                        selector = new TruncationSelector<>();
                    }
                    break;
            }
        }

        return selector;
    }

    public double getOffspringFraction() {
        return this.getDoubleParameter("offspringFraction");
    }

    public ISeq<Genotype<EnumGene<Integer>>> getInitialPopulation(SlotAllocationProblem problem, int populationSize) {
        Map<Flight, Slot> initialAllocation = new HashMap<>();
        Flight[] flights;
        Slot[] slots;

        logger.info("-- Initial Population --");

        logger.info("Sort flights and available slots to get initial population.");
        flights = problem.getFlights().stream().sorted().toArray(Flight[]::new);
        slots = problem.getAvailableSlots().stream().sorted().toArray(Slot[]::new);

        logger.info("Allocate slots according to scheduled time.");
        for(int i = 0; i < flights.length; i++) {
            initialAllocation.put(flights[i], slots[i]);
        }

        Genotype<EnumGene<Integer>> genotype = problem.codec().encode(initialAllocation);

        Genotype[] genotypes = new Genotype[populationSize];

        genotypes[0] = genotype;

        logger.info("Swap neighboring flights in initial allocation.");
        for(int i = 1; i < genotypes.length; i++) {
            for(int j = 0; j < flights.length; j++) {
                Map<Flight, Slot> swappedAllocation = new HashMap<>(initialAllocation);

                if(j+1 < flights.length) {
                    Slot s1 = swappedAllocation.get(flights[j]);
                    Slot s2 = swappedAllocation.get(flights[j+1]);
                    swappedAllocation.put(flights[j], s2);
                    swappedAllocation.put(flights[j+1], s1);
                }
                genotype = problem.codec().encode(swappedAllocation);
                genotypes[i] = genotype;
            }
        }

        return ISeq.of(genotypes);
    }

    public Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>[] getTerminationConditions() {
        List<Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>> predicates = new LinkedList<>();

        Map<String,Object> terminationConditionParameters = this.getMapParameter("terminationConditions");

        if(terminationConditionParameters != null) {
            Set<String> terminationConditionTypes = terminationConditionParameters.keySet();

            for(String terminationConditionType : terminationConditionTypes) {
                Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>> nextPredicate = null;

                switch(terminationConditionType) {
                    case "WORST_FITNESS": {
                        int threshold = (int) terminationConditionParameters.get("WORST_FITNESS");

                        nextPredicate = result -> result.worstFitness() > threshold;
                        break;
                    }
                    case "BY_FITNESS_THRESHOLD": {
                        int threshold = (int) terminationConditionParameters.get("BY_FITNESS_THRESHOLD");

                        nextPredicate = Limits.byFitnessThreshold(threshold);
                        break;
                    }
                    case "BY_STEADY_FITNESS": {
                        int generations = (int) terminationConditionParameters.get("BY_STEADY_FITNESS");

                        nextPredicate = Limits.bySteadyFitness(generations);
                        break;
                    }
                    case "BY_FIXED_GENERATION": {
                        int generation = (int) terminationConditionParameters.get("BY_FIXED_GENERATION");

                        nextPredicate = Limits.byFixedGeneration(generation);
                        break;
                    }
                    case "BY_EXECUTION_TIME": {
                        int duration = (int) terminationConditionParameters.get("BY_EXECUTION_TIME");

                        nextPredicate = Limits.byExecutionTime(Duration.ofSeconds(duration));
                        break;
                    }
                    case "BY_POPULATION_CONVERGENCE": {
                        double epsilon = (double) terminationConditionParameters.get("BY_POPULATION_CONVERGENCE");

                        nextPredicate = Limits.byPopulationConvergence(epsilon);
                        break;
                    }
                    case "BY_FITNESS_CONVERGENCE": {
                        Map<String, Object> fitnessConvergenceParameters =
                                (Map<String, Object>) terminationConditionParameters.get("BY_FITNESS_CONVERGENCE");

                        int shortFilterSize = (int) fitnessConvergenceParameters.get("shortFilterSize");
                        int longFilterSize = (int) fitnessConvergenceParameters.get("longFilterSize");
                        double epsilon = (double) fitnessConvergenceParameters.get("epsilon");

                        nextPredicate = Limits.byFitnessConvergence(shortFilterSize, longFilterSize, epsilon);
                        break;
                    }
                }

                predicates.add(nextPredicate);
            }
        }

        return predicates.toArray(Predicate[]::new);
    }

    private Map<String,Object> getMapParameter(String param) {
        Map<String,Object> mapValue = null;
        Object value = this.getParameter(param);

        if(value != null) mapValue = (Map<String,Object>) value;

        return mapValue;
    }

    public void setTerminationConditions(Map<String,Object> terminationConditionParameters) {
        this.setParameter("terminationConditions", terminationConditionParameters);
    }

    public void setOffspringFraction(double offspringFraction) {
        this.setParameter("offspringFraction", offspringFraction);
    }

    public void setMutator(String mutator) {
        this.setParameter("mutator", mutator);
    }

    public void setCrossover(String crossover) {
        this.setParameter("crossover", crossover);
    }

    public void setMutatorAlterProbability(double mutatorAlterProbability) {
        this.setParameter("mutatorAlterProbability", mutatorAlterProbability);
    }

    public void setCrossoverAlterProbability(double crossoverAlterProbability) {
        this.setParameter("crossoverAlterProbability", crossoverAlterProbability);
    }

    public void setOffspringSelector(String offspringSelector) {
        this.setParameter("offspringSelector", offspringSelector);
    }

    public void setSurvivorsSelector(String survivorsSelector) {
        this.setParameter("survivorsSelector", survivorsSelector);
    }

    public void setOffspringSelectorParameter(Number offspringSelectorParameter) {
        this.setParameter("offspringSelectorParameter", offspringSelectorParameter);
    }

    public void setSurvivorsSelectorParameter(Number survivorsSelectorParameter) {
        this.setParameter("survivorsSelectorParameter", survivorsSelectorParameter);
    }
}
