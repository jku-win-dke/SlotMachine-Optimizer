package at.jku.dke.slotmachine.optimizer.rest.legacy;

import at.jku.dke.slotmachine.optimizer.service.OptimizationService;
import at.jku.dke.slotmachine.optimizer.service.PrivacyEngineService;
import at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.MarginsDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationModeEnum;
import io.swagger.annotations.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

@Api(value = "SlotMachine Optimization Legacy")
@RestController
public class LegacyEndpoint {
    private static final Logger logger = LogManager.getLogger();
    
    @Autowired
    OptimizationService optimizationService;

    @ApiOperation(
            value = "Convert an optimization in the legacy representation to the new format.",
            response = at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO.class,
            produces = "application/json",
            consumes = "application/json"
    )
    @PostMapping(path = "/conversion/optimizations", produces = "application/json", consumes = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Bad Request")
            }
    )
    public ResponseEntity<at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO> convertOptimization(@RequestBody OptimizationDTO input) {
        ResponseEntity<at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO> optimizationResponse;

        try {
            at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO optimizationDto =
                    new at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO();

            logger.info("Converting base data ...");
            optimizationDto.setOptId(input.getOptId());

            optimizationDto.setInitialFlightSequence(input.getInitialFlightSequence());
            optimizationDto.setFitnessEstimator(input.getFitnessEstimator());
            optimizationDto.setParameters(input.getParameters());
            optimizationDto.setOptimizationFramework(input.getOptimizationFramework());
            optimizationDto.setOptimizationMode(input.getOptimizationMode());
            optimizationDto.setPrivacyEngineEndpoint(input.getPrivacyEngineEndpoint());

            logger.info("Done. Converting flights ...");

            at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO[] flights = Arrays.stream(input.getFlights())
                    .map(flight -> {
                        FlightDTO newFlight = new FlightDTO();

                        newFlight.setFlightId(flight.getFlightId());
                        newFlight.setScheduledTime(LocalDateTime.ofInstant(flight.getScheduledTime(), ZoneOffset.UTC));
                        newFlight.setWeightMap(flight.getWeightMap());

                        Optional<at.jku.dke.slotmachine.optimizer.rest.legacy.MarginsDTO> oldMargins = null;

                        if(input.getMargins() != null) {
                            oldMargins =
                                    Arrays.stream(input.getMargins()).filter(marginsOpt -> marginsOpt.getFlightId().equals(flight.getFlightId())).findFirst();
                        }

                        if(oldMargins != null && oldMargins.isPresent()) {
                            MarginsDTO newMargins =
                                    new MarginsDTO();

                            newMargins.setScheduledTime(LocalDateTime.ofInstant(oldMargins.get().getScheduledTime(), ZoneOffset.UTC));
                            newMargins.setTimeWished(LocalDateTime.ofInstant(oldMargins.get().getTimeWished(), ZoneOffset.UTC));
                            newMargins.setTimeNotBefore(LocalDateTime.ofInstant(oldMargins.get().getTimeNotBefore(), ZoneOffset.UTC));
                            newMargins.setTimeNotAfter(LocalDateTime.ofInstant(oldMargins.get().getTimeNotAfter(), ZoneOffset.UTC));

                            newFlight.setMargins(newMargins);
                        }

                        return newFlight;
                    })
                    .toArray(at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO[]::new);

            optimizationDto.setFlights(flights);

            logger.info("Done. Converting slots ...");

            at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO[] slots = Arrays.stream(input.getSlots())
                    .map(slot -> {
                        at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO newSlot =
                                new at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO();

                        newSlot.setTime(LocalDateTime.ofInstant(slot.getTime(), ZoneOffset.UTC));

                        return newSlot;
                    })
                    .toArray(at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO[]::new);

            optimizationDto.setSlots(slots);

            logger.info("Done.");

            optimizationResponse = new ResponseEntity<>(optimizationDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Couldn't finish conversion.", e);
            optimizationResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return optimizationResponse;
    }

    @ApiOperation(
            value = "Create an optimization session with an optimization in the legacy representation as input.",
            response = at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO.class,
            produces = "application/json",
            consumes = "application/json"
    )
    @PostMapping(path = "/legacy/optimizations", produces = "application/json", consumes = "application/json")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Bad Request")
            }
    )
    public ResponseEntity<at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO> createAndInitializeOptimizationLegacy(
            @RequestBody OptimizationDTO input,
            @RequestParam(name = "optimizationMode", required = false)
            @ApiParam(value = "the optimization mode")
            OptimizationModeEnum optimizationMode,
            @RequestParam(name = "fitnessEstimator", required = false)
            @ApiParam(value = "the fitness estimator used to determine the fitness (logarithmic, sigmoid, linear, etc.)")
            String fitnessEstimator,
            @RequestParam(name = "traceFitnessEvolution", defaultValue = "true")
            @ApiParam(value = "true if evolution of fitness should be included in stats; used for evaluation.")
            boolean traceFitnessEvolution
    ) {
        ResponseEntity<at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO> optimizationResponse = null;

    	at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO optDto = null;
    	
    	ResponseEntity<at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO> optimizationConvertResponse =
                this.convertOptimization(input);

    	if (optimizationConvertResponse.getStatusCode().equals(HttpStatus.OK)) {
    		optDto = optimizationConvertResponse.getBody();
    	} else {
            optimizationResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}

        if(optimizationResponse == null) {
            if (optimizationMode != null) {
                optDto.setOptimizationMode(optimizationMode);
            }

            if (fitnessEstimator != null) {
                optDto.setFitnessEstimator(fitnessEstimator);
            }

            optDto.setTraceFitnessEvolution(traceFitnessEvolution);

            try {
                at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO optimizationDto =
                        optimizationService.createAndInitializeOptimization(optDto);

                optimizationResponse = new ResponseEntity<>(optimizationDto, HttpStatus.OK);

            } catch (Exception e) {
                optimizationResponse = new ResponseEntity<>(optDto, HttpStatus.BAD_REQUEST);
            }
        }

        return optimizationResponse;
    }
}
