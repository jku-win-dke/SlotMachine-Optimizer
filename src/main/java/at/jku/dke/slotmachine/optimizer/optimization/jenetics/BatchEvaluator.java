package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

    private SlotAllocationProblem problem;

    public BatchEvaluator(SlotAllocationProblem problem) {
        this.problem = problem;
    }

    @Override
    public ISeq<Phenotype<EnumGene<Integer>, Integer>> eval(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        logger.debug("Starting population evaluation ...");

        List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation =
                new LinkedList<>();

        for(Phenotype<EnumGene<Integer>, Integer> phenotype : population) {
            Phenotype<EnumGene<Integer>, Integer> evaluatedPhenotype =
                    phenotype.withFitness(problem.fitness(phenotype.genotype()));

            evaluatedPopulation.add(evaluatedPhenotype);
        }

        evaluatedPopulation.sort(Comparator.comparingInt(Phenotype::fitness));

        List<Phenotype<EnumGene<Integer>, Integer>> relativeFitnessPopulation =
                new LinkedList<>();

        for(Phenotype<EnumGene<Integer>, Integer> phenotype : evaluatedPopulation) {
            Phenotype<EnumGene<Integer>, Integer> evaluatedPhenotype =
                    phenotype.withFitness(evaluatedPopulation.indexOf(phenotype));

            relativeFitnessPopulation.add(evaluatedPhenotype);
        }

        logger.debug("Population evaluated.");

        return ISeq.of(relativeFitnessPopulation);
    }
}
