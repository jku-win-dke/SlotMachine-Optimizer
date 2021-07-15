package at.jku.dke.slotmachine.optimizer.optimization.jenetics; 

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class JeneticsOptimizationFactory extends OptimizationFactory {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public JeneticsOptimization createOptimization(Flight[] flights, Slot[] slots) {
        return new JeneticsOptimization(flights, slots);
    }

    @Override
    public JeneticsOptimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        JeneticsOptimization optimization = this.createOptimization(flights, slots);

        try {
            optimization.newConfiguration(parameters);
        } catch (InvalidOptimizationParameterTypeException e) {
            logger.error("Wrong parameter for Jenetics configuration.", e);
            throw e;
        }

        return optimization;
    }
}
