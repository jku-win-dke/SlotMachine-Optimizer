package at.jku.dke.slotmachine.optimizer.service.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OptimizationStatisticsDTO {
    private String optId;

    // the instant when the optimization object was retrieved
    private Instant validTime;

    private enum StatusType {
        CREATED, IN_PROGRESS, FINISHED, ABORTED
    }

    private StatusType status;

    private Instant timeCreated;
    private Instant timeStarted;
    private Instant timeAborted;
    private Instant timeFinished;

    private Duration duration;

    private double initialFitness;
    private double resultFitness;
    private int iterations;

    public String getOptId() {
        return optId;
    }

    public void setOptId(String optId) {
        this.optId = optId;
    }

    public Instant getValidTime() {
        return validTime;
    }

    public void setValidTime(Instant validTime) {
        this.validTime = validTime;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public Instant getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Instant timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Instant getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(Instant timeStarted) {
        this.timeStarted = timeStarted;
    }

    public Instant getTimeAborted() {
        return timeAborted;
    }

    public void setTimeAborted(Instant timeAborted) {
        this.timeAborted = timeAborted;
    }

    public Instant getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished(Instant timeFinished) {
        this.timeFinished = timeFinished;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public double getInitialFitness() {
        return initialFitness;
    }

    public void setInitialFitness(double initialFitness) {
        this.initialFitness = initialFitness;
    }

    public double getResultFitness() {
        return resultFitness;
    }

    public void setResultFitness(double resultFitness) {
        this.resultFitness = resultFitness;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
