package at.jku.dke.slotmachine.optimizer.optimization;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;

import java.util.Map;

public abstract class OptimizationFactory {
    public abstract Optimization createOptimization(Flight[] flights, Slot[] slots);
    public abstract Optimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException;
}
