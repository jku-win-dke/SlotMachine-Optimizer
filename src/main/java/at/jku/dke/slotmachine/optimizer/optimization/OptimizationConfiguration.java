package at.jku.dke.slotmachine.optimizer.optimization;

import org.optaplanner.core.config.solver.SolverConfig;

import java.util.HashMap;
import java.util.Map;

public abstract class OptimizationConfiguration {
    public Map<String,Object> parameters = new HashMap<>();

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public String getStringParameter(String param) {
        Object value = this.getParameter(param);

        String stringValue = null;

        if(value != null) {
            stringValue = (String) value;
        }

        return stringValue;
    }

    public double getDoubleParameter(String param) {
        double doubleValue = Double.MIN_VALUE;
        Object value = this.getParameter(param);

        if(value != null) doubleValue = (double) value;

        return doubleValue;
    }

    public int getIntegerParameter(String param) {
        int integerValue = Integer.MIN_VALUE;
        Object value = this.getParameter(param);

        if(value != null) integerValue = (int) value;

        return integerValue;
    }
}
