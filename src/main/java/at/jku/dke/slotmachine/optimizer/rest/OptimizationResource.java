package at.jku.dke.slotmachine.optimizer.rest;

import at.jku.dke.slotmachine.optimizer.service.OptimizationService;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationResultDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatisticsDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/***
 * The OptimizationResource relays the REST calls to the {@link at.jku.dke.slotmachine.optimizer.service.OptimizationService}
 * in order to to initiate and manage an optimization.
 */
@Api(value = "SlotMachine Optimization")
@RestController
public class OptimizationResource {
    private static final Logger logger = LogManager.getLogger();

    private final OptimizationService optimizationService;

    public OptimizationResource(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @ApiOperation(value = "Create and initialize a (heuristic) optimization with flights and preferences.", response = OptimizationDTO.class)
    @PostMapping(path = "/optimizations", consumes = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Created"),
                    @ApiResponse(code = 400, message = "Bad Request")
            }
    )
    public ResponseEntity<OptimizationDTO> createAndInitializeOptimization(@RequestBody OptimizationDTO optimization) {
        logger.debug("Given parameter to initialize optimization: " + optimization.toString());

        ResponseEntity<OptimizationDTO> optimizationResponse;

        try {
            OptimizationDTO optimizationDto =
                optimizationService.createAndInitializeOptimization(optimization);

            optimizationResponse = new ResponseEntity<>(optimizationDto, HttpStatus.OK);
        } catch (Exception e) {
            optimizationResponse = new ResponseEntity<>(optimization, HttpStatus.BAD_REQUEST);
        }

        return optimizationResponse;
    }

    @ApiOperation(value = "Start a specific optimization that was previously created and initialized.")
    @PutMapping(path = "/optimizations/{optId}/start")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 202, message = "Accepted"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<Void> startOptimization(@PathVariable UUID optId) {
        ResponseEntity<Void> optimizationResponse;

        if (!optimizationService.existsOptimizationWithId(optId)) {
            logger.info("Optimization with id " + optId + " not found.");
            optimizationResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            optimizationService.runOptimizationAsynchronously(optId);

            optimizationResponse = new ResponseEntity<>(HttpStatus.ACCEPTED);

            logger.info("Optimization with id " + optId + " has successfully started.");
        }

        return optimizationResponse;
    }

    @ApiOperation(value = "Run a specific optimization that was previously created and initialized in a synchronized way, waiting for the response.")
    @PutMapping(path = "/optimizations/{optId}/start/wait")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 202, message = "Accepted"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationResultDTO> startOptimizationSync(@PathVariable UUID optId) {
        ResponseEntity<OptimizationResultDTO> optimizationResponse;

        if (!optimizationService.existsOptimizationWithId(optId)) {
            logger.info("Optimization with id " + optId + " not found.");
            optimizationResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            OptimizationResultDTO optimizationResult = optimizationService.runOptimization(optId);

            optimizationResponse = new ResponseEntity<>(optimizationResult, HttpStatus.ACCEPTED);

            logger.info("Optimization run with id " + optId + " has finished.");
        }

        return optimizationResponse;
    }

    @ApiOperation(value = "Abort a previously started optimization; if available, an intermediate result can be obtained.")
    @PutMapping(path = "/optimizations/{optId}/abort", produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found"),
                    @ApiResponse(code = 409, message = "Conflict")
            }
    )
    public ResponseEntity<Void> abortOptimization(@PathVariable UUID optId) {
        // TODO implement abortOptimization method.
        return null;
    }

    @ApiOperation(value = "Get the result of an optimization, if available.", response = OptimizationResultDTO.class)
    @GetMapping(path = {"/optimizations/{optId}/result"}, produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationResultDTO> getOptimizationResult(@PathVariable UUID optId) {
        // margins will be returned if they were submitted upon creation of optimization session
        OptimizationResultDTO optimizationResult =
                optimizationService.getOptimizationResult(optId);

        ResponseEntity<OptimizationResultDTO> response;

        if (optimizationResult != null) {
            response = new ResponseEntity<>(optimizationResult, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
    }

    @ApiOperation(value = "Delete the result of an optimization, if available, and the optimization data.")
    @GetMapping(path = {"/optimizations/{optId}/remove"}, produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<Void> deleteOptimizationResult(@PathVariable UUID optId) {
    	ResponseEntity<Void> optimizationResponse;

        if (!optimizationService.existsOptimizationWithId(optId)) {
            logger.info("Optimization with id " + optId + " not found.");
            optimizationResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            boolean removed = optimizationService.deleteOptimizationResult(optId);

            optimizationResponse = new ResponseEntity<>(HttpStatus.ACCEPTED);

            if (removed) {
            	logger.info("Optimization + result with id " + optId + " has successfully been removed.");
            } else {
            	logger.info("Optimization result with id " + optId + " has not been found.");
            	if (!(optimizationService.existsOptimizationWithId(optId))) {
            		logger.info("Optimization with id " + optId + " has been removed.");
            	}
            }
        }

        return optimizationResponse;
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
        OptimizationStatisticsDTO optimizationStatistics = optimizationService.getOptimizationStatistics(optId);

        ResponseEntity<OptimizationStatisticsDTO> response;

        if (optimizationStatistics != null) {
            response = new ResponseEntity<>(optimizationStatistics, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
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
        // include margins as well (only for development purposes, not meant as production feature)
        OptimizationDTO optimization =
                optimizationService.getOptimization(optId);

        ResponseEntity<OptimizationDTO> response;

        if (optimization != null) {
            response = new ResponseEntity<>(optimization, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
    }
}
