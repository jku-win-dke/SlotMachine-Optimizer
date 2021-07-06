package at.jku.dke.slotmachine.optimizer.optimization.optaplanner;

import at.jku.dke.slotmachine.optimizer.domain.Slot;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.time.Instant;
import java.util.Objects;

public class SlotProblemFact implements Comparable<SlotProblemFact> {
    private Slot wrappedSlot;

    // PlanningId is used for OptaPlanner (move thread count)
    @PlanningId
    private Instant time;

    public SlotProblemFact(Slot wrappedSlot) {
        this.wrappedSlot = wrappedSlot;
        this.time = wrappedSlot.getTime();
    }

    public SlotProblemFact() { }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public Slot getWrappedSlot() {
        return wrappedSlot;
    }

    public void setWrappedSlot(Slot wrappedSlot) {
        this.wrappedSlot = wrappedSlot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotProblemFact that = (SlotProblemFact) o;
        return wrappedSlot.equals(that.wrappedSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrappedSlot);
    }

    @Override
    public int compareTo(SlotProblemFact slotProblemFact) {
        return this.getWrappedSlot().compareTo(slotProblemFact.getWrappedSlot());
    }
}
