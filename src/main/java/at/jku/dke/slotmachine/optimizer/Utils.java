package at.jku.dke.slotmachine.optimizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Obtains a map from a JSON string. The JSON string must be a collection of key/value pairs.
     * @param json the JSON string to be parsed
     * @return a map with the key/value pairs from the JSON string
     */
    public static Map<String, String> getMapFromJson(String json) {
        Map<String, String> map = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
           map = mapper.readValue(json, new TypeReference<Map<String, String>>(){});
        } catch (JsonProcessingException e) {
            logger.error("Could not process JSON string", e);
        }

        return map;
    }
}
