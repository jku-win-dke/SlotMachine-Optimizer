package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;

import java.util.Map;

public class OptaplannerOptimization extends Optimization {
    public OptaplannerOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
    }

    @Override
    public Map<Flight, Slot> run() {
        return null;
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
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {

    }

    @Override
    public OptimizationStatistics getStatistics() {
        return null;
    }
}
