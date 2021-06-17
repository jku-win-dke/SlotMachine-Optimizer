package at.jku.dke.slotmachine.optimizer.frameworks.optaplanner;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Acceptor;
import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.LocalSearchPhase;
import at.jku.dke.slotmachine.optimizer.domain.OptaPlannerConfig;
import at.jku.dke.slotmachine.optimizer.domain.Optimization;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.domain.TerminationOptaPlanner;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchType;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorType;
import org.optaplanner.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.FinalistPodiumType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationCompositionStyle;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import at.jku.dke.slotmachine.optimizer.service.dto.*;
import at.jku.dke.slotmachine.optimizer.service.dto.AcceptorDTO.AcceptorTypeEnum;
import at.jku.dke.slotmachine.optimizer.service.dto.TerminationOptaPlannerDTO.TerminationComposition;
import at.jku.dke.slotmachine.optimizer.service.dto.TerminationOptaPlannerDTO.TerminationEnum;

public class OptaPlannerRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Run OptaPlanner, without prior configuration, use solver_config.xml as configuration file
	 * @param flights
	 * @param slots
	 * @return Map (solution)
	 */
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots, Optimization opt) {
		logger.info("Start optimization using OptaPlanner framework.");
		SolverFactory<FlightPrioritization> solverFactory = SolverFactory.createFromXmlResource("solver_config.xml");

        Solver<FlightPrioritization> solver = solverFactory.buildSolver();
        
        for(int i = 0; i < flights.size(); i++) {
        	flights.get(i).setSlot(slots.get(i));
        }

        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);
        logger.info("Score Calculations applications for unsolved: " + unsolvedFlightPrioritization.getApplications());
        FlightPrioritization solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        logger.info("Score Calculations applications for solved: " + solvedFlightPrioritization.getApplications());
        logger.info("print solved sequence:\n");
        for(Flight f : solvedFlightPrioritization.getFlights()) {
            logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
        }

        logger.info("Score of solved Slotsequence: " + solvedFlightPrioritization.getScore());
        Map<Flight, Slot> solution = new LinkedHashMap<Flight, Slot>();

        for(Map.Entry<Flight, Slot> entry: solvedFlightPrioritization.getSequence().entrySet()) {
            Flight f = new Flight(entry.getKey().getFlightId(),
    	   		                  entry.getKey().getScheduledTime(),
    			                  entry.getKey().getWeightMap());
    	    solution.put(f, entry.getValue());
        }
       
       logger.info("Size: " + solution.keySet().size());
       opt.setFitnessApplications(solvedFlightPrioritization.getApplications());
       return solution;
        
	}
	
	/**
	 * Run OptaPlanner, with prior configuration, do not use solver_config.xml as configuration file
	 * @param flights
	 * @param slots
	 * @param optConfig configuration of OptaPlanner
	 * @return Map (solution)
	 */
	public static Map<Flight,Slot> run(List<Flight> flights, List<Slot> slots, OptaPlannerConfig optConfig, Optimization opt) {
		logger.info("Start optimization using OptaPlanner framework - with configuration due to optaPlannerConfig.");
		SolverFactory<FlightPrioritization> solverFactory = null;
		
		// configured SolverConfig
		SolverConfig sc = new SolverConfig();
		
		// move thread count (how many threads act in parallel)
		if (optConfig.getMoveThreadCount() != null) {
			sc.setMoveThreadCount(optConfig.getMoveThreadCountString());
			logger.info("Move Thread Count set to " + sc.getMoveThreadCount() + ".");
		} else {
			logger.info("Move Thread Count was not set. Default of NONE is used.");
			sc.setMoveThreadCount("NONE");
		}
		
		// environment mode
		if (optConfig.getEnvironmentMode() != null) {
			sc.setEnvironmentMode(optConfig.getEnvironmentModeSolver());
			logger.info("Environment Mode set to "+ sc.getEnvironmentMode() + ".");
		} else {
			logger.info("Environment Mode was not set. Default of REPRODUCIBLE is used.");
			sc.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
		}
		
		// Solution class, entity class (list) and easy score calculator are used with default values
		sc.setSolutionClass(at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.FlightPrioritization.class);
		List<Class<?>> classListEntity = new LinkedList<Class<?>>();
		classListEntity.add(at.jku.dke.slotmachine.optimizer.domain.Flight.class);
		sc.setEntityClassList(classListEntity);
		ScoreDirectorFactoryConfig sdfc = new ScoreDirectorFactoryConfig();
		sdfc.setEasyScoreCalculatorClass(at.jku.dke.slotmachine.optimizer.frameworks.optaplanner.FlightPrioritizationEasyScoreCalculator.class);
		sc.setScoreDirectorFactoryConfig(sdfc);
		
		// termination
		TerminationConfig tc = new TerminationConfig();
		tc = getTerminationsConfig(optConfig.getTermination(), 0);
		sc.setTerminationConfig(tc);
		
		List<PhaseConfig> phases = new LinkedList<PhaseConfig>();
		// construction heuristics phase
		if (optConfig.getConstructionHeuristic() != null) {
			ConstructionHeuristicPhaseConfig ch = new ConstructionHeuristicPhaseConfig();
			ch.setConstructionHeuristicType(optConfig.getConstructionHeuristic().getConstructionHeuristicType());
			// TODO integrate termination here (not suggested to use termination according to OptaPlanner user guide,
			// as construction heuristic phase terminates automatically)
			logger.info("No termination method is set here, as construction heuristic phase terminates automatically.");
			logger.info("Construction heuristic type of " + ch.getConstructionHeuristicType() + " is used.");
			phases.add(ch);	
		}
		// local search phase
		if (optConfig.getLocalSearch() != null) {
			LocalSearchPhaseConfig ls = getLocalSearchConfig(optConfig.getLocalSearch());
			
			phases.add(ls);
		}
		
		sc.setPhaseConfigList(phases);
		solverFactory = SolverFactory.create(sc);
		

        Solver<FlightPrioritization> solver = solverFactory.buildSolver();
        
        for(int i = 0; i < flights.size(); i++) {
        	flights.get(i).setSlot(slots.get(i));
        }

        FlightPrioritization unsolvedFlightPrioritization = new FlightPrioritization(slots, flights);
        logger.info("Score Calculations applications for unsolved: " + unsolvedFlightPrioritization.getApplications());
        FlightPrioritization solvedFlightPrioritization = solver.solve(unsolvedFlightPrioritization);
        logger.info("Score Calculations applications for solved: " + solvedFlightPrioritization.getApplications());
        logger.info("print solved sequence:\n");
        for(Flight f : solvedFlightPrioritization.getFlights()) {
            logger.info(f.getFlightId() + " :" + f.getSlot().getTime());
        }

        logger.info("Score of solved Slotsequence: " + solvedFlightPrioritization.getScore());
        Map<Flight, Slot> solution = new LinkedHashMap<Flight, Slot>();

        for(Map.Entry<Flight, Slot> entry: solvedFlightPrioritization.getSequence().entrySet()) {
            Flight f = new Flight(entry.getKey().getFlightId(),
    	   		                  entry.getKey().getScheduledTime(),
    			                  entry.getKey().getWeightMap());
    	    solution.put(f, entry.getValue());
        }
       
       logger.info("Size: " + solution.keySet().size());
       opt.setFitnessApplications(solvedFlightPrioritization.getApplications());
       return solution;
        
	}

	/**
	 * Converts TerminationEnum and used value to TerminationConfig
	 * @param termination1
	 * @param terminationScore1
	 * @param terminationValue1
	 * @param terminationBoolean1
	 * @param phase (0 = solver; 1 = constructionHeuristic; 2 = localSearch)
	 * @return TerminationConfig
	 */
	private static TerminationConfig getTerminationConfig(TerminationEnum term, HardSoftScore termScore,
			double termValue, boolean termBoolean, int phase) {
		TerminationConfig tc = new TerminationConfig();
		switch (term) {
		// ...SPENTLIMIT (Time) is suggested for use on localSearchPhase or Solver, not on 
		// constructionHeuristicPhase
		// (UNIMPROVED)STEPCOUNTTERMINATION can not be used on solver directly
			case MILLISECONDSSPENTLIMIT: 
				tc.setMillisecondsSpentLimit((long) termValue);
				logger.info("Termination set to Milliseconds Spent Limit of " + (long) termValue + ".");
				return tc;
			case SECONDSSPENTLIMIT:
				tc.setSecondsSpentLimit((long) termValue);
				logger.info("Termination set to Seconds Spent Limit of " + (long) termValue + ".");
				return tc;
			case MINUTESSPENTLIMIT:
				tc.setMinutesSpentLimit((long) termValue);
				logger.info("Termination set to Minutes Spent Limit of " + (long) termValue + ".");
				return tc;
			case HOURSSPENTLIMIT:
				tc.setHoursSpentLimit((long) termValue);
				logger.info("Termination set to Hours Spent Limit of " + (long) termValue + ".");
				return tc;
			case DAYSSPENTLIMIT:
				tc.setDaysSpentLimit((long) termValue);
				logger.info("Termination set to Days Spent Limit of " + (long) termValue + ".");
				return tc;
			case UNIMPROVEDMILLISECONDSSPENTLIMIT: 
				tc.setUnimprovedMillisecondsSpentLimit((long) termValue);
				logger.info("Termination set to Unimproved Milliseconds Spent Limit of " + (long) termValue + ".");
				return tc;
			case UNIMPROVEDSECONDSSPENTLIMIT:
				tc.setUnimprovedSecondsSpentLimit((long) termValue);
				logger.info("Termination set to Unimproved Seconds Spent Limit of " + (long) termValue + ".");
				return tc;
			case UNIMPROVEDMINUTESSPENTLIMIT:
				tc.setUnimprovedMinutesSpentLimit((long) termValue);
				logger.info("Termination set to Unimproved Minutes Spent Limit of " + (long) termValue + ".");
				return tc;
			case UNIMPROVEDHOURSSPENTLIMIT:
				tc.setUnimprovedHoursSpentLimit((long) termValue);
				logger.info("Termination set to Unimproved Hours Spent Limit of " + (long) termValue + ".");
				return tc;
			case UNIMPROVEDDAYSSPENTLIMIT:
				tc.setUnimprovedDaysSpentLimit((long) termValue);
				logger.info("Termination set to Unimproved Days Spent Limit of " + (long) termValue + ".");
				return tc;		
			case BESTSCORELIMIT:
				// if termScore is null, use default values of 0hard/0soft
				String scoreString;
				if (termScore != null) {
					scoreString = termScore.getHardScore() + "";
					scoreString = scoreString + "hard/";
					scoreString = scoreString + termScore.getSoftScore();
					scoreString = scoreString + "soft";
				} else {
					scoreString = "0hard/0soft";
				}
				tc.setBestScoreLimit(scoreString);
				logger.info("Termination set to best score limit of " + scoreString + ".");
				return tc;
			case BESTSCOREFEASIBLE:
				// termBoolean has to be true, to use setBestScoreFeasible
				if (termBoolean == true) {
					tc.setBestScoreFeasible(termBoolean);
					logger.info("Termination set to best score feasible of " + termBoolean + ".");
				} else {
					// otherwise (if termBoolean is false), use default values
					logger.info("Termination of best score feasible of false is not allowed.");
					tc.setUnimprovedSecondsSpentLimit((long) 10);
					logger.info("Termination set to default value of unimproved Seconds Spent " +
					"Limit of 10.");
				}
				return tc;
			case STEPCOUNTTERMINATION:
				// can only be used on localSearchPhase/constructionHeuristicPhase
				if (phase == 0) { //solver phase
					logger.info("Step count limit for termination cannot be used on solver directly.");
					tc.setUnimprovedSecondsSpentLimit((long) 10);
					logger.info("Termination set to default value of unimproved Seconds Spent " +
					"Limit of 10.");
				} else { //localSearchPhase/constructionHeuristicPhase
					tc.setStepCountLimit((int) termValue);
					logger.info("Termination set to step count limit of " + (int) termValue + ".");
				}
				return tc;
			case UNIMPROVEDSTEPCOUNTTERMINATION:
				// can only be used on localSearchPhase/constructionHeuristicPhase
				if (phase == 0) { //solver phase
					logger.info("Unimproved step count limit for termination cannot be "
							+ "used on solver directly.");
					tc.setUnimprovedSecondsSpentLimit((long) 10);
					logger.info("Termination set to default value of unimproved Seconds Spent " +
					"Limit of 10.");
				} else { //localSearchPhase/constructionHeuristicPhase
					tc.setUnimprovedStepCountLimit((int) termValue);
					logger.info("Termination set to unimproved step count limit of " + (int) termValue + ".");
				}
				return tc;
			case SCORECALCULATIONCOUNTLIMIT:
				tc.setScoreCalculationCountLimit((long) termValue);
				logger.info("Termination set to score calculation count limit of " + (long) termValue + ".");
				return tc;
			default:
				// default value (unimproved seconds spent limit of 10 seconds)
				tc.setUnimprovedSecondsSpentLimit((long) 360);
				logger.info("No Termination method has been properly set. Default of UNIMPROVED_SECONDS_SPENT_LIMIT (" + tc.getUnimprovedSecondsSpentLimit() 
					+ " seconds) is used.");
				return tc;
		}
	}
	
	/**
	 * Used to convert TerminationComposition to TerminationCompositionStyle with 
	 * default value of AND (for usage with termination method 1 and 2)
	 * 
	 * @param termComp TerminationsComposition (AND or OR)
	 * @return TerminationCompositionStyle (AND or OR)
	 */
	private static TerminationCompositionStyle getTerminationComposition(TerminationComposition termComp) {
		// set termination composition
		TerminationCompositionStyle tcStyle = TerminationCompositionStyle.AND;
		switch (termComp) {
			case AND:
				tcStyle = TerminationCompositionStyle.AND;
				break;
			case OR:
				tcStyle = TerminationCompositionStyle.OR;
				break;
			default:
				tcStyle = TerminationCompositionStyle.AND;
				break;
		}
		logger.info("Termination and Termination 2 have composition style of " + tcStyle);
		return tcStyle;
	}
	
	/**
	 * Converts TerminationEnum and used value to TerminationConfig for one or two termination methods
	 * methods (using getTerminationConfig() and getTerminationComposition().
	 * 
	 * Default value is UNIMPROVED_SECONDS_SPENT_LIMIT (10 seconds)
	 * @param term TerminationOptaPlanner (containing all relevant information for the termination methods)
	 * @param phase current phase (0 = solver, 1 = construction heuristic, 2 = local search)
	 * @return TerminationConfig
	 */
	private static TerminationConfig getTerminationsConfig(TerminationOptaPlanner term, int phase) {
		TerminationConfig tc = new TerminationConfig();
		if (term == null) {
			tc.setUnimprovedSecondsSpentLimit((long) 360);
			logger.info("No Termination method has been properly set. Default of UNIMPROVED_SECONDS_SPENT_LIMIT (" + tc.getUnimprovedSecondsSpentLimit() 
				+ " seconds) is used.");
			return tc;
		}
		if ((term.getTermination1() == null && term.getTermination2() != null)
			|| (term.getTermination1() != null && term.getTermination2() == null)
			|| (term.getTermination1() != null && term.getTermination2() != null)) {
			// only Termination1 is set (or only Termination2 is set)
			if (term.getTermination1() != null && term.getTermination2() == null) {
				tc = getTerminationConfig(term.getTermination1(), term.getTerminationScore1(), term.getTerminationValue1(), term.isTerminationBoolean1(), phase);
			} else if (term.getTermination1() == null && term.getTermination2() != null) {
				tc = getTerminationConfig(term.getTermination2(), term.getTerminationScore2(), term.getTerminationValue2(), term.isTerminationBoolean2(), phase);
			// Termination1 and Termination2 are set
			} else if (term.getTermination1() != null && term.getTermination2() != null) {
				List<TerminationConfig> tcs = new LinkedList<TerminationConfig>();
				tcs.add(getTerminationConfig(term.getTermination1(), term.getTerminationScore1(), term.getTerminationValue1(), term.isTerminationBoolean1(), phase));
				tcs.add(getTerminationConfig(term.getTermination2(), term.getTerminationScore2(), term.getTerminationValue2(), term.isTerminationBoolean2(), phase));
				tc.setTerminationConfigList(tcs);
				tc.setTerminationCompositionStyle(getTerminationComposition(term.getTermComp()));
			}
			return tc;
		} else {
			tc.setUnimprovedSecondsSpentLimit((long) 360);
			logger.info("No Termination method has been properly set. Default of UNIMPROVED_SECONDS_SPENT_LIMIT (" + tc.getUnimprovedSecondsSpentLimit() 
				+ " seconds) is used.");
			return tc;
		}
		
	}

	/**
	 * Converts LocalSearchPhase to LocalSearchPhaseConfig with two options to configure the 
	 * local search phase: simple (usage of localSearchType) or advanced (usage of the other 
	 * parameters, such as acceptor, forager, ...). Simple configuration is used as default.
	 * @param localSearch Parameters of LocalSearch, as given by the user
	 * @return LocalSearchPhaseConfig for OptaPlanner configuration
	 */
	private static LocalSearchPhaseConfig getLocalSearchConfig(LocalSearchPhase localSearch) {
		LocalSearchPhaseConfig localSearchConfig = new LocalSearchPhaseConfig();
		// integrate Termination
		if (localSearch.getTermination()  != null) {
			TerminationConfig tc = getTerminationsConfig(localSearch.getTermination(), 2);
			localSearchConfig.setTerminationConfig(tc);
		}
		if (localSearch.getLocalSearchEnum() != null) {
			// ignore other settings, LocalSearchType is the default setting to use 
			// (except for Simulated Annealing, uses simulated annealing starting temperature)
			localSearchConfig.setLocalSearchType(localSearch.getLocalSearchType());
			logger.info("Local Search Type is set to " + localSearch.getLocalSearchType() 
				+ ", therefore simple configuration is used.");
			// if LocalSearchType == Simulated Annealing, Simulated Annealing Starting Temperature is required
			if (localSearchConfig.getLocalSearchType().equals(LocalSearchType.SIMULATED_ANNEALING)) {
				LocalSearchAcceptorConfig localSearchAcceptorConfig = new LocalSearchAcceptorConfig();
				if (localSearch.getAcceptor() != null) {
					localSearchAcceptorConfig.setSimulatedAnnealingStartingTemperature(localSearch.getAcceptor().getSimulAnnealStartTempString());
				} else {
					localSearchAcceptorConfig.setSimulatedAnnealingStartingTemperature("0hard/100soft");
					logger.info("As no starting temperature for simulated annealing has been found, the default value will be used.");
				}
				logger.info("As Local Search Type Simulated Annealing is used, Starting Temperature has been set to "
						+ localSearchAcceptorConfig.getSimulatedAnnealingStartingTemperature() + ".");
				localSearchConfig.setAcceptorConfig(localSearchAcceptorConfig);
				// local search type is not allowed to be set for Simulated Annealing, therefore it is removed
				localSearchConfig.setLocalSearchType(null);
			}
			
			return localSearchConfig;
		}
		// otherwise, used advanced settings
		LocalSearchAcceptorConfig localSearchAcceptorConfig = new LocalSearchAcceptorConfig();
		LocalSearchForagerConfig localSearchForagerConfig = new LocalSearchForagerConfig();
		if (localSearch.getAcceptor() != null) {
			Acceptor acceptorDomain = localSearch.getAcceptor();
			int acceptedCountLimitDefault = 1; // default value if not set individually
			
			// HILL CLIMBING
			if (acceptorDomain.getAcceptorType() != null && acceptorDomain.getAcceptorType().equals(AcceptorTypeEnum.HILLCLIMBING)) {
				AcceptorType acceptorType = AcceptorType.HILL_CLIMBING;
				List<AcceptorType> acceptorTypeList = new LinkedList<AcceptorType>();
				acceptorTypeList.add(acceptorType);
				localSearchAcceptorConfig.setAcceptorTypeList(acceptorTypeList);
				logger.info("Local Search uses HILL CLIMBING (advanced settings).");
				acceptedCountLimitDefault = 1;
				
				// VARIABLE NEIGHBORHOOD DESCENT (currently only implemented for Hill Climbing)
				// throws potential error with the usage of OptaPlanner configuration
				//		"Cannot invoke "java.util.List.stream()" because "innerMoveSelectorList" is null"
//				if (localSearch.getSelectionOrder() != null) {
//					UnionMoveSelectorConfig moveSelectorConfig = new UnionMoveSelectorConfig();
//					moveSelectorConfig.setSelectionOrder(SelectionOrder.ORIGINAL);
//					localSearchConfig.setMoveSelectorConfig(moveSelectorConfig);
//					String loggertext = "Local Search uses VARIABLE NEIGHBORHOOD DESCENT with HILL CLIMBING";
//					if (localSearch.getForager() != null && localSearch.getForager().getPickEarlyType() != null &&
//							localSearch.getForager().getPickEarlyLocalSearchType().equals(LocalSearchPickEarlyType.FIRST_LAST_STEP_SCORE_IMPROVING)) {
//						localSearchForagerConfig.setPickEarlyType(LocalSearchPickEarlyType.FIRST_LAST_STEP_SCORE_IMPROVING);
//						loggertext = loggertext + " and PickEarlyType of FIRST_LAST_STEP_SCORE_IMPROVING";
//					}
//					loggertext = loggertext + ".";
//					logger.info(loggertext);
//				}
			}
			
			// TABU SEARCH
			if (acceptorDomain.getAcceptorType() != null && acceptorDomain.getAcceptorType().equals(AcceptorTypeEnum.TABUSEARCH)) {
				// values with value == 0 are assumed to be not set
				// entity ratio (default) or size
				// can be reworked to allow other algorithm types to use tabu search parameters
				// acceptor type has to be set as well
				String loggertext = "Local Search uses TABU SEARCH (advanced settings) with ";
				List<AcceptorType> acceptorTypeList = new LinkedList<AcceptorType>();
				
				localSearchAcceptorConfig.setAcceptorTypeList(acceptorTypeList);
				if (acceptorDomain.getEntityTabuRatio() != 0) {
					localSearchAcceptorConfig.setEntityTabuRatio(acceptorDomain.getEntityTabuRatio());
					AcceptorType at = AcceptorType.ENTITY_TABU;
					acceptorTypeList.add(at);
					loggertext = loggertext + "entity tabu ratio of " + acceptorDomain.getEntityTabuRatio() + "; ";
				}
				if (acceptorDomain.getEntityTabuRatio() == 0 && acceptorDomain.getEntityTabuSize() != 0) {
					localSearchAcceptorConfig.setEntityTabuSize(acceptorDomain.getEntityTabuSize());
					AcceptorType at = AcceptorType.ENTITY_TABU;
					acceptorTypeList.add(at);
					loggertext = loggertext + "entity tabu size of " + acceptorDomain.getEntityTabuSize() + "; ";
				}
				
				// value ratio (default) or size
				if (acceptorDomain.getValueTabuRatio() != 0) { // NOT IMPLEMENTED BY OPTAPLANNER 
					// (error message: getValueCount is not yet supported - this blocks ValueRatioTabuSizeStrategy)
					//localSearchAcceptorConfig.setValueTabuRatio(acceptorDomain.getValueTabuRatio());
					//AcceptorType at = AcceptorType.VALUE_TABU;
					//acceptorTypeList.add(at);
					loggertext = loggertext + "value tabu ratio of " + acceptorDomain.getValueTabuRatio() + " (will not be used " +
							"as OptaPlanner has not implemented it yet!); ";
					
				}
				if (/*acceptorDomain.getValueTabuRatio() == 0 && */acceptorDomain.getValueTabuSize() != 0) {
					localSearchAcceptorConfig.setValueTabuSize(acceptorDomain.getValueTabuSize());
					AcceptorType at = AcceptorType.VALUE_TABU;
					acceptorTypeList.add(at);
					loggertext = loggertext + "value tabu size of " + acceptorDomain.getValueTabuSize() + "; ";
				}
				
				// move size
				if (acceptorDomain.getMoveTabuSize() != 0) {
					localSearchAcceptorConfig.setMoveTabuSize(acceptorDomain.getMoveTabuSize());
					AcceptorType at = AcceptorType.MOVE_TABU;
					acceptorTypeList.add(at);
					loggertext = loggertext + "move tabu size of " + acceptorDomain.getMoveTabuSize() + "; ";
				}
				
				// undo move size
				if (acceptorDomain.getUndoMoveTabuSize() != 0) {
					localSearchAcceptorConfig.setUndoMoveTabuSize(acceptorDomain.getUndoMoveTabuSize());
					AcceptorType at = AcceptorType.UNDO_MOVE_TABU;
					acceptorTypeList.add(at);
					loggertext = loggertext + "undo move tabu size of " + acceptorDomain.getUndoMoveTabuSize() + "; ";
				}
				
				logger.info(loggertext);
				//localSearchAcceptorConfig.setAcceptorTypeList(acceptorTypeList); //not necessary for TABU SEARCH
				acceptedCountLimitDefault = 1000;
			}
			
			// SIMULATED ANNEALING
			if (acceptorDomain.getSimulAnnealStartTemp() != null) {
				localSearchAcceptorConfig.setSimulatedAnnealingStartingTemperature(acceptorDomain.getSimulAnnealStartTempString());
				logger.info("Local Search uses SIMULATED ANNEALING (advanced settings) with starting temperature of " 
						+ acceptorDomain.getSimulAnnealStartTempString() + ".");
				acceptedCountLimitDefault = 1;
			}
			
			// LATE ACCEPTANCE (as value may be set with default value (0), 
			// late acceptance will only work with values != 0
			if (acceptorDomain.getLateAcceptanceSize() != 0) {
				localSearchAcceptorConfig.setLateAcceptanceSize(acceptorDomain.getLateAcceptanceSize());
				logger.info("Local Search uses LATE ACCEPTANCE (advanced settings) with late acceptance size of "
						+ acceptorDomain.getLateAcceptanceSize() + ".");
				acceptedCountLimitDefault = 4;
			}
			
			// GREAT DELUGE
			if (acceptorDomain.getAcceptorType() != null && acceptorDomain.getAcceptorType().equals(AcceptorTypeEnum.GREATDELUGE)) {
				AcceptorType acceptorType = AcceptorType.GREAT_DELUGE;
				List<AcceptorType> acceptorTypeList = new LinkedList<AcceptorType>();
				acceptorTypeList.add(acceptorType);
				localSearchAcceptorConfig.setAcceptorTypeList(acceptorTypeList);
				if (acceptorDomain.getGrDelInitWaterLevel() != null) {
					// great deluge initial water level cannot be set within localSearchAcceptorConfig
					logger.info("Great Deluge Initial Water Level will not be used, as recommended.");
				}
				if (acceptorDomain.getGrDelWaterLevelIncrRatio() != 0) {
					logger.info("Local Search uses GREAT DELUGE (advanced settings) with water level increment ratio of "
							+ acceptorDomain.getGrDelWaterLevelIncrRatio());
					localSearchAcceptorConfig.setGreatDelugeWaterLevelIncrementRatio(acceptorDomain.getGrDelWaterLevelIncrRatio());
				} else {
					String scoreString;
					if (acceptorDomain.getGrDelWaterLevelIncrScore() != null) {
						scoreString = acceptorDomain.getGrDelWaterLevelIncrScore().getHardScore() + "";
						scoreString = scoreString + "hard/";
						scoreString = scoreString + acceptorDomain.getGrDelWaterLevelIncrScore().getSoftScore();
						scoreString = scoreString + "soft";
					} else {
						scoreString = "0hard/100soft";
					}
					logger.info("Local Search uses GREAT DELUGE (advanced settings) with water level increment score of "
							+ scoreString);
					localSearchAcceptorConfig.setGreatDelugeWaterLevelIncrementScore(scoreString);
				}
				
				acceptedCountLimitDefault = 1;
			}
			
			// Step Counting Hill Climbing
			if (acceptorDomain.getStepCountHillClimbSize() != 0) {
				localSearchAcceptorConfig.setStepCountingHillClimbingSize(acceptorDomain.getStepCountHillClimbSize());
				logger.info("Local Search uses STEP COUNTING HILL CLIMBING (advanced settings) with size of "
						+ acceptorDomain.getStepCountHillClimbSize() + ".");
				acceptedCountLimitDefault = 1;
			}

			// check for Strategic Oscillation
			// currently only enabled for TABU SEARCH
			if (acceptorDomain.getAcceptorType() != null && acceptorDomain.getAcceptorType().equals(AcceptorTypeEnum.TABUSEARCH)) {
				if (localSearch.getForager() != null && localSearch.getForager().getFinalistPodiumType() != null) {
					switch (localSearch.getForager().getFinalistPodiumType()) {
						case HIGHESTSCORE: 
							localSearchForagerConfig.setFinalistPodiumType(FinalistPodiumType.HIGHEST_SCORE);
							break;
						case STRATEGICOSCILLATION: 
							localSearchForagerConfig.setFinalistPodiumType(FinalistPodiumType.STRATEGIC_OSCILLATION);
							break;
						case STRATEGICOSCILLATIONBYLEVEL:
							localSearchForagerConfig.setFinalistPodiumType(FinalistPodiumType.STRATEGIC_OSCILLATION_BY_LEVEL);
							break;
						case STRATEGICOSCILLATIONBYLEVELONBESTSCORE:
							localSearchForagerConfig.setFinalistPodiumType(FinalistPodiumType.STRATEGIC_OSCILLATION_BY_LEVEL_ON_BEST_SCORE);
							break;
						default:
							localSearchForagerConfig.setFinalistPodiumType(FinalistPodiumType.HIGHEST_SCORE);
							break;
					}
					logger.info("For TABU SEARCH, Strategic Oscillation is enabled (" + localSearchForagerConfig.getFinalistPodiumType() + ").");
				}
			}
			
			// set acceptedCountLimit value (cannot be negative or zero)
			if (localSearch.getForager() != null && localSearch.getForager().getAcceptedCountLimit() > 0) {
				localSearchForagerConfig.setAcceptedCountLimit(localSearch.getForager().getAcceptedCountLimit());
				logger.info("Accepted Count Limit of " + localSearchForagerConfig.getAcceptedCountLimit() + " will be used.");
			} else {
				localSearchForagerConfig.setAcceptedCountLimit(acceptedCountLimitDefault);
				logger.info("Accepted Count Limit of " + localSearchForagerConfig.getAcceptedCountLimit() + " (default value) will be used.");
			}
			
			localSearchConfig.setAcceptorConfig(localSearchAcceptorConfig);
			localSearchConfig.setForagerConfig(localSearchForagerConfig);
			
		} else {
			logger.info("Advanced Settings require Acceptor Configuration. Default Value of HILL CLIMBING will be used.");
			localSearchConfig.setLocalSearchType(LocalSearchType.HILL_CLIMBING);
		}

		return localSearchConfig;
	}
}
