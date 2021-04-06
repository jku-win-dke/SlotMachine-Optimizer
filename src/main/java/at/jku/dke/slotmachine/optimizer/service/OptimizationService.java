package at.jku.dke.slotmachine.optimizer.service;

import java.time.Instant;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.optimizer.domain.*;
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
				if (optimizationResults != null) {
					for (OptimizationResultDTO optRes: optimizationResults) {
						if (optNew.getOptId().equals(optRes.getOptId())) {
							logger.info("Found old result entry according to UUID, delete old entry.");
							optimizationResults.remove(optRes);
						}
					}
				}
				return optdto;
			}
		}
		optimizationDTOs.add(optdto);
		Optimization opt = toOptimization(optdto);
		optimizations.add(opt);	
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
			resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		} else if (curOpt.getOptimization().getClass().equals(OptaPlannerRun.class)) {
			logger.info("Optimization uses OptaPlannerRun framework.");
			resultMap = OptaPlannerRun.run(curOpt.getFlightList(), curOpt.getSlotList());
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
			int posOfSlot = sortedSlots.indexOf(slot.getTime());
			assignedSequence[posOfSlot] = flight.getFlightId();
		}
		
		OptimizationResultDTO optResult = new OptimizationResultDTO();
		optResult.setOptId(optId);
		optResult.setFlightSequence(assignedSequence);
		logger.info("Storing results.");
		optimizationResults.add(optResult);
	}
	
	public OptimizationResultDTO getOptimizationResult(UUID optId) {
		if (optimizationResults != null) {
			
			for (OptimizationResultDTO optRes: optimizationResults) {
				if (optId.equals(optRes.getOptId())) {
					logger.info("Returning results for this UUID.");
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
		
		// store object of chosen framework run-class, default is JeneticsRun
		if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework().equals(OptimizationFramework.JENETICS)) {
			JeneticsRun classRun = new JeneticsRun();
			logger.info("Jenetics Framework is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId());
		} else if (optdto.getOptimizationFramework() != null && optdto.getOptimizationFramework() == OptimizationFramework.OPTAPLANNER) {
			OptaPlannerRun classRun = new OptaPlannerRun();
			logger.info("OptaPlanner Framework is chosen.");
			return new Optimization(flightList, slotList, classRun, optdto.getOptId());
		} else if (optdto.getOptimizationFramework() == null){
			logger.info("Framework is not set for given UUID, therefore default Jenetics Framework is used.");
			JeneticsRun classRun = new JeneticsRun();
			return new Optimization(flightList, slotList, classRun, optdto.getOptId());
		} else {
			logger.info("No recognizable framework is chosen, therefore default Jenetics Framework is used.");
			JeneticsRun classRun = new JeneticsRun();
			return new Optimization(flightList, slotList, classRun, optdto.getOptId());
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
}
