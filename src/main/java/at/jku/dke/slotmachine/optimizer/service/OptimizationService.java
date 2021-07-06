package at.jku.dke.slotmachine.optimizer.service;

import at.jku.dke.slotmachine.optimizer.OptimizerApplication;
import at.jku.dke.slotmachine.optimizer.Utils;
import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationFactory;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;
import at.jku.dke.slotmachine.optimizer.service.dto.MarginsDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationResultDTO;

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
	public OptimizationDTO createAndInitializeOptimization(final OptimizationDTO optimizationDto) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
		logger.info("Starting process to initialize optimization session.");

		UUID optId = optimizationDto.getOptId();

		// remove existing optimization if same optimization id is used twice
		if(this.optimizations.containsKey(optId)) {
			logger.info("Found duplicate optimization entry for optimization with id " + optId + ". Deleting old entry.");
			optimizationDTOs.remove(optId);
			optimizations.remove(optId);
			optimizationResultDTOs.remove(optId);
		}

		// keep the DTO for later
		optimizationDTOs.put(optId, optimizationDto);

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

			logger.info("Create a new optimization with the specified characteristics");
			Optimization newOptimization = factory.createOptimization(flights, slots, optimizationDto.getParameters());

			logger.info("Store the optimization for later invocation");
			optimizations.put(optId, newOptimization);
		} catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			logger.error("Could not instantiate optimization factory.", e);
			throw e;
		}

		logger.debug("List of current optimization sessions: ");
		for(Optimization o: optimizations.values()) {
			logger.info(o.getOptId());

			if(optimizationResultDTOs.containsKey(optId)) {
				logger.debug("Results for optimization with id " + optId + " are available.");
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
		// search for optId
		Optimization optimization = this.optimizations.get(optId);
		OptimizationDTO optimizationDto = this.optimizationDTOs.get(optId);

		Map<Flight,Slot> resultMap;

		OptimizationResultDTO optimizationResultDto = null;

		if(optimization != null) {
			logger.info("Starting optimization " + optId + " and running optimization algorithm.");
			resultMap = optimization.run();

			logger.info("Optimization " + optId + " has finished.");

			// convert the result map into the required format
			optimizationResultDto = OptimizationResultDTO.fromResultMap(optId, resultMap);

			// if the margins were in the original optimization DTO then include the margins in the result
			MarginsDTO[] margins = optimizationDto.getMargins();
			if(margins != null) {
				optimizationResultDto.setMargins(margins);
			}

			// get the fitness from the statistics and include it in the results
			optimizationResultDto.setFitness(optimization.getStatistics().getSolutionFitness());

			// register the optimization result
			optimizationResultDTOs.put(optId, optimizationResultDto);
		} else {
			logger.info("Optimization " + optId + " not found.");
		}

		return CompletableFuture.completedFuture(optimizationResultDto);
	}

	/**
	 *
	 * @param optId
	 * @return
	 */
	public OptimizationResultDTO getOptimizationResult(UUID optId) {
		// TODO retrieve intermediate result if the optimization has not finished
		OptimizationResultDTO optimizationResultDto = optimizationResultDTOs.get(optId);

		if(optimizationResultDto == null) {
			logger.info("No result found for " + optId);
		}

		return optimizationResultDto;
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



//	public OptimizationResultDTO getOptimizationResult(UUID optId, boolean includeMargins) {
//		if (optimizationResults != null) {
//
//			for (OptimizationResultWithMarginsDTO optRes: optimizationResults) {
//				if (optId.equals(optRes.getOptId())) {
//					logger.info("Returning results for this UUID.");
//					if(logger.isInfoEnabled()) {
//						//calculate and print how good the result is
//						printMarginResultComparison(optRes);
//					}
//					OptimizationResultWithMarginsDTO result = new OptimizationResultWithMarginsDTO();
//					result.setFlightSequence(optRes.getFlightSequence());
//					result.setOptId(optRes.getOptId());
//					result.setMargins(optRes.getMargins());
//					result.setSlots(optRes.getSlots());
//
//					if (!includeMargins) {
//						result.setMargins(null);
//					} else {
//						if (result.getMargins() == null) {
//							logger.info("No margins have been set.");
//						}
//					}
//
//					result.setSumOfWeights(getTotalWeights(optRes));
//					result.setFitnessFunctionApplications(optRes.getFitnessFunctionApplications());
//					return result;
//				}
//			}
//		}
//		logger.info("No results to return for this UUID.");
//		return null;
//	}
//
//	public OptimizationResultDTO getOptimizationResult(UUID optId) {
//		if (optimizationResults != null) {
//
//			for (OptimizationResultWithMarginsDTO optRes: optimizationResults) {
//				if (optId.equals(optRes.getOptId())) {
//					logger.info("Returning results for this UUID.");
//					if(logger.isInfoEnabled()) {
//						//calculate and print how good the result is
//						printMarginResultComparison(optRes);
//					}
//
//					return new OptimizationResultDTO(optRes.getOptId(),optRes.getFlightSequence());
//				}
//			}
//		}
//		logger.info("No results to return for this UUID.");
//		return null;
//	}
//
//	/**
//	 * Converts OptimizationDTO to Optimization.
//	 *
//	 * @param optdto OptimizationDTO
//	 * @return Optimization
//	 */
//	private static Optimization toOptimization(OptimizationDTO optdto) {
//		List<Flight> flightList = new LinkedList<>();
//		for (FlightDTO flightdto: optdto.getFlights()) {
//			Flight f = new Flight(flightdto.getFlightId(),flightdto.getScheduledTime(),flightdto.getWeightMap());
//			flightList.add(f);
//		}
//		List<Slot> slotList = new LinkedList<>();
//		for (int i = 0; i < optdto.getSlots().length; i++) {
//			Slot s = new Slot(optdto.getSlots()[i].getTime());
//			slotList.add(s);
//		}
//
//		// jenConfig
//		JeneticsConfig0 jenConfig = null;
//		if (optdto.getJenConfig() != null) {
//			jenConfig = new JeneticsConfig0(
//					optdto.getJenConfig().getAlterer(),
//					optdto.getJenConfig().getAltererAttributes(),
//					optdto.getJenConfig().getOffspringSelector(),
//					optdto.getJenConfig().getOffspringSelectorAttributes(),
//					optdto.getJenConfig().getSurvivorSelector(),
//					optdto.getJenConfig().getSurvivorSelectorAttributes(),
//					optdto.getJenConfig().getOffspringFraction(),
//					optdto.getJenConfig().getMaximalPhenotypeAge(),
//					optdto.getJenConfig().getPopulationSize(),
//					optdto.getJenConfig().getTermination(),
//					optdto.getJenConfig().getTerminationAttributes());
//		} else {
//			jenConfig = new JeneticsConfig0();
//		}
//		// optaPlannerConfig (see getOptaPlannerConfig method)
//		OptaPlannerConfig optaPlannerConfig = getOptaPlannerConfig(optdto.getOptaPlannerConfig());
//
//		// margins (is not null only when optdto.getMargins() contains data)
//		Margins[] margins = null;
//		if (optdto.getMargins() != null) {
//			logger.info("Margins have been detected and will be converted as well to Optimization (from OptimizationDTO)");
//			margins = new Margins[optdto.getMargins().length];
//			for (int i = 0; i < optdto.getMargins().length; i++) {
//				margins[i] = new Margins(
//						optdto.getMargins()[i].getFlightId(),
//						optdto.getMargins()[i].getScheduledTime(),
//						optdto.getMargins()[i].getTimeNotBefore(),
//						optdto.getMargins()[i].getTimeWished(),
//						optdto.getMargins()[i].getTimeNotAfter());
//			}
//		}
//
//		// store object of chosen framework run-class, default is JeneticsRun
//		if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework().equals(OptimizationFramework.JENETICS)) {
//			JeneticsRun classRun = new JeneticsRun();
//			logger.info("Jenetics Framework is chosen.");
//			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
//		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.OPTAPLANNER) {
//			OptaPlannerRun classRun = new OptaPlannerRun();
//			logger.info("OptaPlanner Framework is chosen.");
//			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
//		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.BENCHMARK) {
//			BenchmarkRun classRun = new BenchmarkRun();
//			logger.info("Benchmark Framework is chosen (OptaPlanner).");
//			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
//		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.BENCHMARKOPTAPLANNER) {
//			BenchmarkOptaPlannerRun classRun = new BenchmarkOptaPlannerRun();
//			logger.info("Benchmark functionality of OptaPlanner is chosen.");
//			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
//		} else if (optdto.getOptimizationFramework() == null){
//			logger.info("Framework is not set for given UUID, therefore default Jenetics Framework is used.");
//			JeneticsRun classRun = new JeneticsRun();
//			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
//		} else {
//			logger.info("No recognizable framework is chosen, therefore default Jenetics Framework is used.");
//			JeneticsRun classRun = new JeneticsRun();
//			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
//		}
//	}
//
//	/**
//	 * Return Optimization, if existing, otherwise return null.
//	 * @param optId
//	 * @return Optimization (if existing, otherwise null)
//	 */
//	private Optimization getOptimizationById(UUID optId) {
//		return optimizations.get(optId);
//	}
//
//	private int getTotalWeights(OptimizationResultWithMarginsDTO optRes) {
//		Optimization opt = getOptimizationById(optRes.getOptId());
//		int totalWeights = 0;
//		for (int i = 0; i < optRes.getFlightSequence().length; i++) {
//			Flight currentFlight = null;
//			for (Flight f: opt.getFlights()) {
//				if (f.getFlightId().equals(optRes.getFlightSequence()[i])) {
//					currentFlight = f;
//				}
//			}
//			if (currentFlight != null) {
//				totalWeights = totalWeights + currentFlight.getWeightMap()[i];
//			}
//		}
//		return totalWeights;
//	}
//	private void printMarginResultComparison(OptimizationResultWithMarginsDTO optRes) {
//		Optimization opt = getOptimizationById(optRes.getOptId());
//		int totalWeights = 0;
//		for (int i = 0; i < optRes.getFlightSequence().length; i++) {
//			Flight currentFlight = null;
//			for (Flight f: opt.getFlights()) {
//				if (f.getFlightId().equals(optRes.getFlightSequence()[i])) {
//					currentFlight = f;
//				}
//			}
//			if (currentFlight != null) {
//				logger.debug("Weight at assigned slot for flight " + currentFlight.getFlightId() + ": "
//						+ currentFlight.getWeightMap()[i]);
//				totalWeights = totalWeights + currentFlight.getWeightMap()[i];
//			} else {
//				logger.info("No slot assigned for flight!");
//			}
//		}
//		logger.info("Total weights for complete flight sequence: " + totalWeights);
//	}
//
//	public void abortOptimization(UUID optId) {
//		// TODO improve method
//		/*for (SolverJob<FlightPrioritization, UUID> solverJob: currentSolvers) {
//			logger.info("Optimization session " + optId + " will be aborted or has already finished.");
//			if (solverJob.getProblemId().equals(optId)) {
//				solverJob.terminateEarly();
//				logger.info("Optimization session " + optId + " has been aborted or has already finished.");
//				return;
//			}
//		}
//		logger.info("No optimization session with the optId " + optId + " has been found or the session cannot be aborted.");
//		return;*/
//		return;
//	}
//
//	/**
//	 * Converts OptaPlannerConfigDTO to OptaPlannerConfig
//	 * @param optaPlannerConfigDTO
//	 * @return OptaPlannerConfig
//	 */
//	private static OptaPlannerConfig getOptaPlannerConfig(OptaPlannerConfigDTO optaPlannerConfigDTO) {
//		if (optaPlannerConfigDTO == null) {
//			return new OptaPlannerConfig();
//		}
//
//		OptaPlannerConfig optaPlannerConfig = null;
//
//		TerminationOptaPlanner terminationOptaPlanner = getTerminationOfConfig(optaPlannerConfigDTO.getTermination(), " for all phases ");
//
//		ConstructionHeuristicPhase constructionHeuristic = null;
//		ConstructionHeuristicPhaseDTO constructionHeuristicDTO = optaPlannerConfigDTO.getConstructionHeuristic();
//
//		LocalSearchPhase localSearch = null;
//		LocalSearchPhaseDTO localSearchDTO = optaPlannerConfigDTO.getLocalSearch();
//
//		// construction heuristic
//		if (constructionHeuristicDTO != null) {
//			TerminationOptaPlanner constructionTermination = getTerminationOfConfig(optaPlannerConfigDTO.getConstructionHeuristic().getTermination(),
//					" for construction heuristic phase ");;
//			ConstructionEnum constructionHeuristicType = constructionHeuristicDTO.getConstructionEnum();
//			constructionHeuristic = new ConstructionHeuristicPhase(
//					constructionHeuristicType,
//					constructionTermination);
//			logger.debug("Construction Heuristic phase of type " + constructionHeuristicType + " is defined.");
//		} else {
//			logger.info("No construction heuristic phase is defined.");
//		}
//
//		// local search
//		if (localSearchDTO != null) {
//			TerminationOptaPlanner localSearchTermination = getTerminationOfConfig(optaPlannerConfigDTO.getLocalSearch().getTermination(),
//					" for local search phase ");
//			// acceptor
//			Acceptor acceptor = getAcceptor(optaPlannerConfigDTO.getLocalSearch().getAcceptor());
//
//			// forager
//			Forager forager = getForager(optaPlannerConfigDTO.getLocalSearch().getForager());
//
//			// local search type
//			LocalSearchEnum localSearchType = optaPlannerConfigDTO.getLocalSearch().getLocalSearchEnum();
//			// unionMoveSelector
//			SelectionOrderEnum selectionOrder = optaPlannerConfigDTO.getLocalSearch().getSelectionOrder();
//
//			logger.debug("Local Search phase has type " + localSearchType
//					+ " and selection order of " + selectionOrder + ".");
//
//			localSearch = new LocalSearchPhase(
//					localSearchType,
//					acceptor,
//					forager,
//					selectionOrder,
//					localSearchTermination);
//		} else {
//			logger.info("No local search phase is defined.");
//		}
//
//		// optaPlannerConfig
//		logger.debug("Move Thread Count is: " + optaPlannerConfigDTO.getMoveThreadCount() + " and EnvironmentMode is: "
//				+ optaPlannerConfigDTO.getEnvironmentMode() + ".");
//		optaPlannerConfig = new OptaPlannerConfig(
//				optaPlannerConfigDTO.getMoveThreadCount(),
//				optaPlannerConfigDTO.getEnvironmentMode(),
//				terminationOptaPlanner,
//				constructionHeuristic,
//				localSearch
//				);
//
//		return optaPlannerConfig;
//	}
//
//	/**
//	 * Prints termination type and values to logger.
//	 * @param term TerminationOptaPlanner
//	 * @param part String textual value for logger
//	 */
//	private static void printTerminationToLogger(TerminationOptaPlanner term, String part) {
//		logger.debug("Given termination" + part + "are (only the relevant value to the termination type are considered): \n"
//				+ term.getTermination1() + ": value = " + term.getTerminationValue1() + " score = "
//				+ term.getTerminationScore1() + " boolean = " + term.isTerminationBoolean1() + " "
//				+ term.getTermComp() + " \n" + term.getTermination2() + ": value = "
//				+ term.getTerminationValue2() + " score = " + term.getTerminationScore2()
//				+ " boolean = " + term.isTerminationBoolean2());
//	}
//	/**
//	 * Converts TerminationOptaPlannerDTO to TerminationOptaPlanner. Uses String textual value for logger output.
//	 * @param termDTO TerminationOptaPlannerDTO
//	 * @param part String textual value for logger.
//	 * @return TerminationOptaPlanner based on termDTO (or null, if termDTO is null)
//	 */
//	private static TerminationOptaPlanner getTerminationOfConfig(TerminationOptaPlannerDTO termDTO, String part) {
//		if (termDTO != null) {
//			TerminationOptaPlanner term = new TerminationOptaPlanner(
//					termDTO.getTermination1(),
//					termDTO.getTermination2(),
//					termDTO.getTermComp(),
//					termDTO.getTerminationValue1(),
//					termDTO.getTerminationValue2(),
//					termDTO.getTerminationScore1(),
//					termDTO.getTerminationScore2(),
//					termDTO.isTerminationBoolean1(),
//					termDTO.isTerminationBoolean2()
//					);
//			printTerminationToLogger(term, part);
//			return term;
//		} else {
//			logger.info("No given termination " + part);
//			return null;
//		}
//	}
//
//	/**
//	 * Converts AcceptorDTO to Acceptor.
//	 * @param acceptorDTO AcceptorDTO
//	 * @return Acceptor based on acceptorDTO (or null, if acceptorDTO is null)
//	 */
//	private static Acceptor getAcceptor(AcceptorDTO acceptorDTO) {
//		if (acceptorDTO != null) {
//			Acceptor acceptor = new Acceptor(
//					acceptorDTO.getAcceptorType(),
//					acceptorDTO.getEntityTabuSize(),
//					acceptorDTO.getEntityTabuRatio(),
//					acceptorDTO.getValueTabuSize(),
//					acceptorDTO.getValueTabuRatio(),
//					acceptorDTO.getMoveTabuSize(),
//					acceptorDTO.getUndoMoveTabuSize(),
//					acceptorDTO.getSimulAnnealStartTemp(),
//					acceptorDTO.getLateAcceptanceSize(),
//					acceptorDTO.getGrDelInitWaterLevel(),
//					acceptorDTO.getGrDelWaterLevelIncrRatio(),
//					acceptorDTO.getGrDelWaterLevelIncrScore(),
//					acceptorDTO.getStepCountHillClimbSize()
//					);
//			logger.debug("Acceptor for Local Search phase is: \n"
//					+ "type: " + acceptor.getAcceptorType() + "\n"
//					+ "entity tabu size: " + acceptorDTO.getEntityTabuSize()
//					+ " | entity tabu ratio: " + acceptorDTO.getEntityTabuRatio()
//					+ " | value tabu size: " + acceptorDTO.getValueTabuSize()
//					+ " | value tabu ratio: " + acceptorDTO.getValueTabuRatio()
//					+ " | move tabu size: " + acceptorDTO.getMoveTabuSize()
//					+ " | undo move tabu size: " + acceptorDTO.getUndoMoveTabuSize()
//					+ "\n simulated annealing starting temperature: "
//					+ acceptor.getSimulAnnealStartTemp()
//					+ " | late acceptance size: " + acceptor.getLateAcceptanceSize()
//					+ "\n great deluge water level => initial: "
//					+ acceptor.getGrDelInitWaterLevel() + " | increment ratio: "
//					+ acceptor.getGrDelWaterLevelIncrRatio() + " | increment score: "
//					+ acceptor.getGrDelWaterLevelIncrScore()
//					+ "\n step counting hill climbing size: "
//					+ acceptor.getStepCountHillClimbSize()
//					+ "\n" + "Only relevant values will be used!");
//			return acceptor;
//		} else {
//			logger.info("No Acceptor was defined.");
//			return null;
//		}
//	}
//
//	/**
//	 * Converts ForagerDTO to Forager.
//	 * @param foragerDTO ForagerDTO
//	 * @return Forager based on foragerDTO (or null, if foragerDTO is null)
//	 */
//	private static Forager getForager(ForagerDTO foragerDTO) {
//		if (foragerDTO != null) {
//			Forager forager = new Forager(
//					foragerDTO.getAcceptedCountLimit(),
//					foragerDTO.getFinalistPodiumType(),
//					foragerDTO.getPickEarlyType());
//			logger.debug("Forager for Local Search phase is: \n"
//					+ "accepted count limit: " + forager.getAcceptedCountLimit()
//					+ " | finalist podium type: " + forager.getFinalistPodiumType()
//					+ " | pick early type: " + forager.getPickEarlyType()
//					+ "\n" + "Only relevant values will be used!");
//			return forager;
//		} else {
//			logger.info("No Forager was defined.");
//			return null;
//		}
//	}
//
//	/**
//	 * Integrates Margins to OptimizationResultDTO
//	 *
//	 * @param optResult current optimization result
//	 * @param margins current margins
//	 * @return OptimizationResultDTO with margins
//	 */
//	private OptimizationResultWithMarginsDTO setMargins(OptimizationResultWithMarginsDTO optResult, Margins[] margins) {
//		if (margins != null) {
//			MarginsDTO[] marginsDTO = new MarginsDTO[margins.length];
//			for (int i = 0; i < margins.length; i++) {
//				marginsDTO[i] = new MarginsDTO(
//						margins[i].getFlightId(),
//						margins[i].getScheduledTime(),
//						margins[i].getTimeNotBefore(),
//						margins[i].getTimeWished(),
//						margins[i].getTimeNotAfter());
//			}
//			optResult.setMargins(marginsDTO);
//			return optResult;
//		}
//		optResult.setMargins(null);
//		return optResult;
//	}
//
//	/**
//	 * Integrates Slots to OptimizationResultDTO as a list of Instants
//	 *
//	 * @param optResult current optimization result
//	 * @param slots current slots
//	 * @return OptimizationResultDTO with slots
//	 */
//	private OptimizationResultWithMarginsDTO setSlots(OptimizationResultWithMarginsDTO optResult, List<Slot> slots) {
//		if (slots != null) {
//			Instant[] slotList = new Instant[slots.size()];
//			for (int i = 0; i < slots.size(); i++) {
//				slotList[i] = slots.get(i).getTime();
//			}
//			optResult.setSlots(slotList);
//			return optResult;
//		}
//		optResult.setSlots(null);
//		return optResult;
//	}
}
