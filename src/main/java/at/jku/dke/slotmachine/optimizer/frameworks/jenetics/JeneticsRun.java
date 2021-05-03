package at.jku.dke.slotmachine.optimizer.frameworks.jenetics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.JeneticsConfig;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;
import at.jku.dke.slotmachine.optimizer.service.dto.JeneticConfigDTO.Alterer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jenetics.Crossover;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.Limits;
import io.jenetics.engine.RetryConstraint;
import io.jenetics.util.ISeq;

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
				.populationSize(100)
				.alterers(
						getMutator(jenConfig),
						getCrossover(jenConfig))
				.constraint(constraint)
				.build();
		logger.info("Alterer used by the engine: " + e.alterer().toString());
		logger.debug("Constraints used by the engine: " + e.constraint().toString());
		logger.debug("Problem used by the engine " + e.toString() + ": " + p.toString());
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
}
