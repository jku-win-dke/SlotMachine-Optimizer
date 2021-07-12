package at.jku.dke.slotmachine.optimizer.optimization.hungarian;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HungarianOptimizationFactory extends OptimizationFactory {
	private static final Logger logger = LogManager.getLogger();
	
    @Override
    public HungarianOptimization createOptimization(Flight[] flights, Slot[] slots) {
        return new HungarianOptimization(flights, slots);
    }

    @Override
    public HungarianOptimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
    	HungarianOptimization optimization = this.createOptimization(flights, slots);

        try {
            optimization.newConfiguration(parameters);
        } catch (InvalidOptimizationParameterTypeException e) {
            logger.error("Wrong parameter for Hungarian Algorithm configuration.", e);
            throw e;
        }

        return optimization;
    }
}
