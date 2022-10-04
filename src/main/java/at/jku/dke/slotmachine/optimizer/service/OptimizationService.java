package at.jku.dke.slotmachine.optimizer.service;

import at.jku.dke.slotmachine.optimizer.OptimizerApplication;
import at.jku.dke.slotmachine.optimizer.Utils;
import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.*;
import at.jku.dke.slotmachine.optimizer.optimization.hungarian.HungarianOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.service.dto.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class OptimizationService {
	private static final Logger logger = LogManager.getLogger();

	private final Map<UUID, OptimizationDTO> optimizationDTOs;
	private final Map<UUID, Optimization> optimizations;
	private final Map<UUID, Future<OptimizationResultDTO>> threads;

	private final PrivacyEngineService privacyEngineService;

	public OptimizationService(PrivacyEngineService privacyEngineService) {
		this.privacyEngineService = privacyEngineService;

		this.optimizationDTOs = new ConcurrentHashMap<>();
		this.optimizations = new ConcurrentHashMap<>();
		this.threads = new ConcurrentHashMap<>();
	}

	/**
	 * Create the optimization and initialize it with the given data.
	 * @param optimizationDto data for the optimization session
	 * @return information about the optimization
	 */
	public OptimizationDTO createAndInitializeOptimization(final OptimizationDTO optimizationDto)
			throws ClassNotFoundException, InvocationTargetException,
				   InstantiationException, IllegalAccessException,
			       NoSuchMethodException, InvalidOptimizationParameterTypeException {
		logger.info("Starting process to initialize optimization session.");

		UUID optId = optimizationDto.getOptId();

		// remove existing optimization if same optimization id is used twice
		if(this.optimizations.containsKey(optId)) {
			logger.info("Found duplicate optimization entry for optimization with id " + optId + ". Deleting old entry.");
			optimizationDTOs.remove(optId);
			optimizations.remove(optId);
		}

		try {
			logger.info("Read the factory class from the JSON properties file.");
			String factoryClasses = System.getProperty(OptimizerApplication.FACTORY_PROPERTY);

			String className =
				Utils.getMapFromJson(factoryClasses).get(optimizationDto.getOptimizationFramework());

			OptimizationFactory factory;

			try {
				logger.info("Instantiate " + className + " for optimization framework " + optimizationDto.getOptimizationFramework());
				factory = (OptimizationFactory) Class.forName(className).getDeclaredConstructor().newInstance();
			} catch (ClassNotFoundException |
					InvocationTargetException |
					InstantiationException |
					IllegalAccessException |
					NoSuchMethodException e) {
				logger.error("Could not instantiate optimization factory.", e);
				throw e;
			}

			// get flights array from the DTO
			// preserve original order (important to receive correct results from Privacy Engine)
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
				if (optimizationDto.getParameters() != null) {
					newOptimization = factory.createOptimization(flights, slots, optimizationDto.getParameters());
				} else {
					newOptimization = factory.createOptimization(flights, slots);
				}

				newOptimization.setOptId(optId);
				// set initial flight sequence
				newOptimization.setInitialFlightSequence(optimizationDto.getInitialFlightSequence());

				// set the benchmarking mode (whether evolution of fitness is tracked)
				newOptimization.setTraceFitnessEvolution(optimizationDto.isTraceFitnessEvolution());

				if(optimizationDto.isTraceFitnessEvolution()) {
					newOptimization.getStatistics().setFitnessEvolution(new LinkedList<>());
				}

				// set the creation time in the optimization's statistics
				newOptimization.getStatistics().setTimeCreated(LocalDateTime.now());

				logger.info("Store optimization " + optId + " for later invocation");
				optimizations.put(optId, newOptimization);

				String estimatorName = optimizationDto.getFitnessEstimator();
				String estimatorClassName;

				logger.info("Read the fitness estimator class from the JSON properties file.");
				String estimatorClasses = System.getProperty(OptimizerApplication.FITNESS_ESTIMATOR);

				if(estimatorName != null) {
					estimatorClassName =
							Utils.getMapFromJson(estimatorClasses).get(optimizationDto.getFitnessEstimator());

					try {
						logger.info("Setting fitness estimator to " + estimatorClassName);
						FitnessEstimator estimator =
								(FitnessEstimator) Class.forName(estimatorClassName).getDeclaredConstructor().newInstance();

						newOptimization.setFitnessEstimator(estimator);
					} catch (ClassNotFoundException |
							InvocationTargetException |
							InstantiationException |
							IllegalAccessException |
							NoSuchMethodException e) {
						logger.error("Could not instantiate fitness estimator.", e);
						throw e;
					}
				} else {
					logger.info("No fitness estimator specified. Trying to use absolute fitness.");
				}

				if (optimizationDto.getOptimizationMode() == OptimizationModeEnum.PRIVACY_PRESERVING) {
					String privacyEngineEndpoint = optimizationDto.getPrivacyEngineEndpoint();
					logger.info("Set the endpoint URI for connection with the PrivacyEngine: " + privacyEngineEndpoint);
					newOptimization.setPrivacyEngineEndpoint(privacyEngineEndpoint);

					logger.debug("Setting the Privacy Engine service using the class " + this.privacyEngineService.getClass());
					newOptimization.setPrivacyEngineService(this.privacyEngineService);

					logger.info("Setting optimization mode to PRIVACY_PRESERVING.");
					newOptimization.setMode(OptimizationMode.PRIVACY_PRESERVING);
				} else if (optimizationDto.getOptimizationMode() == OptimizationModeEnum.NON_PRIVACY_PRESERVING) {
					logger.info("Setting optimization mode to NON_PRIVACY_PRESERVING.");
					newOptimization.setMode(OptimizationMode.NON_PRIVACY_PRESERVING);
				} else if (optimizationDto.getOptimizationMode() == OptimizationModeEnum.DEMONSTRATION) {
					logger.info("Setting optimization mode to DEMONSTRATION.");
					newOptimization.setMode(OptimizationMode.DEMONSTRATION);
				} else if (optimizationDto.getOptimizationMode() == OptimizationModeEnum.BENCHMARKING) {
					logger.info("Setting optimization mode to BENCHMARKING.");
					newOptimization.setMode(OptimizationMode.BENCHMARKING);
				}

				if(optimizationDto.getFitnessMethod() != null) {
					logger.info("Set fitness method: " + optimizationDto.getFitnessMethod());
					switch(optimizationDto.getFitnessMethod()) {
						case FITNESS_RANGE_QUANTILES -> {
							newOptimization.setFitnessMethod(FitnessMethod.FITNESS_RANGE_QUANTILES);
						}
						case ABOVE_RELATIVE_THRESHOLD -> {
							newOptimization.setFitnessMethod(FitnessMethod.ABOVE_RELATIVE_THRESHOLD);
						}
						case ABOVE_ABSOLUTE_THRESHOLD -> {
							newOptimization.setFitnessMethod(FitnessMethod.ABOVE_ABSOLUTE_THRESHOLD);
						}
						case ORDER -> {
							newOptimization.setFitnessMethod(FitnessMethod.ORDER);
						}
						case ORDER_QUANTILES -> {
							newOptimization.setFitnessMethod(FitnessMethod.ORDER_QUANTILES);
						}
						case ACTUAL_VALUES -> {
							newOptimization.setFitnessMethod(FitnessMethod.ACTUAL_VALUES);
						}
					}
				}

				// set initial solution's fitness in optimization statistics; only available in non-privacy-preserving mode
				logger.info("Initial flight sequence: ");
				StringBuilder sb = new StringBuilder();
				sb.append("\t[");
				for(var flight : newOptimization.getInitialFlightSequence()){
					sb.append(flight).append(",");
				}
				sb.append("]\n");
				logger.info(sb.toString());

				if(optimizationDto.getOptimizationMode() == OptimizationModeEnum.BENCHMARKING ||
						optimizationDto.getOptimizationMode() == OptimizationModeEnum.DEMONSTRATION ||
						optimizationDto.getOptimizationMode() == OptimizationModeEnum.NON_PRIVACY_PRESERVING
				) {
					int initialFitness = newOptimization.computeInitialFitness();

					newOptimization.getStatistics().setInitialFitness(initialFitness);
				}

				if(optimizationDto.getOptimizationMode() == OptimizationModeEnum.BENCHMARKING ||
						optimizationDto.getOptimizationMode() == OptimizationModeEnum.DEMONSTRATION) {
					logger.info("Get theoretical maximum fitness by running the Hungarian algorithm before the actual optimization.");

					HungarianOptimization hungarianOptimization = new HungarianOptimization(flights, slots);
					var optimalSolution = hungarianOptimization.run();
					logger.info("Checking if optimal solution produced by Hungarian is valid.");
					var invalidMappings = optimalSolution.entrySet().stream().filter(e -> e.getKey().getScheduledTime() != null && e.getValue().getTime().isBefore(e.getKey().getScheduledTime())).count();
					logger.info("Solution contains {} assignments where the scheduled time of the flight is available and after the assigned slots' time.", invalidMappings);

					double theoreticalMaximumFitness = hungarianOptimization.getStatistics().getResultFitness();

					newOptimization.setTheoreticalMaximumFitness(theoreticalMaximumFitness);
					newOptimization.getConfiguration().setParameter("theoreticalMaximumFitness", theoreticalMaximumFitness);
				}

				if(optimizationDto.getFitnessPrecision() != null) {
					logger.info("Set fitness precision: " + optimizationDto.getFitnessPrecision());
					newOptimization.setFitnessPrecision(optimizationDto.getFitnessPrecision());
				}

				logger.info("Set optimization status to INITIALIZED");
				newOptimization.setStatus(OptimizationStatus.INITIALIZED);
				optimizationDto.setOptimizationStatus(OptimizationStatusEnum.INITIALIZED);

				// set the timestamp to indicate to the caller when the information was created
				optimizationDto.setTimestamp(LocalDateTime.now());
			} catch (InvalidOptimizationParameterTypeException e) {
				logger.error("Could not create optimization due to error in parameters.", e);
				throw e;
			}
		} catch (ClassNotFoundException |
				InvocationTargetException |
				InstantiationException |
				IllegalAccessException |
				NoSuchMethodException e) {
			logger.info("Could not create optimization with id " + optId);
			throw e;
		}

		// keep the DTO for later
		optimizationDTOs.put(optId, optimizationDto);

		if(logger.isDebugEnabled()) {
			logger.debug("Listing available optimization sessions ...");
			for (Optimization o : optimizations.values()) {
				logger.debug(o.getOptId().toString());
			}
		}

		return optimizationDto;
	}
	
	/**
	 * Start the optimization run. The method runs asynchronously in a separate thread.
	 * @param optId optId of the optimization session
	 */
	@Async("threadPoolTaskExecutor")
	public Future<OptimizationResultDTO> runOptimizationAsynchronously(UUID optId) {
		logger.info("Current thread: " + Thread.currentThread());

		OptimizationResultDTO optimizationResultDto = this.runOptimization(optId);

		return new AsyncResult<>(optimizationResultDto);
	}

	/**
	 * Returns the result of the optimization, if already available.
	 * @param optId the optimization identifier
	 * @param noOfSolutions the number of solutions to be retrieved
	 * @return the result of the optimization
	 */
	public OptimizationResultDTO[] getOptimizationResult(UUID optId, int noOfSolutions) {
		Optimization optimization = this.optimizations.get(optId);
		List<OptimizationResultDTO> results = new LinkedList<>();

		if(optimization != null) {
			Map<Flight, Slot>[] resultMaps = optimization.getResults();

			if(resultMaps != null) {
				for(int i = 0; i < resultMaps.length && i < noOfSolutions; i++) {
					Map<Flight, Slot> resultMap = resultMaps[i];
					results.add(this.convertResultMapToOptimizationResultMapDto(optId, resultMap));

					logger.info("Checking if result " + i + " is invalid ...");
					int invalidCount = 0;
					for (Flight f : resultMap.keySet()) {
						if (f.getScheduledTime() != null && f.getScheduledTime().isAfter(resultMap.get(f).getTime())) {
							invalidCount++;
							logger.info("Flight " + f.getFlightId() + " with scheduled time " + f.getScheduledTime() +" at Slot " + resultMap.get(f).getTime());
						}
					}

					if(invalidCount > 0) {
						logger.info("Solution " + i + " is invalid. Number of invalid assignments: " + invalidCount);
					} else {
						logger.info("Solution " + i + " is valid.");
					}


					if(i == 0) {
						// For the best result, we know the fitness
						logger.info("Set fitness of solution " + i + " to " + optimization.getMaximumFitness());
						results.get(i).setFitness(optimization.getStatistics().getResultFitness());
					} else{
						if(i == 1) logger.info("Setting fitness values of all returned solutions.");
						results.get(i).setFitness(optimization.getFitnessValuesResults() != null && optimization.getFitnessValuesResults().size() > i ?
								optimization.getFitnessValuesResults().get(i)
								: 0.0);
					}
					results.get(i).setOptimizedFlightSequenceIndexes(optimization.getConvertedResults()[i]);
				}
			}
		}

		return results.toArray(OptimizationResultDTO[]::new);
	}
	
	/**
	 * Deletes an optimization and all its associated data. If the optimization is currently running, the optimization
	 * will be aborted.
	 * @param optId the optimization identifier
	 */
	public OptimizationDTO deleteOptimization(UUID optId) {
		OptimizationDTO optimizationDto = optimizationDTOs.remove(optId);
		Optimization optimization = optimizations.remove(optId);
		
		if(optimization.getStatus() == OptimizationStatus.RUNNING) {
			this.abortOptimization(optId);
		}

		return optimizationDto;
	}

	/**
	 * Determines whether an optimization with the argument optimization identifier already exists.
	 * @param optId the optimization identifier
	 * @return true if the optimization exists; false otherwise.
	 */
	public boolean existsOptimization(UUID optId) {
		return optimizations.get(optId) != null;
	}

	/**
	 * Returns an optimization DTO with the specified identifier.
	 * @param optId the identifier of the optimization
	 * @return the optimization DTO with the specified identifier, if it exists; null otherwise.
	 */
	public OptimizationDTO getOptimization(UUID optId) {
		OptimizationDTO optimizationDto = optimizationDTOs.get(optId);
		Optimization optimization = optimizations.get(optId);

		if(optimization != null && optimizationDto != null) {
			switch (optimization.getStatus()) {
				case CREATED -> optimizationDto.setOptimizationStatus(OptimizationStatusEnum.CREATED);
				case INITIALIZED -> optimizationDto.setOptimizationStatus(OptimizationStatusEnum.INITIALIZED);
				case RUNNING -> optimizationDto.setOptimizationStatus(OptimizationStatusEnum.RUNNING);
				case CANCELLED -> optimizationDto.setOptimizationStatus(OptimizationStatusEnum.CANCELLED);
				case DONE -> optimizationDto.setOptimizationStatus(OptimizationStatusEnum.DONE);
			}

			optimizationDto.setTimestamp(LocalDateTime.now());
		} else{
			optimizationDto = null;
			logger.info("Optimization with id " + optId + " not found.");
		}

		return optimizationDto;
	}

	/**
	 * Get the current statistics for an optimization. Statistics are updated constantly during the optimization run.
	 * @param optId the optimization id
	 * @return the current optimization statistics
	 */
	public OptimizationStatisticsDTO getOptimizationStatistics(UUID optId) {
		// search for optId
		Optimization optimization = this.optimizations.get(optId);
		if (optimization == null) {
			logger.info("Optimization with id " + optId + " not found.");
			return null;
		}

		OptimizationStatisticsDTO stats = new OptimizationStatisticsDTO();

		stats.setOptId(optimization.getOptId().toString());

		stats.setRequestTime(LocalDateTime.now());

		switch(optimization.getStatus()) {
			case CREATED -> {
				stats.setStatus(OptimizationStatusEnum.CREATED);
			}
			case INITIALIZED -> {
				stats.setStatus(OptimizationStatusEnum.INITIALIZED);
			}
			case RUNNING -> {
				stats.setStatus(OptimizationStatusEnum.RUNNING);
			}
			case CANCELLED -> {
				stats.setStatus(OptimizationStatusEnum.CANCELLED);
			}
			case DONE -> {
				stats.setStatus(OptimizationStatusEnum.DONE);
			}
		}

		stats.setTimeCreated(optimization.getStatistics().getTimeCreated());
		stats.setTimeStarted(optimization.getStatistics().getTimeStarted());
		stats.setTimeFinished(optimization.getStatistics().getTimeFinished());
		stats.setTimeAborted(optimization.getStatistics().getTimeAborted());
		stats.setDuration(optimization.getStatistics().getDuration());

		stats.setIterations(optimization.getStatistics().getIterations());
		stats.setResultFitness(optimization.getStatistics().getResultFitness());

		if(optimization.getMode() == OptimizationMode.BENCHMARKING ||
		   optimization.getMode() == OptimizationMode.DEMONSTRATION) {
			stats.setTheoreticalMaximumFitness(optimization.getTheoreticalMaximumFitness()); // take theoretical maximum fitness
		}

		stats.setInitialFitness(optimization.getStatistics().getInitialFitness());

		if(optimization.isTraceFitnessEvolution()) {
			logger.debug("Tracing fitness evolution: include fitness evolution in statistics.");
			stats.setFitnessEvolution(
					optimization.getStatistics().getFitnessEvolution().stream()
							.map(fitnessEvolutionStep -> {
								FitnessEvolutionStepDTO newStep = new FitnessEvolutionStepDTO();

								newStep.setGeneration(fitnessEvolutionStep.getGeneration());

								if(fitnessEvolutionStep.getEstimatedPopulation() != null)newStep.setEstimatedPopulation(
										Arrays.stream(fitnessEvolutionStep.getEstimatedPopulation()).mapToDouble(Double::doubleValue).toArray()
								);

								if(fitnessEvolutionStep.getEvaluatedPopulation() != null)newStep.setEvaluatedPopulation(
										Arrays.stream(fitnessEvolutionStep.getEvaluatedPopulation()).mapToDouble(Double::doubleValue).toArray()
								);

								return newStep;
							}).toArray(FitnessEvolutionStepDTO[]::new)
			);
		}

		return stats;
	}

	/**
	 * Runs the optimization with the specified framework and parameters.
	 * @param optId the optimization identifier
	 * @return the best solution found by the optimization
	 */
	public OptimizationResultDTO runOptimization(UUID optId) {
		logger.info("Current thread: " + Thread.currentThread());

		// search for optId
		Optimization optimization = this.optimizations.get(optId);

		Map<Flight,Slot> resultMap;

		OptimizationResultDTO optimizationResultDto = null;

		if(optimization != null) {
			// set optimization status to running
			optimization.setStatus(OptimizationStatus.RUNNING);

			logger.info("Starting optimization " + optId + " and running optimization algorithm.");
			resultMap = optimization.run();

			logger.info("Optimization " + optId + " has finished.");
			if(optimization.getStatus() != OptimizationStatus.CANCELLED) {
				optimization.setStatus(OptimizationStatus.DONE);
			}

			logger.info("Convert the result map into the required format.");
			optimizationResultDto = this.convertResultMapToOptimizationResultMapDto(optId, resultMap);

			// get the fitness and fitness function invocations from the statistics and include it in the results
			logger.info("Including basic statistics in the response.");
			optimizationResultDto.setFitness(optimization.getStatistics().getResultFitness());
		} else {
			logger.info("Optimization " + optId + " not found.");
		}

		return optimizationResultDto;
	}

	/**
	 * Create a new instance from a result map between flights and slots.
	 * @param optId the optimization identifier
	 * @param resultMap a mapping between flights and slots
	 * @return an OptimizationResultDTO based on the input mapping
	 */
	public OptimizationResultDTO convertResultMapToOptimizationResultMapDto(UUID optId, Map<Flight, Slot> resultMap) {
		// sort the flights by slot instant
		String[] optimizedFlightSequence = resultMap.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.map(Flight::getFlightId)
				.toArray(String[]::new);

		LocalDateTime[] slots = resultMap.values().stream().sorted().map(Slot::getTime).toArray(LocalDateTime[]::new);

		return new OptimizationResultDTO(optId, optimizedFlightSequence, slots);
	}

	/**
	 * Abort the optimization with the specified identifier
	 * @param optId the optimization identifier
	 */
	public void abortOptimization(UUID optId) {
		Future<OptimizationResultDTO> future = this.threads.get(optId);
        Optimization optimization = this.optimizations.get(optId);

		logger.info("Cancel the running optimization " + optId);

		if(future != null && future.cancel(true)) {
            optimization.setStatus(OptimizationStatus.CANCELLED);
			logger.info("Cancellation successfully triggered.");

			optimization.getStatistics().setTimeAborted(LocalDateTime.now()); // set the abort time in the statistics
		} else {
			logger.info("Could not cancel.");
		}
	}

	/**
	 * Register the future of an optimization; this allows to abort the thread later.
	 * @param optId the optimization identifier
	 * @param future the future obtained from an asynchronous run
	 */
	public void registerThread(UUID optId, Future<OptimizationResultDTO> future) {
		logger.info("Registering future for optimization with id " + optId);
		this.threads.put(optId, future);
	}

	/**
	 * Return all the optimizations currently known to the Optimizer
	 * @return a list of optimizations
	 */
    public OptimizationDTO[] getOptimizations() {
		return this.optimizationDTOs.values().toArray(OptimizationDTO[]::new);
    }
}
