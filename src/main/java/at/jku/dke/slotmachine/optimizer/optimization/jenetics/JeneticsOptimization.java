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

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Predicate;

public class JeneticsOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private JeneticsOptimizationConfiguration configuration;

    public JeneticsOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
    }

    @Override
    public Map<Flight, Slot> run() {
        logger.info("Running optimization using Jenetics framework as slot allocation problem ...");

        SlotAllocationProblem problem = new SlotAllocationProblem(
            ISeq.of(this.getFlights()),
            ISeq.of(this.getSlots())
        );

        int populationSize = this.getConfiguration().getPopulationSize();
        if(populationSize == -1) { populationSize = this.getDefaultConfiguration().getPopulationSize(); }

        Mutator<EnumGene<Integer>, Integer> mutator = this.getConfiguration().getMutator();
        if(mutator == null) { mutator = this.getDefaultConfiguration().getMutator(); }

        Crossover<EnumGene<Integer>, Integer> crossover = this.getConfiguration().getCrossover();
        if(crossover == null) { crossover = this.getDefaultConfiguration().getCrossover(); }

        Selector<EnumGene<Integer>, Integer> offspringSelector = this.getConfiguration().getOffspringSelector();
        if(offspringSelector == null) { offspringSelector = this.getDefaultConfiguration().getOffspringSelector(); }

        Selector<EnumGene<Integer>, Integer> survivorsSelector = this.getConfiguration().getSurvivorsSelector();
        if(survivorsSelector == null) { survivorsSelector = this.getDefaultConfiguration().getSurvivorsSelector(); }

        int maximalPhenotypeAge = this.getConfiguration().getMaximalPhenotypeAge();
        if(maximalPhenotypeAge == -1) { maximalPhenotypeAge = this.getDefaultConfiguration().getMaximalPhenotypeAge(); }

        double offspringFraction = this.getConfiguration().getOffspringFraction();
        if(offspringFraction == -1.0) { offspringFraction = this.getDefaultConfiguration().getOffspringFraction(); }

        Engine<EnumGene<Integer>, Integer> engine = Engine.builder(problem)
                .populationSize(populationSize)
                .alterers(mutator, crossover)
                .offspringSelector(offspringSelector)
                .survivorsSelector(survivorsSelector)
                .maximalPhenotypeAge(maximalPhenotypeAge)
                .offspringFraction(offspringFraction)
                .constraint(problem.constraint().isPresent()?problem.constraint().get():null)
                .build();

        ISeq<Phenotype<EnumGene<Integer>, Integer>> initialPopulation = this.getConfiguration().getInitialPopulation();
        if(initialPopulation == null) { initialPopulation = this.getDefaultConfiguration().getInitialPopulation(); }

        Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>[] terminationConditions =
                this.getConfiguration().getTerminationConditions();
        if(terminationConditions == null) {
            terminationConditions = this.getConfiguration().getTerminationConditions();
        }

        EvolutionStatistics <Integer, ?> statistics = EvolutionStatistics.ofNumber();

        EvolutionStream<EnumGene<Integer>, Integer> stream = engine.stream(initialPopulation);

        for(Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>> predicate : terminationConditions) {
            stream = stream.limit(predicate);
        }

        Genotype<EnumGene<Integer>> result = stream
                .peek(statistics)
                .collect(EvolutionResult.toBestGenotype());

        Map<Flight, Slot> resultMap = problem.decode(result);

        this.updateStatistics(statistics);

        return resultMap;
    }

    @Override
    public JeneticsOptimizationConfiguration getDefaultConfiguration() {
        // TODO create default configuration that can be returned

        return null;
    }

    @Override
    public JeneticsOptimizationConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        JeneticsOptimizationConfiguration newConfiguration =
            new JeneticsOptimizationConfiguration();

        Object maximalPhenotypeAge = parameters.get("maximalPhenotypeAge");
        Object populationSize = parameters.get("populationSize");
        Object offspringFraction = parameters.get("offspringFraction");
        Object mutator = parameters.get("mutator");
        Object mutatorAlterProbability = parameters.get("mutatorAlterProbability");
        Object crossover = parameters.get("crossover");
        Object crossoverAlterProbability = parameters.get("crossoverAlterProbability");
        Object terminationConditions = parameters.get("terminationConditions");

        // set the parameters
        try {
            if (maximalPhenotypeAge != null) {
                newConfiguration.setMaximalPhenotypeAge(Integer.parseInt((String) maximalPhenotypeAge));
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("maximalPhenotypeAge", Integer.class);
        }

        try {
            if (populationSize != null) {
                newConfiguration.setMaximalPhenotypeAge(Integer.parseInt((String) populationSize));
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("populationSize", Integer.class);
        }

        try {
            if (offspringFraction != null) {
                newConfiguration.setOffspringFraction(Double.parseDouble((String) offspringFraction));
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
                newConfiguration.setMutatorAlterProbability(Double.parseDouble((String) mutatorAlterProbability));
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
                newConfiguration.setCrossoverAlterProbability(Double.parseDouble((String) crossoverAlterProbability));
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


        // replace the configuration if no error was thrown
        this.configuration = newConfiguration;
    }

    @Override
    public JeneticsOptimizationStatistics getStatistics() {
        return null;
    }

    private void updateStatistics(EvolutionStatistics<Integer,?> statistics) {

    }

}
