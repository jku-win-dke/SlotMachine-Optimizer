package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

import at.jku.dke.slotmachine.optimizer.optimization.OptimizationMode;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;

import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

	private final JeneticsOptimization optimization; // used to register new solutions
    private final SlotAllocationProblem problem;

    public BatchEvaluator(SlotAllocationProblem problem, JeneticsOptimization optimization) {
		this.problem = problem;
		this.optimization = optimization;
    }

    @Override
    public ISeq<Phenotype<EnumGene<Integer>, Integer>> eval(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        logger.debug("Starting population evaluation ...");

        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        double maxFitness;
        double minFitness;

        if(this.optimization.getMode() == OptimizationMode.PRIVACY_PRESERVING) {
            logger.info("Running in privacy-preserving mode: Evaluate the population using the Privacy Engine.");

            logger.info("Convert population to format required by Privacy Engine.");
            int[][] input = this.convertPopulationToArray(population);

            logger.info("Invoke the Privacy Engine service to evaluate population.");
            PopulationOrderDTO populationOrder =
                    this.optimization.getPrivacyEngineService().computePopulationOrder(this.optimization, input);

            int[] order = populationOrder.getOrder();

            logger.info("Convert the population order received from the Privacy Engine to the format required by Jenetics.");
            evaluatedPopulation =
                population.stream()
                          .sorted(Comparator.comparingInt(phenotype -> order[population.indexOf(phenotype)]))
                          .toList();

            maxFitness = populationOrder.getMaximum();
        } else {
            logger.info("Running in non-privacy-preserving mode: Evaluate the population using the submitted weights.");
            evaluatedPopulation =
                population.stream()
                          .map(phenotype -> phenotype.withFitness(problem.fitness(phenotype.genotype())))
                          .sorted(Comparator.comparingInt(Phenotype::fitness))
                          .sorted(Comparator.reverseOrder())
                          .toList();

            maxFitness = evaluatedPopulation.get(0).fitness();
        }

        logger.info("Actual maximum fitness of the population: " + maxFitness);

        minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness) * 0.0001);
        logger.info("Estimated minimum fitness of the population: " + minFitness);

        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;

        if(this.optimization.getFitnessEstimator() != null) {
            logger.info("Getting estimated fitness value from estimator: " + this.optimization.getFitnessEstimator().getClass());
            double[] estimatedFitnessValues =
                    this.optimization.getFitnessEstimator().estimateFitnessDistribution(population.size(), maxFitness, minFitness);

            logger.info("Assign each solution in the population an estimated fitness value.");
            estimatedPopulation =
                    evaluatedPopulation.stream()
                            .map(phenotype -> phenotype.withFitness((int) estimatedFitnessValues[evaluatedPopulation.indexOf(phenotype)]))
                            .toList();
        } else {
            logger.info("No estimator specified. Using exact fitness (if available).");

            if(this.optimization.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING){
                logger.info("Running in non-privacy-preserving mode. Exact fitness values available.");
                estimatedPopulation = evaluatedPopulation;
            }
        }

        return ISeq.of(estimatedPopulation);
    }

    private int[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
         int[][] populationArray =
            population.stream()
                      .map(phenotype -> this.problem.decode(phenotype.genotype()))
                      .map(map -> {
                               // 1. Get a flight list from the mapping of flights to slots, where the flights are
                               // ordered by their assigned time slot.
                               // 2. Replace the flights by their position in the problem's sequence of flights.
                               Integer[] solutionArray =
                                   map.entrySet().stream()
                                      .sorted(Map.Entry.comparingByValue())
                                      .map(Map.Entry::getKey)
                                      .map(flight -> this.problem.getFlights().indexOf(flight))
                                      .toArray(Integer[]::new);

                               return solutionArray;
                           }).toArray(int[][]::new);

        return populationArray;
    }

    /**
     * Returns the current generation of the given population, or -1 in case of error.
     */
    private static int getGeneration(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
    	int currentGen = -1;

    	for (Phenotype<EnumGene<Integer>, Integer> phenotype: population) {
    		if (phenotype.generation() > currentGen) {
    			currentGen = (int) phenotype.generation();
    		}
    	}
    	return currentGen;
    }
}
