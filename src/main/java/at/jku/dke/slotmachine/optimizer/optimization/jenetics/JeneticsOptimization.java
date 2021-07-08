package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

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
                .constraint(problem.constraint().get())
                .build();

        ISeq<Phenotype<EnumGene<Integer>, Integer>> initialPopulation = this.getConfiguration().getInitialPopulation();

        Genotype<EnumGene<Integer>> result = engine.stream(initialPopulation).collect(EvolutionResult.toBestGenotype());


        // TODO obtain the result


        Map<Flight, Slot> resultMap = problem.decode(result);

        return null;
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

        Object maximumPhenotypeAge = parameters.get("maximumPhenotypeAge");
        Object populationSize = parameters.get("populationSize");

        // set the parameters
        try {
            if (maximumPhenotypeAge != null) {
                newConfiguration.setMaximumPhenotypeAge(Integer.parseInt((String) maximumPhenotypeAge));
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("maximumPhenotypeAge", Integer.class);
        }

        try {
            if (populationSize != null) {
                newConfiguration.setMaximumPhenotypeAge(Integer.parseInt((String) populationSize));
            }
        } catch (Exception e) {
            throw new InvalidOptimizationParameterTypeException("populationSize", Integer.class);
        }

        // replace the configuration if no error was thrown
        this.configuration = newConfiguration;
    }

    @Override
    public JeneticsOptimizationStatistics getStatistics() {
        return null;
    }


}
