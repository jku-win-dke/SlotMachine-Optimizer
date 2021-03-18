package at.jku.dke.slotmachine.optimizer;

import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.UUID;

@RestController
public class OptimizerRestController {

    /**
     * Initialize a new optimizer session.
     * @return a new optimizer session with a generated sessionId
     */
    @PostMapping("/sessions")
    public OptimizerSession initOptimizerSession() {
        return new OptimizerSession(UUID.randomUUID());
    }

    /**
     * Submit preferences for a session id.
     * @param sessionId
     * @param payload
     */
    @PostMapping("/sessions/{sessionId}/preferences")
    public void submitPreferences(@PathVariable UUID sessionId, SlotPreferences payload) {

    }

    /**
     * Start the optimizer session.
     */
    @PutMapping("/sessions/{sessionId}/start")
    public void startOptimizerSession(@PathVariable UUID sessionId) {

    }

    /**
     * Get the result of the optimization result, if already available.
     * @param sessionId
     */
    @GetMapping("/sessions/{sessionId}/result")
    public void getOptimizedFlightOrders(@PathVariable UUID sessionId) {

    }
}
