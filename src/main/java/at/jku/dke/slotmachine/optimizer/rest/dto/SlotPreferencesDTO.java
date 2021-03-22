package at.jku.dke.slotmachine.optimizer.rest.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlotPreferencesDTO {
    private UUID sessionId;
    private Map<String, List<Integer>> weightMap;

    public SlotPreferencesDTO(UUID sessionId, Map<String, List<Integer>> weightMap) {
        this.sessionId = sessionId;
        this.weightMap = weightMap;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, List<Integer>> getWeightMap() {
        return weightMap;
    }

    public void setWeightMap(Map<String, List<Integer>> weightMap) {
        this.weightMap = weightMap;
    }


}
