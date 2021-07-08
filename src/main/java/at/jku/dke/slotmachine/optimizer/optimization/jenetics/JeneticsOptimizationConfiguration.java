package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import io.jenetics.*;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;

public class JeneticsOptimizationConfiguration extends OptimizationConfiguration {

    public void setProblemClassName(String problemClassName) {
        this.setParameter("problemClassName", problemClassName);
    }

    public String getProblemClassName() {
        return (String) this.getParameter("problemClassName");
    }

    public Problem getProblem(ISeq<Flight> flights, ISeq<Slot> slots) {
        return null;
    }

    /**
     * Returns the maximum phenotype age, or -1 if the parameter is not set.
     * @return the maximum phenotype age
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

        return null;
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
}
