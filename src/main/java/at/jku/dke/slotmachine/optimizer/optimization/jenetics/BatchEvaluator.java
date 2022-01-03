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
import java.util.stream.Stream;

public class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

	private final JeneticsOptimization optimization; // used to register new solutions
    private final SlotAllocationProblem problem;

    /**
     *
     * @param problem the slot allocation problem
     * @param optimization the Jenetics optimization run
     */
    public BatchEvaluator(SlotAllocationProblem problem, JeneticsOptimization optimization) {
		this.problem = problem;
		this.optimization = optimization;
    }

    /**
     * Takes a population of (unevaluated) candidate solutions and returns a sequence of evaluated solutions.
     * @param population the population of candidate solutions
     * @return an evaluated population
     */
    @Override
    public ISeq<Phenotype<EnumGene<Integer>, Integer>> eval(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        logger.debug("Starting population evaluation ...");

        final List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;

        double maxFitness;
        double minFitness;

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
        }

        logger.debug("Actual maximum fitness of the population: " + maxFitness);

        minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness) * 0.0001);
        logger.debug("Estimated minimum fitness of the population: " + minFitness);

        Stream<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulationStream = null;

        if(this.optimization.getFitnessEstimator() != null) {
            logger.debug("Getting estimated fitness value from estimator: " + this.optimization.getFitnessEstimator().getClass());
            double[] estimatedFitnessValues =
                    this.optimization.getFitnessEstimator().estimateFitnessDistribution(population.size(), maxFitness, minFitness);

            logger.debug("Assign each solution in the population an estimated fitness value.");
            estimatedPopulationStream = evaluatedPopulation.stream()
                    .map(phenotype -> phenotype.withFitness((int) estimatedFitnessValues[evaluatedPopulation.indexOf(phenotype)]));

            try {
                estimatedPopulation = estimatedPopulationStream.toList();
            } catch(Exception e) {
                logger.error(e);
            }

            logger.debug("Assigned estimated fitness values.");
        } else {
            logger.debug("No estimator specified. Using exact fitness (if available).");

            if(this.optimization.getMode() == OptimizationMode.NON_PRIVACY_PRESERVING){
                logger.debug("Running in non-privacy-preserving mode. Exact fitness values available.");
                estimatedPopulation = evaluatedPopulation;
            }
        }

        if(maxFitness >= this.optimization.getMaximumFitness() && estimatedPopulationStream != null) {
            logger.debug("Best fitness of current generation better than current best fitness. Adding intermediate result to the optimization run");
            this.optimization.setResults(estimatedPopulation.stream().distinct().map(phenotype -> this.problem.decode(phenotype.genotype())).toList());

            // set the optimization's maximum fitness to this generation's maximum fitness
            this.optimization.setMaximumFitness(maxFitness);
        }

        logger.debug("Finished evaluation");

        logger.debug("Update statistics.");
        this.optimization.getStatistics().setFitnessFunctionInvocations(problem.getFitnessFunctionApplications());
        this.optimization.getStatistics().setResultFitness(this.optimization.getMaximumFitness());

        return ISeq.of(estimatedPopulation);
    }

    /**
     * Convert the population from the Jenetics native representation to the array format required by the
     * Privacy Engine.
     * @param population the population in Jenetics representation
     * @return the population in array format required by Privacy Engine
     */
    private Integer[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        return population.stream()
                  .map(phenotype -> this.problem.decode(phenotype.genotype()))
                  .map(map ->
                       // 1. Get a flight list from the mapping of flights to slots, where the flights are
                       // ordered by their assigned time slot.
                       // 2. Replace the flights by their position in the problem's sequence of flights.
                       map.entrySet().stream()
                          .sorted(Map.Entry.comparingByValue())
                          .map(Map.Entry::getKey)
                          .map(flight -> this.problem.getFlights().indexOf(flight))
                          .toArray(Integer[]::new)
                  ).toArray(Integer[][]::new);
    }

    /**
     * Returns the generation of the given population, or -1 in case of error. If different individuals have
     * different generation numbers, it returns the maximum (youngest) generation.
     * @param population the population of individuals
     * @return the current generation, or -1 in case of error
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
