package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;
import io.jenetics.Alterer;
import io.jenetics.EnumGene;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JeneticsOptimization extends Optimization {
    private static final Logger logger = LogManager.getLogger();

    private JeneticsOptimizationConfiguration configuration;

    public JeneticsOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
    }

    @Override
    public Map<Flight, Slot> run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted.", e);
        }

        return new HashMap<Flight, Slot>();
    }

    @Override
    public OptimizationConfiguration getDefaultConfiguration() {
        return null;
    }

    @Override
    public OptimizationConfiguration getConfiguration() {
        return null;
    }

    @Override
    public void newConfiguration(Map<String, Object> parameters) {
        JeneticsOptimizationConfiguration newConfiguration =
            new JeneticsOptimizationConfiguration();

        Object maximumPhenotypeAge = parameters.get("maximumPhenotypeAge");
        Object populationSize = parameters.get("populationSize");

        // set the parameters
        try {
            if (maximumPhenotypeAge != null) {
                newConfiguration.setMaximumPhenotypeAge(maximumPhenotypeAge);
            }

            if (populationSize != null) {
                newConfiguration.setPopulationSize(populationSize);
            }
        } catch (InvalidParameterTypeException e) {
            logger.error("Wrong parameter type", e);
        }

        // replace the configuration if no error occurred
        this.configuration = newConfiguration;
    }

    @Override
    public OptimizationStatistics getStatistics() {
        return null;
    }

    @Override
    public void setStatistics(OptimizationStatistics statistics) {

    }


}
