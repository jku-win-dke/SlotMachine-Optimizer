package at.jku.dke.slotmachine.optimizer.domain;

import java.time.Instant;

import org.optaplanner.core.api.domain.lookup.PlanningId;

public class Slot implements Comparable<Slot> {
	// PlanningId is used for OptaPlanner (move thread count)
	@PlanningId
    private Instant time;

    public Slot(Instant time) {
        this.time = time;
    }

    public Slot() { }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }


    @Override
    public int compareTo(Slot o) {
        return this.getTime().compareTo(o.getTime());
    }


}
