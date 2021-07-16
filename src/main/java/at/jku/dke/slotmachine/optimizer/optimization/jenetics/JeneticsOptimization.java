package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.EvolutionStream;
import io.jenetics.util.ISeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class JeneticsOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private JeneticsOptimizationConfiguration configuration = null;
    private JeneticsOptimizationStatistics statistics;

    public JeneticsOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);

        this.statistics = new JeneticsOptimizationStatistics();
    }

    @Override
    public Map<Flight, Slot> run() {

        SlotAllocationProblem problem = new SlotAllocationProblem(
            ISeq.of(this.getFlights()),
            ISeq.of(this.getSlots())
        );

        logger.info("Slot allocation problem initialized.");

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

        logger.info("Initial population consists of " + initialPopulation.length() + " individuals.");

        logger.info("-- Optimization --");

        logger.info("Build the genetic algorithm engine.");

        Engine<EnumGene<Integer>, Integer> engine = Engine.builder(problem)
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

        Phenotype<EnumGene<Integer>, Integer> result = stream
                .peek(statistics)
                .collect(EvolutionResult.toBestPhenotype());


        logger.info("Finished optimization");

        Map<Flight, Slot> resultMap = problem.decode(result.genotype());

        logger.info("Statistics: \n" + statistics);

        logger.info("Setting statistics for this optimization.");
        this.statistics = new JeneticsOptimizationStatistics();

        // get number of fitness function invocations first so as to not distort statistics because we
        // invoke the fitness function
        this.getStatistics().setFitnessFunctionInvocations(problem.getFitnessFunctionApplications());
        this.getStatistics().setSolutionFitness(problem.fitness(result.genotype()));
        this.getStatistics().setGenerations(statistics.altered().count());
        this.getStatistics().setSolutionGeneration(result.generation());

        logger.info("Number of fitness function applications: " + this.getStatistics().getFitnessFunctionInvocations());
        logger.info("Fitness of best solution: " + this.getStatistics().getSolutionFitness());
        logger.info("Number of generations: " + this.getStatistics().getGenerations());
        logger.info("Generation of best solution: " + this.getStatistics().getSolutionGeneration());

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


        // replace the configuration if no error was thrown
        this.configuration = newConfiguration;
    }

    @Override
    public JeneticsOptimizationStatistics getStatistics() {
        return this.statistics;
    }

}
