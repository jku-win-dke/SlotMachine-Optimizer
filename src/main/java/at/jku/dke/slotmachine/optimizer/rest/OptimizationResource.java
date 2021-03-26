package at.jku.dke.slotmachine.optimizer.rest;

import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationResultDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatisticsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.*;

import java.util.*;

@Api(value = "SlotMachine Optimization")
@RestController
public class OptimizationResource {
	@ApiOperation(value = "Create and initialize a heuristic optimization with flights and preferences.", response = OptimizationDTO.class)
	@PostMapping(path = "/optimizations", consumes = "application/json")
	@ApiResponses(
		value = {
			@ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 400, message = "Bad Request")
		}
	)
	public ResponseEntity<OptimizationDTO> createAndInitializeOptimization(@RequestBody OptimizationDTO optimization) {
		return null;
    }

	@ApiOperation(value = "Start a specific optimization that was previously created and initialized.")
	@PutMapping(path = "/optimizations/{optId}/start")
	@ApiResponses(
		value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 409, message = "Conflict")
		}
	)
	public ResponseEntity<Void> startOptimization(@PathVariable UUID optId) {
		return null;
    }

    @ApiOperation(value = "Abort a previously started optimization.")
    @PutMapping(path = "/optimizations/{optId}/abort", produces = "application/json")
	@ApiResponses(
		value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 409, message = "Conflict")
		}
	)
	public ResponseEntity<Void> abortOptimization(@PathVariable UUID optId) {
		return null;
	}


	@ApiOperation(value = "Get the result of an optimization; returns intermediate result if not finished.", response = OptimizationResultDTO.class)
	@GetMapping(path = "/optimizations/{optId}/result", produces = "application/json")
	@ApiResponses(
	    value = {
	        @ApiResponse(code = 200, message = "OK"),
	    	@ApiResponse(code = 404, message = "Not Found")
		}
	)
    public ResponseEntity<OptimizationResultDTO> getOptimizationResult(@PathVariable UUID optId) {
		return null;
    }


	@ApiOperation(value = "Get current statistics for a specific optimization and its result.", response = OptimizationStatisticsDTO.class)
	@GetMapping(path = "/optimizations/{optId}/stats", produces = "application/json")
	@ApiResponses(
		value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found")
		}
	)
	public ResponseEntity<OptimizationStatisticsDTO> getOptimizationStatistics(@PathVariable UUID optId) {
		return null;
	}

	@ApiOperation(value = "Get the description of a specific optimization.", response = OptimizationStatisticsDTO.class)
	@GetMapping(path = "/optimizations/{optId}", produces = "application/json")
	@ApiResponses(
		value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found")
		}
	)
	public ResponseEntity<OptimizationDTO> getOptimization(@PathVariable UUID optId) {
		return null;
	}
}
