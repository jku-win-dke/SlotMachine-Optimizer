package at.jku.dke.slotmachine.optimizer.rest;

import at.jku.dke.slotmachine.optimizer.service.OptimizationService;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationResultDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatisticsDTO;

import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatusEnum;
import io.swagger.annotations.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;

/***
 * The OptimizationEndpoint relays the REST calls to the {@link at.jku.dke.slotmachine.optimizer.service.OptimizationService}
 * in order to initiate and manage an optimization.
 */
@Api(value = "SlotMachine Optimization")
@RestController
public class OptimizationEndpoint {
    private static final Logger logger = LogManager.getLogger();

    private final OptimizationService optimizationService;

    public OptimizationEndpoint(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @ApiOperation(
            value = "Create and initialize a (heuristic) optimization for flights and slots.",
            response = OptimizationDTO.class,
            produces = "application/json",
            consumes = "application/json"
    )
    @PostMapping(path = "/optimizations", produces = "application/json", consumes = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Created"),
                    @ApiResponse(code = 400, message = "Bad Request")
            }
    )
    public ResponseEntity<OptimizationDTO> createAndInitializeOptimization(@RequestBody OptimizationDTO optimization) {
        logger.debug("Initializing optimization using the following parameters: " + optimization.toString());

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



    @ApiOperation(
            value = "Get descriptions of all currently registered optimizations.",
            response = OptimizationDTO[].class,
            produces = "application/json"
    )
    @PostMapping(path = "/optimizations", consumes = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK")
            }
    )
    public ResponseEntity<OptimizationDTO[]> getOptimizations() {
        return null;
    }

    @ApiOperation(
            value = "Start a specific optimization that was previously created and initialized.",
            response = OptimizationDTO.class
    )
    @PutMapping(path = "/optimizations/{optId}/start", produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK; the optimization is already running."),
                    @ApiResponse(code = 202, message = "Accepted; if the optimization was successfully started."),
                    @ApiResponse(code = 303, message = "See Other; returns location of result in header, if cancelled or already done."),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationDTO> startOptimization(@PathVariable @ApiParam(value = "the optimization's identifier") UUID optId) {
        ResponseEntity<OptimizationDTO> optimizationResponse = null;

        if (!optimizationService.existsOptimization(optId)) {
            logger.info("Optimization with id " + optId + " not found.");
            optimizationResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            OptimizationDTO optimization = optimizationService.getOptimization(optId);

            if(optimization.getOptimizationStatus() == OptimizationStatusEnum.INITIALIZED) {
                Future<OptimizationResultDTO> future = optimizationService.runOptimizationAsynchronously(optId);

                // register the thread (future) with the optimization service so that abort works
                optimizationService.registerThread(optId, future);

                // get an updated optimization
                optimization = optimizationService.getOptimization(optId);
                optimization.setOptimizationStatus(OptimizationStatusEnum.RUNNING);

                logger.info("The start of optimization with id " + optId + " was triggered.");
                optimizationResponse = new ResponseEntity<>(optimization, HttpStatus.ACCEPTED);
            } else {
                if(optimization.getOptimizationStatus() == OptimizationStatusEnum.DONE ||
                        optimization.getOptimizationStatus() == OptimizationStatusEnum.CANCELLED) {
                    logger.info("Optimization with id " + optId + " has finished running.");
                    optimizationResponse = new ResponseEntity<>(optimization, HttpStatus.SEE_OTHER);
                    ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("http://localhost:8080/" + "optimization/" + optId + "/result"));
                } else if(optimization.getOptimizationStatus() == OptimizationStatusEnum.RUNNING) {
                    logger.info("Optimization with id " + optId + " currently running.");
                    optimizationResponse = new ResponseEntity<>(optimization, HttpStatus.OK);
                }
            }
        }

        return optimizationResponse;
    }

    @ApiOperation(
            value = "Run a previously created and initialized optimization in a synchronized way, waiting for the response.",
            response = OptimizationResultDTO.class,
            produces = "application/json"
    )
    @PutMapping(path = "/optimizations/{optId}/start/wait", produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationResultDTO> startOptimizationSync(@PathVariable @ApiParam(value = "the optimization's identifier") UUID optId) {
        ResponseEntity<OptimizationResultDTO> optimizationResponse;

        if (!optimizationService.existsOptimization(optId)) {
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
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationDTO> abortOptimization(@PathVariable @ApiParam(value = "the optimization's identifier") UUID optId) {
        ResponseEntity<OptimizationDTO> response;

        OptimizationDTO optimization = optimizationService.getOptimization(optId);

        if(optimization != null) {
            optimizationService.abortOptimization(optId);

            // get updated optimization object with new status
            optimization = optimizationService.getOptimization(optId);

            response = new ResponseEntity<>(optimization, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
    }

    @ApiOperation(value = "Get the n best solutions found by an optimization run, if available.", response = OptimizationResultDTO[].class)
    @GetMapping(path = {"/optimizations/{optId}/result"}, produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found; no result available or optimization does not exist")
            }
    )
    public ResponseEntity<OptimizationResultDTO[]> getOptimizationResult(@PathVariable
                                                                         @ApiParam(value = "the optimization's identifier")
                                                                                 UUID optId,
                                                                         @RequestParam(name = "limit", defaultValue = "1")
                                                                         @ApiParam(value = "the number of solutions to be returned")
                                                                                 int noOfSolutions) {
        // margins will be returned if they were submitted upon creation of optimization session
        OptimizationResultDTO[] optimizationResult =
                optimizationService.getOptimizationResult(optId, noOfSolutions);

        ResponseEntity<OptimizationResultDTO[]> response;

        if (optimizationResult != null) {
            response = new ResponseEntity<>(optimizationResult, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
    }

    @ApiOperation(value = "Delete an optimization and its results, if available. Abort a running optimization. ")
    @DeleteMapping(path = {"/optimizations/{optId}"}, produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<Void> deleteOptimizationResult(@PathVariable @ApiParam(value = "the optimization's identifier") UUID optId) {
    	ResponseEntity<Void> optimizationResponse;

        if (!optimizationService.existsOptimization(optId)) {
            logger.info("Optimization with id " + optId + " not found.");
            optimizationResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            optimizationService.deleteOptimization(optId);

            optimizationResponse = new ResponseEntity<>(HttpStatus.ACCEPTED);
            logger.info("Optimization and result with id " + optId + " removed.");
        }

        return optimizationResponse;
    }

    @ApiOperation(value = "Get current statistics for a specific optimization and its results, if available.", response = OptimizationStatisticsDTO.class)
    @GetMapping(path = "/optimizations/{optId}/stats", produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationStatisticsDTO> getOptimizationStatistics(@PathVariable @ApiParam(value = "the optimization's identifier") UUID optId) {
        OptimizationStatisticsDTO optimizationStatistics = optimizationService.getOptimizationStatistics(optId);

        ResponseEntity<OptimizationStatisticsDTO> response;

        if (optimizationStatistics != null) {
            response = new ResponseEntity<>(optimizationStatistics, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
    }

    @ApiOperation(value = "Get the description of a specific optimization.", response = OptimizationDTO.class)
    @GetMapping(path = "/optimizations/{optId}", produces = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 404, message = "Not Found")
            }
    )
    public ResponseEntity<OptimizationDTO> getOptimization(@PathVariable @ApiParam(value = "the optimization's identifier") UUID optId) {
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
