package at.jku.dke.slotmachine.optimizer.service;

import java.time.Instant;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.optimizer.domain.*;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;
import at.jku.dke.slotmachine.optimizer.frameworks.jenetics.JeneticsRun;
import at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.OptaPlannerRun;
import at.jku.dke.slotmachine.optimizer.service.dto.*;

public class OptimizationService {

	private List<OptimizationDTO> optimizationDTOs;
	private List<OptimizationResultDTO> optimizationResults;
	private List<Optimization> optimizations;
	private static final Logger logger = LogManager.getLogger();
	
	public OptimizationDTO createAndInitialize(OptimizationDTO optdto, FrameworkDTO frameworkdto) {
		if(optimizationDTOs == null) optimizationDTOs = new LinkedList<OptimizationDTO>();
		if(optimizations == null) optimizations = new LinkedList<Optimization>();
		optimizationDTOs.add(optdto);
		Optimization opt = toOptimization(optdto, frameworkdto);
		optimizations.add(opt);	
		return optdto;
	}
	
	public void startOptimization(UUID optId) {
		logger.info("Starting optimization and running optimization algorithm.");
		if(optimizationResults == null) optimizationResults = new LinkedList<OptimizationResultDTO>();
		// search for optId
		Optimization curOpt = getOptimizationById(optId);
		
		// Map<Flight,Slot> resultMap = curOpt.getClass()
		//Map<Flight,Slot> resultMap = JeneticsRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		Map<Flight,Slot> resultMap = OptaPlannerRun.run(curOpt.getFlightList(), curOpt.getSlotList());
		
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

	private static Optimization toOptimization(OptimizationDTO optdto, FrameworkDTO frameworkdto) {
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
		if (frameworkdto != null && frameworkdto.equals("jenetics")) {
			//return new Optimization(flightList, slotList, JeneticsRun.class, optdto.getOptId());
			return new Optimization(flightList, slotList, Run.class, optdto.getOptId());
		} else if (frameworkdto != null && frameworkdto.equals("optaplanner")) {
			//return new Optimization(flightList, slotList, OptaPlannerRun.class, optdto.getOptId());
			return new Optimization(flightList, slotList, Run.class, optdto.getOptId());
		} else {
			return new Optimization(flightList, slotList, Run.class, optdto.getOptId());
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
