package at.jku.dke.slotmachine.optimizer.rest.dto;

import java.util.UUID;

import io.swagger.annotations.ApiModelProperty;

public class OptimizationSessionDTO {
	@ApiModelProperty(notes = "ID of session", name="sessionId",required=true,value="test session id")
    private UUID sessionId;

	public OptimizationSessionDTO(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
	    this.sessionId = sessionId;
    }
}
