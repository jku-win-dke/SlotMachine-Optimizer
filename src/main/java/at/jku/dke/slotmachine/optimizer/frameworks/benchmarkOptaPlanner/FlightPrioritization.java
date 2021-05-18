package at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class FlightPrioritization {

	private int applications; // applications of score calculations, used for logger
	
	private Map<FlightBenchmarkOptaPlanner,Slot> sequence = null;
	
    @ValueRangeProvider(id = "slotRange")
    @ProblemFactCollectionProperty
    private List<Slot> slots;

    @PlanningEntityCollectionProperty
    private List<FlightBenchmarkOptaPlanner> flights;

    @PlanningScore
    private HardSoftScore score;

    private FlightPrioritization() {

    }
	
	public FlightPrioritization(List<Slot> slots, List<Flight> flightDomain) {
        this.slots = slots;
        // create FlightBenchmarkOptaPlanner
        List<FlightBenchmarkOptaPlanner> flightList = new LinkedList<FlightBenchmarkOptaPlanner>();
        for (Flight f: flightDomain) {
        	flightList.add(new FlightBenchmarkOptaPlanner(f.getFlightId(), f.getScheduledTime(), f.getWeightMap(), f.getSlot()));
        }
        this.flights = flightList;
        this.applications = 0;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public List<FlightBenchmarkOptaPlanner> getFlights() {
        return flights;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public Map<FlightBenchmarkOptaPlanner, Slot> getSequence() {
        if(this.sequence == null) {
            this.sequence = new HashMap<FlightBenchmarkOptaPlanner, Slot>();

            for (FlightBenchmarkOptaPlanner f : flights) {
                this.sequence.put(f, f.getSlot());
            }
        }

        return this.sequence;
    }

	public int getApplications() {
		return applications;
	}

	public void setApplications(int applications) {
		this.applications = applications;
	}
}
