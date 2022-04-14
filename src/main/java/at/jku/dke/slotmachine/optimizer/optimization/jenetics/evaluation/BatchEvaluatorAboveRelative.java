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

public class BatchEvaluatorAboveRelative extends BatchEvaluator{
    private static final Logger logger = LogManager.getLogger();
    private final double relativeThreshold = 0.3;

    /**
     * @param problem      the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluatorAboveRelative(SlotAllocationProblem problem, JeneticsOptimization optimization) {
        super(problem, optimization);
    }

    @Override
    protected List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness) {
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;

        if(this.optimization.getFitnessEstimator() != null) {
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
        List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        double maxFitness;

        if(this.optimization.getMode() == OptimizationMode.PRIVACY_PRESERVING) {
            logger.debug("Running in privacy-preserving mode: Evaluate the population using the Privacy Engine.");

            logger.debug("Convert population to format required by Privacy Engine.");
            Integer[][] input = this.convertPopulationToArray(population);

            logger.debug("Invoke the Privacy Engine service to evaluate population.");
            PopulationOrderDTO populationOrder =
                    this.optimization.getPrivacyEngineService().computePopulationOrder(this.optimization, input);

            int[] order = populationOrder.getOrder();

            logger.debug("Convert the population order received from the Privacy Engine to the format required by Jenetics.");
            evaluatedPopulation =
                    population.stream()
                            .sorted(Comparator.comparingInt(phenotype -> order[population.indexOf(phenotype)]))
                            .toList();

            maxFitness = populationOrder.getMaximum();
        } else {
            logger.debug("Running in non-privacy-preserving mode: Evaluate the population using the submitted weights.");
            evaluatedPopulation =
                    population.stream()
                            .map(phenotype -> phenotype.withFitness(problem.fitness(phenotype.genotype())))
                            .sorted(Comparator.comparingInt(Phenotype::fitness))
                            .sorted(Comparator.reverseOrder())
                            .toList();

            maxFitness = evaluatedPopulation.get(0).fitness();

            logger.debug("Actual minimum fitness of the population: " + evaluatedPopulation.get(evaluatedPopulation.size() - 1).fitness());

            double threshold = percentile(evaluatedPopulation.stream().map(ph -> (double) ph.fitness()).toList(), (1 - relativeThreshold) * 100);

            evaluatedPopulation = evaluatedPopulation.stream().filter(phenotype -> phenotype.fitness() >= threshold).toList();

            if(fitnessEvolutionStep != null) {
                fitnessEvolutionStep.setEvaluatedPopulation(
                        evaluatedPopulation.stream().map(phenotype -> (double) phenotype.fitness()).toArray(Double[]::new)
                );
                logger.debug("Tracing fitness evolution. Size of evaluated population: " + fitnessEvolutionStep.getEvaluatedPopulation().length);
            }
        }

        PopulationEvaluation evaluation = new PopulationEvaluation();
        evaluation.evaluatedPopulation = evaluatedPopulation;
        evaluation.maxFitness = maxFitness;
        return evaluation;
    }
}
