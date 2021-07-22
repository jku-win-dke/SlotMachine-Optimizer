package at.jku.dke.slotmachine.optimizer.service;

import at.jku.dke.slotmachine.optimizer.OptimizerApplication;
import at.jku.dke.slotmachine.optimizer.Utils;
import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;
import at.jku.dke.slotmachine.optimizer.service.dto.MarginsDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationResultDTO;

import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationStatisticsDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OptimizationService {
	private static final Logger logger = LogManager.getLogger();

	private final Map<UUID, OptimizationDTO> optimizationDTOs;
	private final Map<UUID, OptimizationResultDTO> optimizationResultDTOs;
	private final Map<UUID, Optimization> optimizations;

	public OptimizationService() {
		this.optimizationDTOs = new HashMap<>();
		this.optimizationResultDTOs = new HashMap<>();
		this.optimizations = new HashMap<>();
	}

	/**
	 * Creates the optimization session and initializes it with the given data.
	 * @param optimizationDto data for the optimization session
	 * @return information about the optimization (unaltered input DTO, for now)
	 */
	public OptimizationDTO createAndInitializeOptimization(final OptimizationDTO optimizationDto) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvalidOptimizationParameterTypeException {
		logger.info("Starting process to initialize optimization session.");

		UUID optId = optimizationDto.getOptId();

		// remove existing optimization if same optimization id is used twice
		if(this.optimizations.containsKey(optId)) {
			logger.info("Found duplicate optimization entry for optimization with id " + optId + ". Deleting old entry.");
			optimizationDTOs.remove(optId);
			optimizations.remove(optId);
			optimizationResultDTOs.remove(optId);
		}

		try {
			logger.info("Read the factory class from the JSON properties file.");
			String factoryClasses = System.getProperty(OptimizerApplication.FACTORY_PROPERTY);

			String className =
				Utils.getMapFromJson(factoryClasses).get(optimizationDto.getOptimizationFramework());

			logger.info("Instantiate " + className + " for optimization framework " + optimizationDto.getOptimizationFramework());
			OptimizationFactory factory =
				(OptimizationFactory) Class.forName(className).getDeclaredConstructor().newInstance();

			// get flights array from the DTO
			Flight[] flights = Arrays.stream(optimizationDto.getFlights())
					.map(f -> new Flight(f.getFlightId(), f.getScheduledTime(), f.getWeightMap()))
					.toArray(Flight[]::new);

			// get slots array from the DTO
			Slot[] slots = Arrays.stream(optimizationDto.getSlots())
					.map(s -> new Slot(s.getTime()))
					.toArray(Slot[]::new);

			Optimization newOptimization;
			try {
				logger.info("Create a new optimization with the specified characteristics");
				if(optimizationDto.getParameters() != null) {
					newOptimization = factory.createOptimization(flights, slots, optimizationDto.getParameters());
				} else {
					newOptimization = factory.createOptimization(flights, slots);
				}

				newOptimization.setOptId(optId);

				logger.info("Store optimization " + optId + " for later invocation");
				optimizations.put(optId, newOptimization);
			} catch (InvalidOptimizationParameterTypeException e) {
				logger.error("Could not create optimization due to error in parameters.", e);
				throw e;
			}
		} catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			logger.error("Could not instantiate optimization factory.", e);
			throw e;
		}

		// keep the DTO for later
		optimizationDTOs.put(optId, optimizationDto);

		if(logger.isDebugEnabled()) {
			logger.debug("Listing available optimization sessions ...");
			for (Optimization o : optimizations.values()) {
				logger.debug(o.getOptId().toString());

				if (optimizationResultDTOs.containsKey(optId)) {
					logger.debug("Results for optimization with id " + optId + " are available.");
				}
			}
		}

		return optimizationDto;
	}
	
	/**
	 * Start the optimization session. The method runs asynchronously in a separate thread.
	 * @param optId optId of the optimization session
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<OptimizationResultDTO> runOptimizationAsynchronously(UUID optId) {
		OptimizationResultDTO optimizationResultDto = this.runOptimization(optId);

		return CompletableFuture.completedFuture(optimizationResultDto);
	}

	/**
	 * Returns the result of the optimization, if already available.
	 * @param optId the optimization identifier
	 * @return the result of the optimization
	 */
	public OptimizationResultDTO getOptimizationResult(UUID optId) {
		// TODO retrieve intermediate result if the optimization has not finished
		OptimizationResultDTO optimizationResultDto = optimizationResultDTOs.get(optId);

		if(optimizationResultDto == null) {
			logger.info("No result found for " + optId);
		}

		return optimizationResultDto;
	}
	
	/**
	 * Deletes the result of the optimiziation, if already available and the optimization data as well
	 * @param optId the optimization identifier
	 * @return nothing
	 */
	public boolean deleteOptimizationResult(UUID optId) {
		OptimizationResultDTO optimizationResultDto = optimizationResultDTOs.remove(optId);
		OptimizationDTO optimizationDto = optimizationDTOs.remove(optId); //already checked at OptimizationResource.java
		Optimization optimizationValue = optimizations.remove(optId);
		
		if (optimizationResultDto == null || optimizationDto == null || optimizationValue == null) {
			return false;
		}
		
		return true;
	}

	public boolean existsOptimizationWithId(UUID optId) {
		return optimizations.get(optId) != null;
	}

	/**
	 * Returns an optimization DTO with the specified identifier.
	 * @param optId the identifier of the optimization
	 * @return the optimization DTO with the specified identifier, if it exists; null otherwise.
	 */
	public OptimizationDTO getOptimization(UUID optId) {
		return optimizationDTOs.get(optId);
	}

	public OptimizationStatisticsDTO getOptimizationStatistics(UUID optId) {
		// TODO implement the retrieval of statistics

		return null;
	}

	public OptimizationResultDTO runOptimization(UUID optId) {
		// search for optId
		Optimization optimization = this.optimizations.get(optId);
		OptimizationDTO optimizationDto = this.optimizationDTOs.get(optId);

		Map<Flight,Slot> resultMap;

		OptimizationResultDTO optimizationResultDto = null;

		if(optimization != null) {
			logger.info("Starting optimization " + optId + " and running optimization algorithm.");
			resultMap = optimization.run();

			logger.info("Optimization " + optId + " has finished.");

			logger.info("Convert the result map into the required format.");
			optimizationResultDto = OptimizationResultDTO.fromResultMap(optId, resultMap);

			MarginsDTO[] margins = optimizationDto.getMargins();
			if(margins != null) {
				logger.info("Since the margins were in the original submission include the margins also in the result.");
				optimizationResultDto.setMargins(margins);
			}

			// get the fitness from the statistics and include it in the results
			logger.info("Including basic statistics in the response.");
			optimizationResultDto.setFitness(optimization.getStatistics().getSolutionFitness());
			optimizationResultDto.setFitnessFunctionInvocations(optimization.getStatistics().getFitnessFunctionInvocations());

			logger.info("Register the result for optimization " + optId + ".");
			optimizationResultDTOs.put(optId, optimizationResultDto);
		} else {
			logger.info("Optimization " + optId + " not found.");
		}

		return optimizationResultDto;
	}
}
