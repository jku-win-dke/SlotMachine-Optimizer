package at.jku.dke.slotmachine.optimizer.service;

import java.time.Instant;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.optimizer.domain.*;
import at.jku.dke.slotmachine.optimizer.frameworks.benchmark.BenchmarkRun;
import at.jku.dke.slotmachine.optimizer.frameworks.jenetics.JeneticsRun;
import at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.OptaPlannerRun;
import at.jku.dke.slotmachine.optimizer.service.dto.*;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO.OptimizationFramework;

public class OptimizationService {

	private List<OptimizationDTO> optimizationDTOs;
	private List<OptimizationResultDTO> optimizationResults;
	private List<Optimization> optimizations;
	private static final Logger logger = LogManager.getLogger();
	
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
				OptimizationResultDTO optResToBeDeleted = null;
				if (optimizationResults != null) {
					for (OptimizationResultDTO optRes: optimizationResults) {
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
				logger.info("current list of optimiziation sessions: ");
				for(Optimization o: optimizations) {
					boolean availableResult = false;
					if(optimizationResults != null) {
						for(OptimizationResultDTO optRes: optimizationResults) {
							if (optRes.getOptId().equals(optdto.getOptId())) {
								availableResult = true;
							}
						}
					}
					logger.info("optId: " + o.getOptId());
					logger.debug("Result is available? " + availableResult);
				}
				return optdto;
			}
		}
		optimizationDTOs.add(optdto);
		Optimization opt = toOptimization(optdto);
		optimizations.add(opt);	
		logger.info("current list of optimiziation sessions: ");
		for(Optimization o: optimizations) {
			boolean availableResult = false;
			if(optimizationResults != null) {
				for(OptimizationResultDTO optRes: optimizationResults) {
					if (optRes.getOptId().equals(optdto.getOptId())) {
						availableResult = true;
					}
				}
			}
			logger.info("optId: " + o.getOptId());
			logger.debug("Result is available? " + availableResult);
		}
		return optdto;
	}
	
	public void startOptimization(UUID optId) {
		logger.info("Starting optimization and running optimization algorithm.");
		if(optimizationResults == null) optimizationResults = new LinkedList<OptimizationResultDTO>();
		// search for optId
		Optimization curOpt = getOptimizationById(optId);
		
		// run the chosen framework according to the object stored in .getOptimization()
		Map<Flight,Slot> resultMap = null;
		if (curOpt.getOptimization().getClass().equals(JeneticsRun.class)) {
			logger.info("Optimization uses Jenetics framework.");
			
			// use jenetics configuration, if jenConfig is not null
			if (curOpt.getJenConfig()!=null) {
				resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList(), curOpt.getJenConfig());
			} else {
				resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList());
			}
		} else if (curOpt.getOptimization().getClass().equals(OptaPlannerRun.class)) {
			logger.info("Optimization uses OptaPlannerRun framework.");
			resultMap = OptaPlannerRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		} else if (curOpt.getOptimization().getClass().equals(BenchmarkRun.class)) {
			logger.info("Optimization uses BenchmarkRun framework (OptaPlanner).");
			resultMap = BenchmarkRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		} else {
			logger.info("No framework set, uses default Jenetics framework.");
			resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList());
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
		
		OptimizationResultDTO optResult = new OptimizationResultDTO();
		optResult.setOptId(optId);
		optResult.setFlightSequence(assignedSequence);
		logger.info("Storing results.");
		//if optId already has a result remove the old result
		OptimizationResultDTO oldOptResult = null;
		for (OptimizationResultDTO optResDTO: optimizationResults) {
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
	
	public OptimizationResultDTO getOptimizationResult(UUID optId) {
		if (optimizationResults != null) {
			
			for (OptimizationResultDTO optRes: optimizationResults) {
				if (optId.equals(optRes.getOptId())) {
					logger.info("Returning results for this UUID.");
					if(logger.isInfoEnabled()) {
						//calculate and print how good the result is
						printMarginResultComparison(optRes);
					}
					return optRes;
				}
			}
		}
		logger.info("No results to return for this UUID.");
		return null;
	}

	private static Optimization toOptimization(OptimizationDTO optdto) {
		List<Flight> flightList = new LinkedList<Flight>();
		for (FlightDTO flightdto: optdto.getFlights()) {
			Flight f = new Flight(flightdto.getFlightId(),flightdto.getScheduledTime(),flightdto.getWeightMap());
			flightList.add(f);
		}
		List<Slot> slotList = new LinkedList<Slot>();
		for (SlotDTO slotdto: optdto.getSlots()) {
			Slot s = new Slot(slotdto.getTime());
			slotList.add(s);
		}
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
		// store object of chosen framework run-class, default is JeneticsRun
		if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework().equals(OptimizationFramework.JENETICS)) {
			JeneticsRun classRun = new JeneticsRun();
			logger.info("Jenetics Framework is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig);
		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.OPTAPLANNER) {
			OptaPlannerRun classRun = new OptaPlannerRun();
			logger.info("OptaPlanner Framework is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig);
		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.BENCHMARK) {
			BenchmarkRun classRun = new BenchmarkRun();
			logger.info("Benchmark Framework is chosen (OptaPlanner).");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig);
		} else if (optdto.getOptimizationFramework() == null){
			logger.info("Framework is not set for given UUID, therefore default Jenetics Framework is used.");
			JeneticsRun classRun = new JeneticsRun();
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig);
		} else {
			logger.info("No recognizable framework is chosen, therefore default Jenetics Framework is used.");
			JeneticsRun classRun = new JeneticsRun();
			return new Optimization(flightList, slotList, classRun, optdto.getOptId(), jenConfig);
		}	
	}
	private Optimization getOptimizationById(UUID optId) {
		for (Optimization opt: optimizations) {
			if (opt.getOptId().equals(optId)) {
				return opt;
			}
		}
		return null;
	}
	private void printMarginResultComparison(OptimizationResultDTO optRes) {
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
}
