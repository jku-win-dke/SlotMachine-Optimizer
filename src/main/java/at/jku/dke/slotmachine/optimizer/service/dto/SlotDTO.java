package at.jku.dke.slotmachine.optimizer.service.dto;

import java.time.Instant;

public class SlotDTO {
    private Instant time;

    public SlotDTO(Instant time) {
        this.time = time;
    }

    public SlotDTO() {}

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
