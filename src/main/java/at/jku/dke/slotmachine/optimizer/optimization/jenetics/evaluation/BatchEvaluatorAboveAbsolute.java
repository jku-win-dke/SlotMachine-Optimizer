package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.FitnessMethod;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchEvaluatorAboveAbsolute extends BatchEvaluator{
    private static final Logger logger = LogManager.getLogger();
    double threshold = Integer.MIN_VALUE;

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveAbsolute(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;

        if(this.optimization.getFitnessEstimator() != null) {
            // per default, the estimated population size is the same as the population size
            List<Genotype<EnumGene<Integer>>> evaluatedGenotypes =
                    evaluatedPopulation.stream().map(phenotype -> phenotype.genotype()).toList();

            logger.debug("Assign each solution returned by the Privacy Engine the maximum fitness: " + maxFitness);
            estimatedPopulationStream = population.stream()
                    .map(phenotype ->
                            evaluatedGenotypes.contains(phenotype.genotype())?
                                    phenotype.withFitness((int) maxFitness) :
                                    phenotype.withFitness((int) minFitness)
                    ).collect(Collectors.toList());

            estimatedPopulation = estimatedPopulationStream.stream()
                    .sorted(Comparator.comparingInt(Phenotype::fitness))
                    .sorted(Comparator.reverseOrder())
                    .toList();

            logger.debug("Assigned estimated fitness values.");
        } else {
            logger.debug("No estimator specified. Using exact fitness (if available).");

            if(this.optimization.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING){
                logger.debug("Running in non-privacy-preserving mode. Exact fitness values available.");
                estimatedPopulation = evaluatedPopulation;
            }
        }
        return  estimatedPopulation;
    }

    @Override
    protected PopulationEvaluation evaluatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep) {
        PopulationEvaluation evaluation = evaluatePopulationOrder(population, fitnessEvolutionStep);
        threshold = evaluation.maxFitness * 0.7;
        evaluation.evaluatedPopulation = evaluation.evaluatedPopulation.stream().filter(phenotype -> phenotype.fitness() >= threshold).toList();
        return evaluation;
    }
}
