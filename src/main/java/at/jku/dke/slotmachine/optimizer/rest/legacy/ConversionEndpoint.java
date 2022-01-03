package at.jku.dke.slotmachine.optimizer.rest.legacy;

import at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.MarginsDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

@Api(value = "SlotMachine Optimization Legacy")
@RestController
public class ConversionEndpoint {
    private static final Logger logger = LogManager.getLogger();

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

                        Optional<at.jku.dke.slotmachine.optimizer.rest.legacy.MarginsDTO> oldMargins =
                                Arrays.stream(input.getMargins()).filter(marginsOpt -> marginsOpt.getFlightId().equals(flight.getFlightId())).findFirst();

                        if(oldMargins.isPresent()) {
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
}