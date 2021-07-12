package at.jku.dke.slotmachine.optimizer.optimization.hungarian;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;

import java.util.Map;

public class HungarianOptimization extends Optimization {
    public HungarianOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
    }

    @Override
    public Map<Flight, Slot> run() {
        return null;
    }

    @Override
    public HungarianOptimizationConfiguration getDefaultConfiguration() {
        return null;
    }

    @Override
    public HungarianOptimizationConfiguration getConfiguration() {
        return null;
    }

    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {

    }

    @Override
    public HungarianOptimizationStatistics getStatistics() {
        return null;
    }
}
