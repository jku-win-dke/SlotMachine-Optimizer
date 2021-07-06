package at.jku.dke.slotmachine.optimizer.domain;

import java.time.Instant;

public class Margins {
	private String  flightId;
    private Instant scheduledTime;
    private Instant timeNotBefore;
    private Instant timeWished;
    private Instant timeNotAfter;

	public Margins(
		String  flightId,
		Instant scheduledTime,
		Instant timeNotBefore,
		Instant timeWished,
		Instant timeNotAfter
	) {
		this.flightId = flightId;
		this.scheduledTime = scheduledTime;
		this.timeNotBefore = timeNotBefore;
		this.timeWished = timeWished;
		this.timeNotAfter = timeNotAfter;
	}

	public String getFlightId() {
		return flightId;
	}
	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}

	public Instant getScheduledTime() {
		return scheduledTime;
	}
	public void setScheduledTime(Instant scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public Instant getTimeNotBefore() {
		return timeNotBefore;
	}
	public void setTimeNotBefore(Instant timeNotBefore) {
		this.timeNotBefore = timeNotBefore;
	}

	public Instant getTimeWished() {
		return timeWished;
	}
	public void setTimeWished(Instant timeWished) {
		this.timeWished = timeWished;
	}

	public Instant getTimeNotAfter() {
		return timeNotAfter;
	}
	public void setTimeNotAfter(Instant timeNotAfter) {
		this.timeNotAfter = timeNotAfter;
	} 
}
