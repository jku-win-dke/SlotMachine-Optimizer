package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class OptaplannerOptimizationFactory extends OptimizationFactory {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public OptaplannerOptimization createOptimization(Flight[] flights, Slot[] slots) {
        return new OptaplannerOptimization(flights, slots);
    }

    @Override
    public OptaplannerOptimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        OptaplannerOptimization optimization = this.createOptimization(flights, slots);

        try {
            optimization.newConfiguration(parameters);
        } catch (InvalidOptimizationParameterTypeException e) {
            logger.error("Wrong parameter for OptaPlanner configuration.", e);
            throw e;
        }

        return optimization;
    }
}
