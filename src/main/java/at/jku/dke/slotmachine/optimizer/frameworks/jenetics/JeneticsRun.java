package at.jku.dke.slotmachine.optimizer.frameworks.jenetics;

import java.util.List;
import java.util.Map;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;

public class JeneticsRun extends Run {

	private static final Logger logger = LogManager.getLogger();
	
	public static Map<Flight, Slot> run(List<Flight> flights, List<Slot> slots) {
		logger.info("Start optimization using Jenetics framework");
		
		SlotAllocationProblem p = new SlotAllocationProblem(
				ISeq.of(flights), ISeq.of(slots));
		
		Engine<EnumGene<Integer>, Integer> e = Engine.builder(p).build();
		
        EvolutionStatistics <Integer, ?> statistics = EvolutionStatistics.ofNumber();

        Genotype<EnumGene<Integer>> result = e.stream().limit(Limits.bySteadyFitness(250)).peek(statistics).collect(EvolutionResult.toBestGenotype());

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
