package at.jku.dke.slotmachine.optimizer;

import java.util.UUID;

import io.swagger.annotations.ApiModelProperty;

public class OptimizerSession {
	@ApiModelProperty(notes = "ID of session", name="sessionId",required=true,value="test session id")
    private final UUID sessionId;

    public OptimizerSession(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
