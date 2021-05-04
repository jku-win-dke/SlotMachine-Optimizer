package at.jku.dke.slotmachine.optimizer.frameworks.jenetics;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.JeneticsConfig;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;
import at.jku.dke.slotmachine.optimizer.service.dto.JeneticConfigDTO.Alterer;
import at.jku.dke.slotmachine.optimizer.service.dto.JeneticConfigDTO.Termination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jenetics.BoltzmannSelector;
import io.jenetics.Crossover;
import io.jenetics.EnumGene;
import io.jenetics.ExponentialRankSelector;
import io.jenetics.Genotype;
import io.jenetics.LinearRankSelector;
import io.jenetics.Mutator;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.Phenotype;
import io.jenetics.ProbabilitySelector;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.Selector;
import io.jenetics.StochasticUniversalSelector;
import io.jenetics.SwapMutator;
import io.jenetics.TournamentSelector;
import io.jenetics.TruncationSelector;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.Limits;
import io.jenetics.engine.RetryConstraint;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import io.jenetics.util.Seq;

public class JeneticsRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Run Jenetics with default parameters.
	 * 
	 * @param flights list of flights
	 * @param slots list of possible slots
	 * @return
	 */
	public static Map<Flight, Slot> run(List<Flight> flights, List<Slot> slots) {
		logger.info("Start optimization using Jenetics framework");
		
		SlotAllocationProblem p = new SlotAllocationProblem(
				ISeq.of(flights), ISeq.of(slots));
		Constraint<EnumGene<Integer>, Integer> constraint = p.constraint().get();
		
		Engine<EnumGene<Integer>, Integer> e = Engine.builder(p)
				.populationSize(100)
				.alterers(
						new SwapMutator<EnumGene<Integer>, Integer>(0.2),
						new PartiallyMatchedCrossover<>(0.35))
				.constraint(constraint)
				.build();
		
        EvolutionStatistics <Integer, ?> statistics = EvolutionStatistics.ofNumber();

        Genotype<EnumGene<Integer>> result = e.stream()
        		//.limit(Limits.bySteadyFitness(250))
        		//.limit(Limits.byFitnessThreshold(1))
        		// try to have the worst fitness value at -99999, meaning that
        		// the algorithms tries to use no values before scheduledTime
        		// (the hard constraint (-100000)), unless the value is not
        		// improving after 2500 generations or after 6 minutes
        		.limit(pred -> pred.worstFitness() < -99999)
        		.limit(Limits.bySteadyFitness(2500))
        		.limit(Limits.byExecutionTime(Duration.ofSeconds(360)))
        		.peek(statistics)
        		.collect(EvolutionResult.toBestGenotype());
        
        logger.info("Statistics:\n" + statistics);

        logger.info("Result: " + result);

        Map<Flight, Slot> resultMap = p.decode(result);

        logger.info("Size of result: " + resultMap.keySet().size());
        logger.info("Result Map:\n");
        for(Flight f : resultMap.keySet()) {
            logger.info(f.getFlightId() + " " + resultMap.get(f).getTime());
        }
        logger.info("Fitness Function applications: " + p.getFitnessIterations());
        return resultMap;
	}
	
	/**
	 * Run Jenetics with configured parameters according to jenConfig.
	 * 
	 * @param flights list of flights
	 * @param slots list of possible slots
	 * @param jenConfig parameters, to change the configuration of Jenetics for this run
	 * @return
	 */
	public static Map<Flight, Slot> run(List<Flight> flights, List<Slot> slots, JeneticsConfig jenConfig) {
		if (jenConfig == null) {
			return run(flights, slots);
		}
		
		SlotAllocationProblem p = new SlotAllocationProblem(
				ISeq.of(flights), ISeq.of(slots));
		Constraint<EnumGene<Integer>, Integer> constraint = p.constraint().get();
		
		// engine takes mutator and crossover from jenConfig - if no alterer detected default values are used
		Engine<EnumGene<Integer>, Integer> e = Engine.builder(p)
				.populationSize(getPopulationSize(jenConfig))
				.alterers(
						getMutator(jenConfig),
						getCrossover(jenConfig))
				.offspringSelector(getOffspringSelector(jenConfig))
				.survivorsSelector(getSurvivorSelector(jenConfig))
				.maximalPhenotypeAge(getMaximalPhenotypeAge(jenConfig))
				.offspringFraction(getOffspringFraction(jenConfig))
				.constraint(constraint)
				.build();
		logger.info("Alterer used by the engine: " + e.alterer().toString());
		logger.debug("Constraints used by the engine: " + e.constraint().toString());
		logger.debug("Problem used by the engine: " + e.toString() + ": " + p.toString());
		logger.info("Offspring Selector used by the engine: " + e.offspringSelector().toString());
		logger.info("Survivors Selector used by the engine: " + e.survivorsSelector().toString());
		logger.info("Maximal phenotype age: " + e.maximalPhenotypeAge() + " | offspring fraction: " + (double) e.offspringSize()/e.populationSize() 
				+ " | population size: " + e.populationSize());
        EvolutionStatistics <Integer, ?> statistics = EvolutionStatistics.ofNumber();

        Genotype<EnumGene<Integer>> result = null;
        int randomNr = 963;
        List<Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>> limits = getLimits(jenConfig);
        if (limits != null) {
	        switch (limits.size()) {
	        	case 1:
	        		logger.info("1 termination method detected: " + limits.get(0).toString());
	            	result = RandomRegistry.with(new Random(randomNr), r ->
		        		e.stream()
						.limit(limits.get(0))
						.peek(statistics)
						.collect(EvolutionResult.toBestGenotype())
		        	);
	            	break;
	        	case 2:
	        		logger.info("2 termination method detected: " + limits.get(0).toString()
	        				+ " | " + limits.get(1).toString());
	            	result = RandomRegistry.with(new Random(randomNr), r ->
		        		e.stream()
						.limit(limits.get(0))
						.limit(limits.get(1))
						.peek(statistics)
						.collect(EvolutionResult.toBestGenotype())
		        	);
	            	break;
	        	case 3:
	        		logger.info("3 termination method detected: " + limits.get(0).toString()
	        				+ " | " + limits.get(1).toString() 
	        				+ " | " + limits.get(2).toString());
	        		result = RandomRegistry.with(new Random(randomNr), r ->
		        		e.stream()
						.limit(limits.get(0))
						.limit(limits.get(1))
						.limit(limits.get(2))
						.peek(statistics)
						.collect(EvolutionResult.toBestGenotype())
		        	);
	        		break;
	        	default:
	        		logger.info("No termination method detected. Default values will be used.");
	            	result = RandomRegistry.with(new Random(randomNr), r ->
		        		e.stream()
						.limit(pred -> pred.worstFitness() < -99999)
						.limit(Limits.bySteadyFitness(2500))
						.limit(Limits.byExecutionTime(Duration.ofSeconds(180)))
						.peek(statistics)
						.collect(EvolutionResult.toBestGenotype())
		        	);
	            	break;
	        }
        } else {
    		logger.info("No termination method detected. Default values will be used.");
        	result = RandomRegistry.with(new Random(randomNr), r ->
        		e.stream()
				.limit(pred -> pred.worstFitness() < -99999)
				.limit(Limits.bySteadyFitness(2500))
				.limit(Limits.byExecutionTime(Duration.ofSeconds(180)))
				.peek(statistics)
				.collect(EvolutionResult.toBestGenotype())
        	);
        }
        
//        // use random registry to compare different settings from jenConfig
//        Genotype<EnumGene<Integer>> result = RandomRegistry.with(new Random(963), r ->
//			        e.stream()
//					//.limit(Limits.bySteadyFitness(250))
//					//.limit(Limits.byFitnessThreshold(1))
//					// try to have the worst fitness value at -99999, meaning that
//					// the algorithms tries to use no values before scheduledTime
//					// (the hard constraint (-100000)), unless the value is not
//					// improving after 2500 generations or after 6 minutes
//					.limit(pred -> pred.worstFitness() < -99999)
//					.limit(Limits.bySteadyFitness(2500))
//					.limit(Limits.byExecutionTime(Duration.ofSeconds(360)))
//					.peek(statistics)
//					.collect(EvolutionResult.toBestGenotype())
//        		);
        
        logger.info("Statistics:\n" + statistics);

        logger.info("Result: " + result);

        Map<Flight, Slot> resultMap = p.decode(result);

        logger.info("Size of result: " + resultMap.keySet().size());
        logger.info("Result Map:\n");
        for(Flight f : resultMap.keySet()) {
            logger.info(f.getFlightId() + " " + resultMap.get(f).getTime());
        }
        logger.info("Fitness Function applications: " + p.getFitnessIterations());
        return resultMap;	
	}

	// UTILITY METHODS
	/**
	 * Gets the crossover method according to jenConfig. If no crossover method is detected
	 * it returns the default value (PartiallyMatchedCrossover<>(0.35))
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return crossover method (as an object, e.g. PartiallyMatchedCrossover<>(0.35))
	 */
	private static Crossover<EnumGene<Integer>, Integer> getCrossover(JeneticsConfig jenConfig) {
		if (jenConfig.getAlterer() != null && jenConfig.getAlterer().length > 0) {
			Alterer alterer0 = jenConfig.getAlterer()[0];
			Alterer alterer1 = null;
			if (jenConfig.getAlterer().length == 2) {
				alterer1 = jenConfig.getAlterer()[1];
			} else if (jenConfig.getAlterer().length > 2) {
				logger.info("More than 2 Alterers detected. Only the first two will be used.");
			}
			
			// detect if first or second alterer is crossover (or both)
			boolean crossover0 = false;
			boolean crossover1 = false;
			if (alterer0 != null && alterer0.equals(Alterer.PARTIALLYMATCHEDCROSSOVER)) {
				crossover0 = true;
			}
			if (alterer1 != null && alterer1.equals(Alterer.PARTIALLYMATCHEDCROSSOVER)) {
				crossover1 = true;
			}
			
			// if both fields contain crossover or the first field contains crossovers
			if (crossover0) {
				if (crossover0 && crossover1) {
				logger.info("Only the first crossover will be used, the second crossover will be ignored.");
				}
				// get alterer attribute (if existing)
				
				// for PartiallyMatchedCrossover and similiar crossover (1 parameter required)
				if (alterer0.equals(Alterer.PARTIALLYMATCHEDCROSSOVER)) {
					if (jenConfig.getAltererAttributes() != null && jenConfig.getAltererAttributes().length > 0 &&
							jenConfig.getAltererAttributes()[0].length > 0) {
						double parameter = jenConfig.getAltererAttributes()[0][0];
						logger.info("Crossover: PartiallyMatchedCrossover will be used with probability of " + parameter + ".");
						return new PartiallyMatchedCrossover<>(parameter);
					}
				}
				// possible other crossover (with (0 or 1) or (0 or 1 or 2) parameters required)
				
			// if second field contains crossover
			} else if (crossover1) {
				if (alterer1.equals(Alterer.PARTIALLYMATCHEDCROSSOVER)) {
					if (jenConfig.getAltererAttributes() != null && jenConfig.getAltererAttributes().length > 1 &&
							jenConfig.getAltererAttributes()[1].length > 0) {
						double parameter = jenConfig.getAltererAttributes()[1][0];
						logger.info("Crossover: PartiallyMatchedCrossover will be used with probability of " + parameter + ".");
						return new PartiallyMatchedCrossover<>(parameter);
					}
				}
			} else {
				logger.info("No crossover detected. Default value will be used.");
			}
		}
		return new PartiallyMatchedCrossover<>(0.35);
	}

	/**
	 * Gets the mutator method according to jenConfig. If no mutator method is detected
	 * it returns the default value (SwapMutator<EnumGene<Integer>, Integer>(0.2))
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return crossover method (as an object, e.g. SwapMutator<EnumGene<Integer>, Integer>(0.2))
	 */
	private static Mutator<EnumGene<Integer>, Integer> getMutator(JeneticsConfig jenConfig) {
		if (jenConfig.getAlterer() != null && jenConfig.getAlterer().length > 0) {
			Alterer alterer0 = jenConfig.getAlterer()[0];
			Alterer alterer1 = null;
			if (jenConfig.getAlterer().length == 2) {
				alterer1 = jenConfig.getAlterer()[1];
			} else if (jenConfig.getAlterer().length > 2) {
				logger.info("More than 2 Alterers detected. Only the first two will be used.");
			}
			
			// detect if first or second alterer is crossover (or both)
			boolean crossover0 = false;
			boolean crossover1 = false;
			if (alterer0 != null && alterer0.equals(Alterer.SWAPMUTATOR)) {
				crossover0 = true;
			}
			if (alterer1 != null && alterer1.equals(Alterer.SWAPMUTATOR)) {
				crossover1 = true;
			}
			
			// if both fields contain crossover or the first field contains crossovers
			if (crossover0) {
				if (crossover0 && crossover1) {
				logger.info("Only the first mutator will be used, the second mutator will be ignored.");
				}
				// get alterer attribute (if existing)
				
				// for SwapMutator and similiar crossover (1 parameter required)
				if (alterer0.equals(Alterer.SWAPMUTATOR)) {
					if (jenConfig.getAltererAttributes() != null && jenConfig.getAltererAttributes().length > 0 &&
							jenConfig.getAltererAttributes()[0].length > 0) {
						double parameter = jenConfig.getAltererAttributes()[0][0];
						logger.info("Mutator: SwapMutator will be used with probability of " + parameter + ".");
						return new SwapMutator<EnumGene<Integer>, Integer>(parameter);
					}
				}
				// possible other crossover (with (0 or 1) or (0 or 1 or 2) parameters required)
				
			// if second field contains crossover
			} else if (crossover1) {
				if (alterer1.equals(Alterer.SWAPMUTATOR)) {
					if (jenConfig.getAltererAttributes() != null && jenConfig.getAltererAttributes().length > 1 &&
							jenConfig.getAltererAttributes()[1].length > 0) {
						double parameter = jenConfig.getAltererAttributes()[1][0];
						logger.info("Mutator: SwapMutator will be used with probability of " + parameter + ".");
						return new SwapMutator<EnumGene<Integer>, Integer>(parameter);
					}
				}
			} else {
				logger.info("No mutator detected. Default value will be used.");
			}
		}
		return new SwapMutator<EnumGene<Integer>, Integer>(0.2);
	}
	
	/**
	 * Gets the survivor selector according to jenConfig. If no selector is deteced
	 * it returns the default value (TournamentSelector<EnumGene<Integer>, Integer>())
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return selector (as an object, e.g. TournamentSelector<EnumGene<Integer>, Integer>())
	 */
	private static Selector<EnumGene<Integer>, Integer> getSurvivorSelector(JeneticsConfig jenConfig) {
		if (jenConfig == null || jenConfig.getSurvivorSelector() == null) {
			logger.info("TournamentSelector will be used as the survivor selector.");
			return new TournamentSelector<EnumGene<Integer>, Integer>();
		}
		logger.info("The survivor selector, which will be used is:");
		return getSelector(jenConfig.getSurvivorSelector(), jenConfig.getSurvivorSelectorAttributes());
	}

	/**
	 * Gets the offspring selector according to jenConfig. If no selector is detected
	 * it returns the default value (TournamentSelector<EnumGene<Integer>, Integer>())
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return selector (as an object, e.g. TournamentSelector<EnumGene<Integer>, Integer>())
	 */
	private static Selector<EnumGene<Integer>, Integer> getOffspringSelector(JeneticsConfig jenConfig) {
		if (jenConfig == null || jenConfig.getOffspringSelector() == null) {
			logger.info("TournamentSelector will be used as the offspring selector.");
			return new TournamentSelector<EnumGene<Integer>, Integer>();
		}
		logger.info("The survivor selector, which will be used is:");
		return getSelector(jenConfig.getOffspringSelector(), jenConfig.getOffspringSelectorAttributes());
	}

	/**
	 * Gets the selector according to given parameters. If no selector is detected
	 * then TournamentSelector is returned.
	 * 
	 * @param selector chosen selector
	 * @param selectorAttributes chosen attributes of the selector (not always used; not required)
	 * @return selector (as an object, e.g. TournamentSelector<EnumGene<Integer>, Integer>())
	 */
	private static Selector<EnumGene<Integer>, Integer> getSelector(
			at.jku.dke.slotmachine.optimizer.service.dto.JeneticConfigDTO.Selector selector,
			double[] selectorAttributes) {
		// if no selector can be detected, should usually not happen
		if (selector == null) {
			logger.info("TournamentSelector (no selector chosen)");
			return new TournamentSelector<EnumGene<Integer>, Integer>();
		}
		// parameter of survivorSelectorAttribute[0]
		double parameter0 = 0.0;
		if (selectorAttributes != null && selectorAttributes.length > 0) {
			parameter0 = selectorAttributes[0];
		}
		// parameter of survivorSelectorAttribute[1]; usually not used by selectors
		// and currently not used
		/*double parameter1 = 0.0;
		if (selectorAttributes != null && selectorAttributes.length > 1) {
			parameter1 = selectorAttributes[1];
		}*/
		switch (selector) {
			case BOLTZMANNSELECTOR: 
				if (selectorAttributes == null || selectorAttributes.length == 0) {
					logger.info("BoltzmannSelector");
					return new BoltzmannSelector<EnumGene<Integer>, Integer>();
				}
				logger.info("BoltzmannSelector (b = " + parameter0 + ")");
				return new BoltzmannSelector<EnumGene<Integer>, Integer>(parameter0);
			case EXPONENTIALRANKSELECTOR:
				if (selectorAttributes == null || selectorAttributes.length == 0) {
					logger.info("ExponentialRankSelector");
					return new ExponentialRankSelector<EnumGene<Integer>, Integer>();
				}
				logger.info("ExponentialRankSelector (c  = " + parameter0 + ")");
				return new ExponentialRankSelector<EnumGene<Integer>, Integer>(parameter0);
			case LINEARRANKSELECTOR:
				if (selectorAttributes == null || selectorAttributes.length == 0) {
					logger.info("LinearRankSelector");
					return new LinearRankSelector<EnumGene<Integer>, Integer>();
				}
				logger.info("LinearRankSelector (nminus = " + parameter0 + ")");
				return new LinearRankSelector<EnumGene<Integer>, Integer>(parameter0);
			case ROULETTEWHEELSELECTOR:
				logger.info("RouletteWheelSelector");
				return new RouletteWheelSelector<EnumGene<Integer>, Integer>();
			case STOCHASTICUNIVERSALSELECTOR:
				logger.info("StochasticUniversalSelector");
				return new StochasticUniversalSelector<EnumGene<Integer>, Integer>();
			case TOURNAMENTSELECTOR:
				if (selectorAttributes == null || selectorAttributes.length == 0) {
					logger.info("TournamentSelector");
					return new TournamentSelector<EnumGene<Integer>, Integer>();
				}
				logger.info("TournamentSelector (sampleSize = " + parameter0 + ")");
				return new TournamentSelector<EnumGene<Integer>, Integer>((int) parameter0);
			case TRUNCATIONSELECTOR:
				if (selectorAttributes == null || selectorAttributes.length == 0) {
					logger.info("TruncationSelector");
					return new TruncationSelector<EnumGene<Integer>, Integer>();
				}
				logger.info("TruncationSelector (n = " + parameter0 + ")");
				return new TruncationSelector<EnumGene<Integer>, Integer>((int) parameter0);
			default:
				logger.info("TournamentSelector (no selector chosen)");
				return new TournamentSelector<EnumGene<Integer>, Integer>(); 
		}
	}

	/**
	 * Gets the population size according to jenConfig. If jenConfig == null, then
	 * it returns 50 (default value). If the population size is less than 1, then
	 * it returns 50 (default value) as well.
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return population size
	 */
	private static int getPopulationSize(JeneticsConfig jenConfig) {
		if (jenConfig == null || jenConfig.getPopulationSize() < 1) {
			return 50;
		}
		return jenConfig.getPopulationSize();
	}
	
	/**
	 * Gets the maximal phenotype age according to jenConfig. If jenConfig == null, then
	 * it returns 70 (default value). If the maximal phenotype age is less than 1, then
	 * it returns 70 (default value) as well.
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return maximal phenotype age
	 */
	private static int getMaximalPhenotypeAge(JeneticsConfig jenConfig) {
		if (jenConfig == null || jenConfig.getMaximalPhenotypeAge() < 1) {
			return 70;
		}
		return jenConfig.getMaximalPhenotypeAge();
	}
	
	/**
	 * Gets the offspring fraction according to jenConfig. If jenConfig == null, then
	 * it returns 0.6 (default value). If the offspring fraction is not within the 
	 * range of 0 and 1 (including 0 and 1), then it returns 0.6 (default value) as well.
	 * 
	 * @param jenConfig configuration of Jenetics
	 * @return offspring fraction
	 */
	private static double getOffspringFraction(JeneticsConfig jenConfig) {
		if (jenConfig == null || jenConfig.getOffspringFraction() < 0 || 
				jenConfig.getOffspringFraction() > 1) {
			return 0.6;
		}
		return jenConfig.getOffspringFraction();
	}

	/**
	 * Gets the limits (termination methods) according to jenConfig.
	 * 
	 * @param jenConfig
	 * @return
	 */
	private static List<Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>> getLimits(JeneticsConfig jenConfig) {
		if (jenConfig == null || jenConfig.getTermination() == null) {
			logger.info("No termination methods detected.");
			return null;
		}
		List<Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>> terminationList = new LinkedList<Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>>>();
		switch (jenConfig.getTermination().length) {
			case 0:
				logger.info("No chosen termination methods detected.");
				return null;
			case 1:
				if (jenConfig.getTerminationAttributes() == null || jenConfig.getTerminationAttributes().length < 1) {
					logger.info("No attributes detected for the termination method.");
					terminationList.add(getLimit(jenConfig.getTermination()[0], new double[0]));
				} else {
					terminationList.add(getLimit(jenConfig.getTermination()[0], jenConfig.getTerminationAttributes()[0]));
				}
				logger.info("Termination method " + terminationList.get(0).toString() + " will be used.");
				return terminationList;
			case 2:
				if (jenConfig.getTerminationAttributes() == null || jenConfig.getTerminationAttributes().length < 1) {
					logger.info("No attributes detected for the termination method.");
					terminationList.add(getLimit(jenConfig.getTermination()[0], new double[0]));
					terminationList.add(getLimit(jenConfig.getTermination()[1], new double[0]));
				} else {
					terminationList.add(getLimit(jenConfig.getTermination()[0], jenConfig.getTerminationAttributes()[0]));
					
					if (jenConfig.getTerminationAttributes().length < 2) {
						terminationList.add(getLimit(jenConfig.getTermination()[1], new double[0]));
					} else {
						terminationList.add(getLimit(jenConfig.getTermination()[1], jenConfig.getTerminationAttributes()[1]));
					}
				}
				logger.info("Termination methods " + terminationList.get(0).toString() + " and " 
						+ terminationList.get(1) + " will be used.");
				return terminationList;
			case 3:
				if (jenConfig.getTerminationAttributes() == null || jenConfig.getTerminationAttributes().length < 1) {
					logger.info("No attributes detected for the termination method.");
					terminationList.add(getLimit(jenConfig.getTermination()[0], new double[0]));
					terminationList.add(getLimit(jenConfig.getTermination()[1], new double[0]));
					terminationList.add(getLimit(jenConfig.getTermination()[2], new double[0]));
				} else {
					terminationList.add(getLimit(jenConfig.getTermination()[0], jenConfig.getTerminationAttributes()[0]));
					
					if (jenConfig.getTerminationAttributes().length < 2) {
						terminationList.add(getLimit(jenConfig.getTermination()[1], new double[0]));
						terminationList.add(getLimit(jenConfig.getTermination()[2], new double[0]));
					} else {
						terminationList.add(getLimit(jenConfig.getTermination()[1], jenConfig.getTerminationAttributes()[1]));
						
						if (jenConfig.getTerminationAttributes().length < 3) {
							terminationList.add(getLimit(jenConfig.getTermination()[2], new double[0]));
						} else {
							terminationList.add(getLimit(jenConfig.getTermination()[2], jenConfig.getTerminationAttributes()[2]));
						}
					}
				}
				logger.info("Termination methods " + terminationList.get(0).toString() + " and " 
						+ terminationList.get(1) + " and " + terminationList.get(2) + " will be used.");
				return terminationList;
			default:
				logger.info("No chosen termination methods detected or too many methods detected.");
				return null;
		}
	}

	/**
	 * Returns the termination method of the given parameters.
	 * 
	 * @param termination chosen termination method
	 * @param attributes array of attributes
	 * @return termination method
	 */
	private static Predicate<? super EvolutionResult<EnumGene<Integer>, Integer>> getLimit(Termination termination,
			double[] attributes) {
		
		switch (termination) {
			case WORSTFITNESS:
				if (attributes != null && attributes.length > 0) {
					return pred -> pred.worstFitness() < attributes[0];
				}
				return pred -> pred.worstFitness() < -99999;
			case BYFITNESSTHRESHOLD:
				if (attributes != null && attributes.length > 0) {
					return Limits.byFitnessThreshold((int) attributes[0]);
				}
				return Limits.byFitnessThreshold(1000);
			case BYSTEADYFITNESS:
				if (attributes != null && attributes.length > 0 && attributes[0] >= 1) {
					return Limits.bySteadyFitness((int) attributes[0]);
				}
				return Limits.bySteadyFitness(100);
			case BYFIXEDGENERATION:
				if (attributes != null && attributes.length > 0 && attributes[0] >= 0) {
					return Limits.byFixedGeneration((int) attributes[0]);
				}
				return Limits.byFixedGeneration(1000);
			case BYEXECUTIONTIME:
				if (attributes != null && attributes.length > 0) {
					return Limits.byExecutionTime(Duration.ofSeconds((int) attributes[0]));
				}
				return Limits.byExecutionTime(Duration.ofSeconds(10));
			case BYPOPULATIONCONVERGENCE:
				if (attributes != null && attributes.length > 0 && attributes[0] <= 1 && attributes[0] >= 0) {
					return Limits.byPopulationConvergence(attributes[0]);
				}
				return Limits.byPopulationConvergence(0.5);
			default:
				return Limits.byExecutionTime(Duration.ofSeconds(10));
		}
	}
}
