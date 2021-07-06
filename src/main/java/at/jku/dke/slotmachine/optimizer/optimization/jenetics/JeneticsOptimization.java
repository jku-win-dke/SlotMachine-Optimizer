package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
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
