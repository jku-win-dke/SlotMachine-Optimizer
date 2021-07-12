package at.jku.dke.slotmachine.optimizer.optimization.hungarian;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;

import java.util.Map;

public class HungarianOptimizationFactory extends OptimizationFactory {
    @Override
    public HungarianOptimization createOptimization(Flight[] flights, Slot[] slots) {
        return null;
    }

    @Override
    public HungarianOptimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        return null;
    }
}
