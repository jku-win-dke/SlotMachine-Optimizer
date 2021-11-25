package at.jku.dke.slotmachine.optimizer.domain;

import java.time.chrono.ChronoLocalDateTime;

public class Slot implements Comparable<Slot> {
    private ChronoLocalDateTime<?> time;

    public Slot(ChronoLocalDateTime<?> time) {
        this.time = time;
    }

    public Slot() { }

    public ChronoLocalDateTime<?> getTime() {
        return time;
    }

    public void setTime(ChronoLocalDateTime<?> time) {
        this.time = time;
    }

    @Override
    public int compareTo(Slot o) {
        return this.getTime().compareTo(o.getTime());
    }
}
