package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import io.jenetics.*;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;

import java.util.function.Predicate;

public class JeneticsOptimizationConfiguration extends OptimizationConfiguration {
    /**
     * Returns the maximal phenotype age, or -1 if the parameter is not set.
     * @return the maximal phenotype age
     */
    public int getMaximalPhenotypeAge() {
        return this.getIntegerParameter("maximalPhenotypeAge");
    }

    public void setMaximumPhenotypeAge(int maximalPhenotypeAge) {
        this.setParameter("maximalPhenotypeAge", maximalPhenotypeAge);
    }


    public void setMaximumPhenotypeAge(Object maximumPhenotypeAge) throws InvalidOptimizationParameterTypeException {
        if (!(maximumPhenotypeAge instanceof Integer)) {
            throw new InvalidOptimizationParameterTypeException("maximumPhenotypeAge", Integer.class, maximumPhenotypeAge.getClass());
        }

        this.setMaximumPhenotypeAge(maximumPhenotypeAge);
    }


    public int getPopulationSize() {
        return this.getIntegerParameter("populationSize");
    }

    public void setPopulationSize(int populationSize) {
        this.setParameter("populationSize", populationSize);
    }

    public void setPopulationSize(Object populationSize) throws InvalidOptimizationParameterTypeException {
        if (!(populationSize instanceof Integer)) {
            throw new InvalidOptimizationParameterTypeException("populationSize", Integer.class, populationSize.getClass());
        }

        this.setPopulationSize(populationSize);
    }

    private int getIntegerParameter(String param) {
        int integerValue = -1;
        Object value = this.getParameter(param);

        if(value != null) integerValue = (int) value;

        return integerValue;
    }

    public Mutator<EnumGene<Integer>, Integer> getMutator() {
        String mutatorType = this.getStringParameter("mutator");

        Mutator<EnumGene<Integer>, Integer> mutator = null;

        if(mutatorType != null) {
            switch (mutatorType) {
                case "SWAP_MUTATOR":
                    double alterProbability = this.getMutatorAlterProbability();

                    if(alterProbability >= 0) {
                        mutator = new SwapMutator<>(alterProbability);
                    } else {
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


    private String getStringParameter(String param) {
        return (String) this.getParameter(param);
    }


    public Crossover<EnumGene<Integer>, Integer> getCrossover() {

        return null;
    }

    public Selector<EnumGene<Integer>, Integer> getOffspringSelector() {

        return null;
    }

    public Selector<EnumGene<Integer>, Integer> getSurvivorsSelector() {

        return null;
    }

    public double getOffspringFraction() {
        return this.getDoubleParameter("offspringFraction");
    }

    private double getDoubleParameter(String param) {
        double doubleValue = -1.0;
        Object value = this.getParameter(param);

        if(value != null) doubleValue = (double) value;

        return doubleValue;
    }

    public ISeq<Phenotype<EnumGene<Integer>, Integer>> getInitialPopulation() {
        return null;
    }

    public Predicate<EvolutionResult<EnumGene<Integer>, Integer>> getTerminationCondition() {
        Predicate<EvolutionResult<EnumGene<Integer>, Integer>> predicate = null;

        return predicate;
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

}
