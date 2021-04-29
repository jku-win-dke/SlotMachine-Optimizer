package at.jku.dke.slotmachine.optimizer.frameworks.jenetics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jenetics.EnumGene;
import io.jenetics.Genotype;
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
}
