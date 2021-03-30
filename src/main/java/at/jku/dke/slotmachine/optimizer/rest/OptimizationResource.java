package at.jku.dke.slotmachine.optimizer.rest;

import at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.OptaPlannerRun;
import at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationResultDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatisticsDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.jenetics.util.ISeq;
import io.swagger.annotations.*;

import java.time.Instant;
import java.util.*;

@Api(value = "SlotMachine Optimization")
@RestController
/***
 * The OptimizationResource relays the REST calls to {@link at.jku.dke.slotmachine.optimizer.service.OptimizationService}
 * to initiate and manage an optimization.
 */
public class OptimizationResource {
	// variable to store information
	private List<OptimizationDTO> optimizations;
	private List<OptimizationResultDTO> optimizationResults;
	
	private static final Logger logger = LogManager.getLogger();
	
	@ApiOperation(value = "Create and initialize a heuristic optimization with flights and preferences.", response = OptimizationDTO.class)
	@PostMapping(path = "/optimizations", consumes = "application/json")
	@ApiResponses(
		value = {
			@ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 400, message = "Bad Request")
		}
	)
	public ResponseEntity<OptimizationDTO> createAndInitializeOptimization(@RequestBody OptimizationDTO optimization) {
		ResponseEntity<OptimizationDTO> optimizationResponse = new ResponseEntity<OptimizationDTO>(optimization, HttpStatus.OK);
		if(optimizations == null) optimizations = new LinkedList<OptimizationDTO>();
		optimizations.add(optimization); //temporary storage variables
		return optimizationResponse;
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
		logger.info("Starting optimization and running optimization algorithm.");
		if(optimizationResults == null) optimizationResults = new LinkedList<OptimizationResultDTO>();
		ISeq<FlightDTO> flightISeq = ISeq.of(optimizations.get(0).getFlights());
		ISeq<SlotDTO> slotISeq = ISeq.of(optimizations.get(0).getSlots());
		//Map<FlightDTO, SlotDTO> resultMap = JeneticsApplication.run(flightISeq, slotISeq);
		List<FlightDTO> flightList = Arrays.asList(optimizations.get(0).getFlights());
		List<SlotDTO> slotList = Arrays.asList(optimizations.get(0).getSlots());
		Map<FlightDTO, SlotDTO> resultMap = OptaPlannerRun.run(flightList, slotList);
		
		logger.info("Preparing results.");
		String[] assignedSequence = new String[slotList.size()]; //due to perhaps different number of 
																 //flights and slots
		
		// get sorted list of slots (by time)
		List<Instant> sortedSlots = new LinkedList<Instant>();
		for (SlotDTO s: slotList) {
			sortedSlots.add(s.getTime());
		}
		Collections.sort(sortedSlots);
		
		// use sorted list to get an array of assigned flights for the given slots
		for(Map.Entry<FlightDTO, SlotDTO> entry: resultMap.entrySet()) {
			FlightDTO flight = entry.getKey();
			SlotDTO slot = entry.getValue();
			int posOfSlot = sortedSlots.indexOf(slot.getTime());
			assignedSequence[posOfSlot] = flight.getFlightId();
		}
		
		OptimizationResultDTO optResult = new OptimizationResultDTO();
		optResult.setOptId(optId);
		optResult.setFlightSequence(assignedSequence);
		logger.info("Storing results.");
		optimizationResults.add(optResult);
		
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
		if (optimizationResults != null) {
			
			for (OptimizationResultDTO optRes: optimizationResults) {
				if (optId.equals(optRes.getOptId())) {
					ResponseEntity<OptimizationResultDTO> response = new ResponseEntity<OptimizationResultDTO>(optRes, HttpStatus.OK);
					logger.info("Returning results for this UUID.");
					return response;
				}
			}
		}
		logger.info("No results to return for this UUID.");
		ResponseEntity<OptimizationResultDTO> response = new ResponseEntity<OptimizationResultDTO>(HttpStatus.NOT_FOUND);
		return response;
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
