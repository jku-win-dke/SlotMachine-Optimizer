package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class JeneticsOptimizationFactory extends OptimizationFactory {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public JeneticsOptimization createOptimization(Flight[] flights, Slot[] slots) {
        return new JeneticsOptimization(flights, slots);
    }

    @Override
    public JeneticsOptimization createOptimization(Flight[] flights, Slot[] slots, Map<String, Object> parameters) {
        JeneticsOptimization optimization = this.createOptimization(flights, slots);

        optimization.newConfiguration(parameters);

        return optimization;
    }
}
