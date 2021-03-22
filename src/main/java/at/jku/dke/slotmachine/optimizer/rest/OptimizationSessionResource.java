package at.jku.dke.slotmachine.optimizer.rest;

import at.jku.dke.slotmachine.optimizer.rest.dto.FlightSequenceDTO;
import at.jku.dke.slotmachine.optimizer.rest.dto.OptimizationSessionDTO;
import at.jku.dke.slotmachine.optimizer.rest.dto.SlotPreferencesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.*;

import java.util.*;

@Api(value = "OptimizerRestController")
@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized!"),
			@ApiResponse(code = 403, message = "Forbidden!"),
			@ApiResponse(code = 404, message = "Not found!")
	}
)
@RestController
public class OptimizationSessionResource {
	
	private List<OptimizationSessionDTO> sessions;
	private List<SlotPreferencesDTO> slotpreferences = new LinkedList();
	private List<FlightSequenceDTO> flightSequences = new LinkedList();

    /**
     * Initialize heuristic optimization session.
     * @return a new optimizer session with a generated sessionId
     */
	@ApiOperation(value = "Initialize heuristic optimization session.", response = Iterable.class, tags = "initOptimizerSession")
	@PostMapping(path = "/sessions", produces = "application/json")
    public OptimizationSessionDTO initOptimizerSession() {
	    OptimizationSessionDTO optimizerSession = new OptimizationSessionDTO(UUID.randomUUID());

	    return optimizerSession;
        //return null;
    }

    /**
     * Submit preferences for an optimization session.
     * @param sessionId
     * @param payload
     */
	@ApiOperation(value = "Submit preferences for an optimization session", response = Iterable.class, tags = "submitPreferences")
	@PostMapping(path = "/sessions/{sessionId}/preferences", produces = "application/json", consumes = "application/json")
    public SlotPreferencesDTO submitPreferences(@PathVariable UUID sessionId, @RequestBody SlotPreferencesDTO payload) {
	// public void submitPreferences(@PathVariable UUID sessionId, @RequestBody SlotPreferencesDTO payload) {
		SlotPreferencesDTO slots = payload;
		return slots;
    }

    /**
     * Start the optimization session.
     */
	@ApiOperation(value = "Start the optimization session", response = Iterable.class, tags = "startOptimizationSession")
	@PutMapping(path = "/sessions/{sessionId}/start", consumes = "application/json", produces = "application/json")
    //public void startOptimizationSession(@PathVariable UUID sessionId, @RequestBody FlightSequenceDTO flightSequence) {
	public FlightSequenceDTO startOptimizationSession(@PathVariable UUID sessionId, @RequestBody FlightSequenceDTO flightSequence) {
		FlightSequenceDTO flightSeq = flightSequence;
		flightSequences.add(flightSeq);
		return flightSeq;
    }

    /**
     * Get the result of the optimization result, if already available.
     * @param sessionId
     */
	@ApiOperation(value = "Get the result of the optimization result, if already available", response = Iterable.class, tags = "getOptimizedFlightOrders")
	@GetMapping(path = "/sessions/{sessionId}/result", produces = "application/json")
    public ResponseEntity<FlightSequenceDTO> getOptimizedFlightSequence(@PathVariable UUID sessionId) {
		ResponseEntity<FlightSequenceDTO> response =
				ResponseEntity.notFound().build();
		
		for (FlightSequenceDTO f: flightSequences) {
			if (f.getSessionId().equals(sessionId)) {
				response = ResponseEntity.ok(f);
			}
		}
		
		return response;
    }
}
