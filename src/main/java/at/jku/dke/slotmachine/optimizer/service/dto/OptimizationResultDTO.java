package at.jku.dke.slotmachine.optimizer.service.dto;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


public class OptimizationResultDTO {
    private UUID optId;
    private String[] optimizedFlightSequence;
    private Instant[] slots;

    @Nullable
    private MarginsDTO[] margins;

    /** Basic Statistics **/

    @Nullable
    private int fitness;
    @Nullable
    private int fitnessFunctionInvocations;

    public OptimizationResultDTO(UUID optId, String[] optimizedFlightSequence, Instant[] slots) {
        this.optId = optId;
        this.optimizedFlightSequence = optimizedFlightSequence;
        this.slots = slots;
    }

    /**
     * Create a new instance from a result map between flights and slots.
     * @param optId the optimization identifier
     * @param resultMap a mapping between flights and slots
     * @return an OptimizationResultDTO based on the input mapping
     */
    public static OptimizationResultDTO fromResultMap(UUID optId, Map<Flight, Slot> resultMap) {
        // sort the flights by slot instant
        String[] optimizedFlightSequence = resultMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(Flight::getFlightId)
                .toArray(String[]::new);

        Instant[] slots = resultMap.values().stream().sorted().map(Slot::getTime).toArray(Instant[]::new);

        return new OptimizationResultDTO(optId, optimizedFlightSequence, slots);
    }

    public Instant[] getSlots() {
        return slots;
    }

    public void setSlots(Instant[] slots) {
        this.slots = slots;
    }

    @Nullable
    public MarginsDTO[] getMargins() {
        return margins;
    }

    public void setMargins(@Nullable MarginsDTO[] margins) {
        this.margins = margins;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int getFitnessFunctionInvocations() { return fitnessFunctionInvocations; }

    public void setFitnessFunctionInvocations(int fitnessFunctionInvocations) {
        this.fitnessFunctionInvocations = fitnessFunctionInvocations;
    }

	public UUID getOptId() {
        return optId;
    }

    public void setOptId(UUID optId) {
        this.optId = optId;
    }

    public String[] getOptimizedFlightSequence() {
        return optimizedFlightSequence;
    }

    public void setOptimizedFlightSequence(String[] optimizedFlightSequence) {
        this.optimizedFlightSequence = optimizedFlightSequence;
    }
}
