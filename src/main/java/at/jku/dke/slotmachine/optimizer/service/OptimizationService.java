package at.jku.dke.slotmachine.optimizer.service;

import java.time.Instant;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.optimizer.domain.*;
import at.jku.dke.slotmachine.optimizer.frameworks.benchmark.BenchmarkRun;
import at.jku.dke.slotmachine.optimizer.frameworks.benchmarkOptaPlanner.BenchmarkOptaPlannerRun;
import at.jku.dke.slotmachine.optimizer.frameworks.jenetics.JeneticsRun;
import at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.OptaPlannerRun;
import at.jku.dke.slotmachine.optimizer.service.dto.*;
import at.jku.dke.slotmachine.optimizer.service.dto.ConstructionHeuristicPhaseDTO.ConstructionEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.LocalSearchPhaseDTO.LocalSearchEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.LocalSearchPhaseDTO.SelectionOrderEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO.OptimizationFramework;

public class OptimizationService {

	private List<OptimizationDTO> optimizationDTOs;
	private List<OptimizationResultMarginsDTO> optimizationResults;
	private List<Optimization> optimizations;
	
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Creates the optimization session and initializes it with the given data.
	 * @param optdto data for the optimization session
	 * @return given optimization session data
	 */
	public OptimizationDTO createAndInitialize(OptimizationDTO optdto) {
		logger.info("Started process to initialize optimization session.");
		if(optimizationDTOs == null) optimizationDTOs = new LinkedList<OptimizationDTO>();
		if(optimizations == null) optimizations = new LinkedList<Optimization>();
		// overwrite old optimization if same optimization id is used twice
		for (Optimization opt: optimizations) {
			if (opt.getOptId().equals(optdto.getOptId())) {
				logger.info("Found duplicate optimization entry according to UUID, delete old entry.");
				optimizations.remove(opt);
				optimizationDTOs.add(optdto);
				Optimization optNew = toOptimization(optdto);
				optimizations.add(optNew);
				OptimizationResultMarginsDTO optResToBeDeleted = null;
				if (optimizationResults != null) {
					for (OptimizationResultMarginsDTO optRes: optimizationResults) {
						if (optNew.getOptId().equals(optRes.getOptId())) {
							logger.info("Found old result entry according to UUID, delete old entry.");
							optResToBeDeleted = optRes;
							//optimizationResults.remove(optRes);
						}
					}
				}
				if(optResToBeDeleted != null) {
					optimizationResults.remove(optResToBeDeleted);
				}
				logger.info("Current list of optimization sessions: ");
				for(Optimization o: optimizations) {
					boolean availableResult = false;
					if(optimizationResults != null) {
						for(OptimizationResultMarginsDTO optRes: optimizationResults) {
							if (optRes.getOptId().equals(optdto.getOptId())) {
								availableResult = true;
							}
						}
					}
					logger.info("optId: " + o.getOptId());
					logger.debug("Is the result available? " + availableResult);
				}
				return optdto;
			}
		}
		optimizationDTOs.add(optdto);
		Optimization opt = toOptimization(optdto);
		optimizations.add(opt);	
		logger.info("Current list of optimization sessions: ");
		for(Optimization o: optimizations) {
			boolean availableResult = false;
			if(optimizationResults != null) {
				for(OptimizationResultMarginsDTO optRes: optimizationResults) {
					if (optRes.getOptId().equals(optdto.getOptId())) {
						availableResult = true;
					}
				}
			}
			logger.info("optId: " + o.getOptId());
			logger.debug("Is the result available? " + availableResult);
		}
		return optdto;
	}
	
	/**
	 * Starts the optimization sessions, finishes after the optimization has finished
	 * @param optId optId of the optimization session
	 */
	public void startOptimization(UUID optId) {
		logger.info("Starting optimization and running optimization algorithm.");
		if(optimizationResults == null) optimizationResults = new LinkedList<OptimizationResultMarginsDTO>();
		// search for optId
		Optimization curOpt = getOptimizationById(optId);
		
		// run the chosen framework according to the object stored in .getOptimization()
		Map<Flight,Slot> resultMap = null;
		if (curOpt.getOptimization().getClass().equals(JeneticsRun.class)) {
			logger.info("Optimization uses Jenetics framework.");
			
			// use jenetics configuration, if jenConfig is not null
			if (curOpt.getJenConfig()!=null) {
				resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList(), curOpt.getJenConfig(), curOpt);
			} else {
				resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList(), curOpt);
			}
		} else if (curOpt.getOptimization().getClass().equals(OptaPlannerRun.class)) {
			logger.info("Optimization uses OptaPlannerRun framework.");
			if (curOpt.getOptaPlannerConfig() != null) {
				resultMap = OptaPlannerRun.run(curOpt.getFlightList(), curOpt.getSlotList(), curOpt.getOptaPlannerConfig(), curOpt);
			} else {
				resultMap = OptaPlannerRun.run(curOpt.getFlightList(), curOpt.getSlotList(), curOpt);
			}
		} else if (curOpt.getOptimization().getClass().equals(BenchmarkRun.class)) {
			logger.info("Optimization uses BenchmarkRun framework (OptaPlanner).");
			resultMap = BenchmarkRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		} else if (curOpt.getOptimization().getClass().equals(BenchmarkOptaPlannerRun.class)) {
			logger.info("Optimization uses Benchmark functionality of OptaPlanner.");
			// TODO due to the nature of the Benchmark functionality of OptaPlanner, expect errors currently
			resultMap = BenchmarkOptaPlannerRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		} else {
			logger.info("No framework set, uses default Jenetics framework.");
			resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList(), curOpt);
		}
		
		logger.info("Preparing results.");
		String[] assignedSequence = new String[curOpt.getSlotList().size()]; //due to perhaps different number of 
																 //flights and slots
		
		// get sorted list of slots (by time)
		List<Instant> sortedSlots = new LinkedList<Instant>();
		for (Slot s: curOpt.getSlotList()) {
			sortedSlots.add(s.getTime());
		}
		Collections.sort(sortedSlots);
		
		// use sorted list to get an array of assigned flights for the given slots
		for(Map.Entry<Flight, Slot> entry: resultMap.entrySet()) {
			Flight flight = entry.getKey();
			Slot slot = entry.getValue();
			if (slot != null) {
				// to prevent errors, if slot is not available for the current flight
				int posOfSlot = sortedSlots.indexOf(slot.getTime());
				assignedSequence[posOfSlot] = flight.getFlightId();
			}
		}
		
		OptimizationResultMarginsDTO optResult = new OptimizationResultMarginsDTO();
		optResult.setOptId(optId);
		optResult.setFlightSequence(assignedSequence);
		optResult = setMargins(optResult, curOpt.getMargins());
		optResult = setSlots(optResult, curOpt.getSlotList());
		optResult.setFitnessFunctionApplications(curOpt.getFitnessApplications());
		logger.info("Storing results.");
		//if optId already has a result remove the old result
		OptimizationResultMarginsDTO oldOptResult = null;
		for (OptimizationResultMarginsDTO optResDTO: optimizationResults) {
			if (optResDTO.getOptId().equals(optResult.getOptId())) {
				oldOptResult = optResDTO;
			}
		}
		if(oldOptResult != null) {
			logger.info("Old result for this optId found and replaced by new result.");
			optimizationResults.remove(oldOptResult);
		}
		optimizationResults.add(optResult);
	}

	public OptimizationResultMarginsDTO getOptimizationResult(UUID optId, boolean margins) {
		if (optimizationResults != null) {
			
			for (OptimizationResultMarginsDTO optRes: optimizationResults) {
				if (optId.equals(optRes.getOptId())) {
					logger.info("Returning results for this UUID.");
					if(logger.isInfoEnabled()) {
						//calculate and print how good the result is
						printMarginResultComparison(optRes);
					}
					OptimizationResultMarginsDTO result = new OptimizationResultMarginsDTO();
					result.setFlightSequence(optRes.getFlightSequence());
					result.setOptId(optRes.getOptId());
					result.setMargins(optRes.getMargins());
					result.setSlots(optRes.getSlots());
					if (margins == false) {
						result.setMargins(null);
					} else {
						if (result.getMargins() == null) {
							logger.info("No margins have been set.");
						}
					}
					result.setSumOfWeights(getTotalWeights(optRes));
					result.setFitnessFunctionApplications(optRes.getFitnessFunctionApplications());
					return result;
				}
			}
		}
		logger.info("No results to return for this UUID.");
		return null;
	}
	
	public OptimizationResultDTO getOptimizationResult(UUID optId) {
		if (optimizationResults != null) {
			
			for (OptimizationResultMarginsDTO optRes: optimizationResults) {
				if (optId.equals(optRes.getOptId())) {
					logger.info("Returning results for this UUID.");
					if(logger.isInfoEnabled()) {
						//calculate and print how good the result is
						printMarginResultComparison(optRes);
					}
					
					return new OptimizationResultDTO(optRes.getOptId(),optRes.getFlightSequence());
				}
			}
		}
		logger.info("No results to return for this UUID.");
		return null;
	}

	/**
	 * Converts OptimizationDTO to Optimization.
	 * 
	 * @param optdto OptimizationDTO
	 * @return Optimization
	 */
	private static Optimization toOptimization(OptimizationDTO optdto) {
		List<Flight> flightList = new LinkedList<Flight>();
		for (FlightDTO flightdto: optdto.getFlights()) {
			Flight f = new Flight(flightdto.getFlightId(),flightdto.getScheduledTime(),flightdto.getWeightMap());
			flightList.add(f);
		}
		List<Slot> slotList = new LinkedList<Slot>();
		for (int i = 0; i < optdto.getSlots().length; i++) {
			Slot s = new Slot(optdto.getSlots()[i].getTime());
			slotList.add(s);
		}

		// jenConfig
		JeneticsConfig jenConfig = null;
		if (optdto.getJenConfig() != null) {
			jenConfig = new JeneticsConfig(
					optdto.getJenConfig().getAlterer(),
					optdto.getJenConfig().getAltererAttributes(),
					optdto.getJenConfig().getOffspringSelector(),
					optdto.getJenConfig().getOffspringSelectorAttributes(),
					optdto.getJenConfig().getSurvivorSelector(),
					optdto.getJenConfig().getSurvivorSelectorAttributes(),
					optdto.getJenConfig().getOffspringFraction(),
					optdto.getJenConfig().getMaximalPhenotypeAge(),
					optdto.getJenConfig().getPopulationSize(),
					optdto.getJenConfig().getTermination(),
					optdto.getJenConfig().getTerminationAttributes());
		} else {
			jenConfig = new JeneticsConfig();
		}
		// optaPlannerConfig (see getOptaPlannerConfig method)
		OptaPlannerConfig optaPlannerConfig = getOptaPlannerConfig(optdto.getOptaPlannerConfig());		
		
		// margins (is not null only when optdto.getMargins() contains data)
		Margin[] margins = null;
		if (optdto.getMargins() != null) {
			logger.info("Margins have been detected and will be converted as well to Optimization (from OptimizationDTO)");
			margins = new Margin[optdto.getMargins().length];
			for (int i = 0; i < optdto.getMargins().length; i++) {
				margins[i] = new Margin(
						optdto.getMargins()[i].getFlightId(),
						optdto.getMargins()[i].getScheduledTime(),
						optdto.getMargins()[i].getTimeNotBefore(),
						optdto.getMargins()[i].getTimeWished(),
						optdto.getMargins()[i].getTimeNotAfter());
			}
		} 

		// store object of chosen framework run-class, default is JeneticsRun
		if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework().equals(OptimizationFramework.JENETICS)) {
			JeneticsRun classRun = new JeneticsRun();
			logger.info("Jenetics Framework is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.OPTAPLANNER) {
			OptaPlannerRun classRun = new OptaPlannerRun();
			logger.info("OptaPlanner Framework is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.BENCHMARK) {
			BenchmarkRun classRun = new BenchmarkRun();
			logger.info("Benchmark Framework is chosen (OptaPlanner).");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.BENCHMARKOPTAPLANNER) {
			BenchmarkOptaPlannerRun classRun = new BenchmarkOptaPlannerRun();
			logger.info("Benchmark functionality of OptaPlanner is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
		} else if (optdto.getOptimizationFramework() == null){
			logger.info("Framework is not set for given UUID, therefore default Jenetics Framework is used.");
			JeneticsRun classRun = new JeneticsRun();
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
		} else {
			logger.info("No recognizable framework is chosen, therefore default Jenetics Framework is used.");
			JeneticsRun classRun = new JeneticsRun();
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig, optaPlannerConfig, margins);
		}	
	}

	/**
	 * Return Optimization, if existing, otherwise return null.
	 * @param optId
	 * @return Optimization (if existing, otherwise null)
	 */
	private Optimization getOptimizationById(UUID optId) {
		for (Optimization opt: optimizations) {
			if (opt.getOptId().equals(optId)) {
				return opt;
			}
		}
		return null;
	}
	private int getTotalWeights(OptimizationResultMarginsDTO optRes) {
		Optimization opt = getOptimizationById(optRes.getOptId());
		int totalWeights = 0;
		for (int i = 0; i < optRes.getFlightSequence().length; i++) {
			Flight currentFlight = null;
			for (Flight f: opt.getFlightList()) {
				if (f.getFlightId().equals(optRes.getFlightSequence()[i])) {
					currentFlight = f;
				}
			}
			if (currentFlight != null) {
				totalWeights = totalWeights + currentFlight.getWeightMap()[i];
			}
		}
		return totalWeights;
	}
	private void printMarginResultComparison(OptimizationResultMarginsDTO optRes) {
		Optimization opt = getOptimizationById(optRes.getOptId());
		int totalWeights = 0;
		for (int i = 0; i < optRes.getFlightSequence().length; i++) {
			Flight currentFlight = null;
			for (Flight f: opt.getFlightList()) {
				if (f.getFlightId().equals(optRes.getFlightSequence()[i])) {
					currentFlight = f;
				}
			}
			if (currentFlight != null) {
				logger.debug("Weight at assigned slot for flight " + currentFlight.getFlightId() + ": " 
						+ currentFlight.getWeightMap()[i]);
				totalWeights = totalWeights + currentFlight.getWeightMap()[i];
			} else {
				logger.info("No slot assigned for flight!");
			}
		}
		logger.info("Total weights for complete flight sequence: " + totalWeights);
	}

	public void abortOptimization(UUID optId) {
		// TODO improve method
		/*for (SolverJob<FlightPrioritization, UUID> solverJob: currentSolvers) {
			logger.info("Optimization session " + optId + " will be aborted or has already finished.");
			if (solverJob.getProblemId().equals(optId)) {
				solverJob.terminateEarly();
				logger.info("Optimization session " + optId + " has been aborted or has already finished.");
				return;
			}
		}
		logger.info("No optimization session with the optId " + optId + " has been found or the session cannot be aborted.");
		return;*/
		return;
	}
	
	/**
	 * Converts OptaPlannerConfigDTO to OptaPlannerConfig
	 * @param optaPlannerConfigDTO
	 * @return OptaPlannerConfig
	 */
	private static OptaPlannerConfig getOptaPlannerConfig(OptaPlannerConfigDTO optaPlannerConfigDTO) {
		if (optaPlannerConfigDTO == null) {
			return new OptaPlannerConfig();
		}
		
		OptaPlannerConfig optaPlannerConfig = null;
		
		TerminationOptaPlanner terminationOptaPlanner = getTerminationOfConfig(optaPlannerConfigDTO.getTermination(), " for all phases ");
		
		ConstructionHeuristicPhase constructionHeuristic = null;
		ConstructionHeuristicPhaseDTO constructionHeuristicDTO = optaPlannerConfigDTO.getConstructionHeuristic();
		
		LocalSearchPhase localSearch = null;
		LocalSearchPhaseDTO localSearchDTO = optaPlannerConfigDTO.getLocalSearch();
		
		// construction heuristic
		if (constructionHeuristicDTO != null) {
			TerminationOptaPlanner constructionTermination = getTerminationOfConfig(optaPlannerConfigDTO.getConstructionHeuristic().getTermination(), 
					" for construction heuristic phase ");;
			ConstructionEnum constructionHeuristicType = constructionHeuristicDTO.getConstructionEnum();
			constructionHeuristic = new ConstructionHeuristicPhase(
					constructionHeuristicType,
					constructionTermination);
			logger.debug("Construction Heuristic phase of type " + constructionHeuristicType + " is defined.");
		} else {
			logger.info("No construction heuristic phase is defined.");
		}
		
		// local search
		if (localSearchDTO != null) {
			TerminationOptaPlanner localSearchTermination = getTerminationOfConfig(optaPlannerConfigDTO.getLocalSearch().getTermination(), 
					" for local search phase ");
			// acceptor
			Acceptor acceptor = getAcceptor(optaPlannerConfigDTO.getLocalSearch().getAcceptor());
			
			// forager
			Forager forager = getForager(optaPlannerConfigDTO.getLocalSearch().getForager());
			
			// local search type
			LocalSearchEnum localSearchType = optaPlannerConfigDTO.getLocalSearch().getLocalSearchEnum();
			// unionMoveSelector
			SelectionOrderEnum selectionOrder = optaPlannerConfigDTO.getLocalSearch().getSelectionOrder();
			
			logger.debug("Local Search phase has type " + localSearchType 
					+ " and selection order of " + selectionOrder + ".");
			
			localSearch = new LocalSearchPhase(
					localSearchType,
					acceptor,
					forager,
					selectionOrder,
					localSearchTermination);
		} else {
			logger.info("No local search phase is defined.");
		}
		
		// optaPlannerConfig
		logger.debug("Move Thread Count is: " + optaPlannerConfigDTO.getMoveThreadCount() + " and EnvironmentMode is: "
				+ optaPlannerConfigDTO.getEnvironmentMode() + ".");
		optaPlannerConfig = new OptaPlannerConfig(
				optaPlannerConfigDTO.getMoveThreadCount(),
				optaPlannerConfigDTO.getEnvironmentMode(),
				terminationOptaPlanner,
				constructionHeuristic,
				localSearch
				);
		
		return optaPlannerConfig;
	}

	/**
	 * Prints termination type and values to logger.
	 * @param term TerminationOptaPlanner
	 * @param part String textual value for logger
	 */
	private static void printTerminationToLogger(TerminationOptaPlanner term, String part) {
		logger.debug("Given termination" + part + "are (only the relevant value to the termination type are considered): \n"
				+ term.getTermination1() + ": value = " + term.getTerminationValue1() + " score = " 
				+ term.getTerminationScore1() + " boolean = " + term.isTerminationBoolean1() + " " 
				+ term.getTermComp() + " \n" + term.getTermination2() + ": value = " 
				+ term.getTerminationValue2() + " score = " + term.getTerminationScore2() 
				+ " boolean = " + term.isTerminationBoolean2());
	}
	/**
	 * Converts TerminationOptaPlannerDTO to TerminationOptaPlanner. Uses String textual value for logger output.
	 * @param termDTO TerminationOptaPlannerDTO
	 * @param part String textual value for logger.
	 * @return TerminationOptaPlanner based on termDTO (or null, if termDTO is null)
	 */
	private static TerminationOptaPlanner getTerminationOfConfig(TerminationOptaPlannerDTO termDTO, String part) {
		if (termDTO != null) {
			TerminationOptaPlanner term = new TerminationOptaPlanner(
					termDTO.getTermination1(),
					termDTO.getTermination2(),
					termDTO.getTermComp(),
					termDTO.getTerminationValue1(),
					termDTO.getTerminationValue2(),
					termDTO.getTerminationScore1(),
					termDTO.getTerminationScore2(),
					termDTO.isTerminationBoolean1(),
					termDTO.isTerminationBoolean2()
					);
			printTerminationToLogger(term, part);
			return term;
		} else {
			logger.info("No given termination " + part);
			return null;
		}
	}
	
	/**
	 * Converts AcceptorDTO to Acceptor.
	 * @param acceptorDTO AcceptorDTO
	 * @return Acceptor based on acceptorDTO (or null, if acceptorDTO is null)
	 */
	private static Acceptor getAcceptor(AcceptorDTO acceptorDTO) {
		if (acceptorDTO != null) {
			Acceptor acceptor = new Acceptor(
					acceptorDTO.getAcceptorType(),
					acceptorDTO.getEntityTabuSize(),
					acceptorDTO.getEntityTabuRatio(),
					acceptorDTO.getValueTabuSize(),
					acceptorDTO.getValueTabuRatio(),
					acceptorDTO.getMoveTabuSize(),
					acceptorDTO.getUndoMoveTabuSize(),
					acceptorDTO.getSimulAnnealStartTemp(),
					acceptorDTO.getLateAcceptanceSize(),
					acceptorDTO.getGrDelInitWaterLevel(),
					acceptorDTO.getGrDelWaterLevelIncrRatio(),
					acceptorDTO.getGrDelWaterLevelIncrScore(),
					acceptorDTO.getStepCountHillClimbSize()
					);
			logger.debug("Acceptor for Local Search phase is: \n" 
					+ "type: " + acceptor.getAcceptorType() + "\n"
					+ "entity tabu size: " + acceptorDTO.getEntityTabuSize()
					+ " | entity tabu ratio: " + acceptorDTO.getEntityTabuRatio()
					+ " | value tabu size: " + acceptorDTO.getValueTabuSize()
					+ " | value tabu ratio: " + acceptorDTO.getValueTabuRatio()
					+ " | move tabu size: " + acceptorDTO.getMoveTabuSize() 
					+ " | undo move tabu size: " + acceptorDTO.getUndoMoveTabuSize()
					+ "\n simulated annealing starting temperature: " 
					+ acceptor.getSimulAnnealStartTemp()
					+ " | late acceptance size: " + acceptor.getLateAcceptanceSize()
					+ "\n great deluge water level => initial: "
					+ acceptor.getGrDelInitWaterLevel() + " | increment ratio: "
					+ acceptor.getGrDelWaterLevelIncrRatio() + " | increment score: " 
					+ acceptor.getGrDelWaterLevelIncrScore()
					+ "\n step counting hill climbing size: "
					+ acceptor.getStepCountHillClimbSize()
					+ "\n" + "Only relevant values will be used!");
			return acceptor;
		} else {
			logger.info("No Acceptor was defined.");
			return null;
		}
	}
	
	/**
	 * Converts ForagerDTO to Forager.
	 * @param foragerDTO ForagerDTO
	 * @return Forager based on foragerDTO (or null, if foragerDTO is null)
	 */
	private static Forager getForager(ForagerDTO foragerDTO) {
		if (foragerDTO != null) {
			Forager forager = new Forager(
					foragerDTO.getAcceptedCountLimit(),
					foragerDTO.getFinalistPodiumType(),
					foragerDTO.getPickEarlyType());
			logger.debug("Forager for Local Search phase is: \n"
					+ "accepted count limit: " + forager.getAcceptedCountLimit()
					+ " | finalist podium type: " + forager.getFinalistPodiumType()
					+ " | pick early type: " + forager.getPickEarlyType()
					+ "\n" + "Only relevant values will be used!");
			return forager;
		} else {
			logger.info("No Forager was defined.");
			return null;
		}
	}
	
	/**
	 * Integrates Margins to OptimizationResultDTO
	 * 
	 * @param optResult current optimization result
	 * @param margins current margins
	 * @return OptimizationResultDTO with margins
	 */
	private OptimizationResultMarginsDTO setMargins(OptimizationResultMarginsDTO optResult, Margin[] margins) {
		if (margins != null) {
			MarginDTO[] marginsDTO = new MarginDTO[margins.length];
			for (int i = 0; i < margins.length; i++) {
				marginsDTO[i] = new MarginDTO(
						margins[i].getFlightId(),
						margins[i].getScheduledTime(),
						margins[i].getTimeNotBefore(),
						margins[i].getTimeWished(),
						margins[i].getTimeNotAfter());
			}
			optResult.setMargins(marginsDTO);
			return optResult;
		}
		optResult.setMargins(null);
		return optResult;
	}
	
	/**
	 * Integrates Slots to OptimizationResultDTO as a list of Instants
	 * 
	 * @param optResult current optimization result
	 * @param slots current slots
	 * @return OptimizationResultDTO with slots
	 */
	private OptimizationResultMarginsDTO setSlots(OptimizationResultMarginsDTO optResult, List<Slot> slots) {
		if (slots != null) {
			Instant[] slotList = new Instant[slots.size()];
			for (int i = 0; i < slots.size(); i++) {
				slotList[i] = slots.get(i).getTime();
			}
			optResult.setSlots(slotList);
			return optResult;
		}
		optResult.setSlots(null);
		return optResult;
	}
	
	/**
	 * Check if optId is currently initialized or not to prevent errors
	 * @param optId optId to be checked
	 * @return true, if optId is already initialized, otherwise false
	 */
	public boolean findCurOptId(UUID optId) {
		if (getOptimizationById(optId) == null) {
			return false;
		}
		return true;
	}
}
