package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.optimization.InvalidParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;

import java.util.Map;

public class JeneticsOptimizationConfiguration extends OptimizationConfiguration {

    /**
     * Returns the maximum phenotype age, or -1 if the parameter is not set.
     * @return the maximum phenotype age
     */
    public int getMaximumPhenotypeAge() {
        return this.getIntegerParameter("maximumPhenotypeAge");
    }

    public void setMaximumPhenotypeAge(int maximumPhenotypeAge) {
        this.setParameter("maximumPhenotypeAge", maximumPhenotypeAge);
    }


    public void setMaximumPhenotypeAge(Object maximumPhenotypeAge) throws InvalidParameterTypeException {
        if (!(maximumPhenotypeAge instanceof Integer)) {
            throw new InvalidParameterTypeException("maximumPhenotypeAge", Integer.class, maximumPhenotypeAge.getClass());
        }

        this.setMaximumPhenotypeAge(maximumPhenotypeAge);
    }


    public int getPopulationSize() {
        return this.getIntegerParameter("populationSize");
    }

    public void setPopulationSize(int populationSize) {
        this.setParameter("populationSize", populationSize);
    }

    public void setPopulationSize(Object populationSize) throws InvalidParameterTypeException {
        if (!(populationSize instanceof Integer)) {
            throw new InvalidParameterTypeException("populationSize", Integer.class, populationSize.getClass());
        }

        this.setPopulationSize(populationSize);
    }

    private int getIntegerParameter(String param) {
        int integerValue = -1;
        Object value = this.getParameter(param);

        if(value != null) {
            integerValue = (int) value;
        }

        return integerValue;
    }

}
