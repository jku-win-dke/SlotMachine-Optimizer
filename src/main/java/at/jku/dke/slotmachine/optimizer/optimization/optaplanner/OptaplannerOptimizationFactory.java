package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;

import java.util.Map;

public class OptaplannerOptimizationFactory extends OptimizationFactory {
    @Override
    public OptaplannerOptimization createOptimization(Flight[] flights, Slot[] slots) {
        return null;
    }

    @Override
    public OptaplannerOptimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
        return null;
    }
}
