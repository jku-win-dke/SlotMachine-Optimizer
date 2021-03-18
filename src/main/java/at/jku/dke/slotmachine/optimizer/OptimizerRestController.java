package at.jku.dke.slotmachine.optimizer;

import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.*;

import javax.websocket.server.PathParam;
import java.util.UUID;

@Api(value = "OptimizerRestController", description = "REST APIs related to optimization sessions")
@RestController
public class OptimizerRestController {

    /**
     * Initialize a new optimizer session.
     * @return a new optimizer session with a generated sessionId
     */
	@ApiOperation(value = "Initialize heuristic optimizer session.", response = Iterable.class, tags = "initOptimizerSession")
	@ApiResponses(value = { 
	            @ApiResponse(code = 200, message = "Success|OK"),
	            @ApiResponse(code = 401, message = "not authorized!"), 
	            @ApiResponse(code = 403, message = "forbidden!!!"),
	            @ApiResponse(code = 404, message = "not found!!!") })
    @PostMapping("/sessions")
    public OptimizerSession initOptimizerSession() {
        return new OptimizerSession(UUID.randomUUID());
    }

    /**
     * Submit preferences for a session id.
     * @param sessionId
     * @param payload
     */
	@ApiOperation(value = "Submit preferences for the session", response = Iterable.class, tags = "submitPreferences")
	@ApiResponses(value = { 
	            @ApiResponse(code = 200, message = "Success|OK"),
	            @ApiResponse(code = 401, message = "not authorized!"), 
	            @ApiResponse(code = 403, message = "forbidden!!!"),
	            @ApiResponse(code = 404, message = "not found!!!") })
    @PostMapping("/sessions/{sessionId}/preferences")
    public void submitPreferences(@PathVariable UUID sessionId, SlotPreferences payload) {

    }

    /**
     * Start the optimizer session.
     */
	@ApiOperation(value = "Start the optimizer session", response = Iterable.class, tags = "startOptimizerSession")
	@ApiResponses(value = { 
	            @ApiResponse(code = 200, message = "Success|OK"),
	            @ApiResponse(code = 401, message = "not authorized!"), 
	            @ApiResponse(code = 403, message = "forbidden!!!"),
	            @ApiResponse(code = 404, message = "not found!!!") })
    @PutMapping("/sessions/{sessionId}/start")
    public void startOptimizerSession(@PathVariable UUID sessionId) {

    }

    /**
     * Get the result of the optimization result, if already available.
     * @param sessionId
     */
	@ApiOperation(value = "Get the result of the optimization result, if already available", response = Iterable.class, tags = "getOptimizedFlightOrders")
	@ApiResponses(value = { 
	            @ApiResponse(code = 200, message = "Success|OK"),
	            @ApiResponse(code = 401, message = "not authorized!"), 
	            @ApiResponse(code = 403, message = "forbidden!!!"),
	            @ApiResponse(code = 404, message = "not found!!!") })
    @GetMapping("/sessions/{sessionId}/result")
    public void getOptimizedFlightOrders(@PathVariable UUID sessionId) {

    }
}
