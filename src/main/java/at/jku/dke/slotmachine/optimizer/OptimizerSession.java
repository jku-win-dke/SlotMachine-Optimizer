package at.jku.dke.slotmachine.optimizer;

import java.util.UUID;

public class OptimizerSession {
    private final UUID sessionId;

    public OptimizerSession(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
