package at.jku.dke.slotmachine.optimizer.optimization;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;

import java.util.List;
import java.util.Map;

public abstract class OptimizationFactory {
    public abstract Optimization createOptimization(Flight[] flights, Slot[] slots);
    public abstract Optimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters);
}
