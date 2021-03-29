package at.jku.dke.slotmachine.optimizer.algorithms.jenetics;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.optimizer.service.dto.FlightDTO;
import at.jku.dke.slotmachine.optimizer.service.dto.SlotDTO;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;

public class JeneticsApplication {

	private static final Logger logger = LogManager.getLogger();
	
	public static Map<FlightDTO,SlotDTO> run(ISeq<FlightDTO> flights, ISeq<SlotDTO> slots) {
		logger.info("Uses jenetics algorithm");
		
		SlotAllocationProblem p = new SlotAllocationProblem(
				ISeq.of(flights), ISeq.of(slots));
		
		Engine<EnumGene<Integer>, Integer> e = Engine.builder(p).build();
		
        EvolutionStatistics <Integer, ?> statistics = EvolutionStatistics.ofNumber();

        Genotype<EnumGene<Integer>> result = e.stream().limit(Limits.bySteadyFitness(250)).peek(statistics).collect(EvolutionResult.toBestGenotype());

        logger.info("Statistics:\n" + statistics);

        logger.info("Result: " + result);

        Map<FlightDTO,SlotDTO> resultMap = p.decode(result);

        logger.info("Size of result: " + resultMap.keySet().size());
        logger.info("Result Map:\n");
        for(FlightDTO f : resultMap.keySet()) {
            logger.info(f.getFlightId() + " " + resultMap.get(f).getTime());
        }
        
        return resultMap;
	}
}
