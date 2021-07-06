package at.jku.dke.slotmachine.optimizer.optimization;

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


}
