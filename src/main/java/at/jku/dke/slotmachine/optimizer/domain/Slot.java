package at.jku.dke.slotmachine.optimizer.domain;

import java.time.LocalDateTime;

public class Slot implements Comparable<Slot> {
    private LocalDateTime time;

    public Slot(LocalDateTime time) {
        this.time = time;
    }

    public Slot() { }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public int compareTo(Slot o) {
        return this.getTime().compareTo(o.getTime());
    }
}
