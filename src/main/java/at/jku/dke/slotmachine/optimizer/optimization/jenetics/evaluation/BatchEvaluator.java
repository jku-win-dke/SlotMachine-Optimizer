package at.jku.dke.slotmachine.optimizer.optimization.jenetics.evaluation;

import at.jku.dke.slotmachine.optimizer.optimization.FitnessEvolutionStep;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.SlotAllocationProblem;
import io.jenetics.EnumGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

public abstract class BatchEvaluator implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

	protected final JeneticsOptimization optimization; // used to register new solutions
    protected final SlotAllocationProblem problem;

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
        Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation = null;
        List<Phenotype<EnumGene<Integer>, Integer>> estimatedPopulation = null;

        double maxFitness;
        double minFitness;

        FitnessEvolutionStep fitnessEvolutionStep = null;

        logger.debug("Number of distinct solutions in population: " + population.stream().distinct().count());

        if(this.optimization.isTraceFitnessEvolution()) {
            fitnessEvolutionStep = new FitnessEvolutionStep();

            logger.debug("Adding fitness evolution to statistics");
            this.optimization.getStatistics().getFitnessEvolution().add(fitnessEvolutionStep);

            Optional<Long> generation = population.stream().map(Phenotype::generation).max(Long::compareTo);

            if(generation.isPresent()) {
                fitnessEvolutionStep.setGeneration(generation.get().intValue());

                logger.debug("Tracing fitness evolution. Generation: " + fitnessEvolutionStep.getGeneration());
            }
        }

        PopulationEvaluation evaluation = evaluatePopulation(population, fitnessEvolutionStep);
        evaluatedPopulation = evaluation.evaluatedPopulation;
        fitnessQuantilesPopulation = evaluation.fitnessQuantilesPopulation;
        maxFitness = evaluation.maxFitness;

        logger.debug("Actual maximum fitness of the population: " + maxFitness);

        minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness) * 0.0001);

        logger.debug("Estimated minimum fitness of the population: " + minFitness);

        estimatedPopulation = estimatePopulation(population, evaluatedPopulation, fitnessEvolutionStep, fitnessQuantilesPopulation, maxFitness, minFitness);

        if(fitnessEvolutionStep != null) {
            fitnessEvolutionStep.setEstimatedPopulation(
                    estimatedPopulation.stream()
                            .map(phenotype -> (double) phenotype.fitness())
                            .toArray(Double[]::new)
            );
            logger.debug("Tracing fitness evolution. Size of estimated population: " + fitnessEvolutionStep.getEstimatedPopulation().length);
        }

        if(maxFitness >= this.optimization.getMaximumFitness() && estimatedPopulation != null) {
            logger.debug("Best fitness of current generation better than current best fitness. Attaching intermediate result to the optimization run.");
            this.optimization.setResults(
                    estimatedPopulation.stream().distinct().map(phenotype -> this.problem.decode(phenotype.genotype())).toList()
            );

            // set the optimization's maximum fitness to this generation's maximum fitness
            this.optimization.setMaximumFitness(maxFitness);
        }

        logger.debug("Finished evaluation");

        logger.debug("Update statistics.");
        this.optimization.getStatistics().setFitnessFunctionInvocations(problem.getFitnessFunctionApplications());
        this.optimization.getStatistics().setResultFitness(this.optimization.getMaximumFitness());

        return ISeq.of(estimatedPopulation);
    }

    protected abstract List<Phenotype<EnumGene<Integer>, Integer>> estimatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation, FitnessEvolutionStep fitnessEvolutionStep, Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation, double maxFitness, double minFitness);

    protected abstract PopulationEvaluation evaluatePopulation(Seq<Phenotype<EnumGene<Integer>, Integer>> population, FitnessEvolutionStep fitnessEvolutionStep);

    /**
     * Convert the population from the Jenetics native representation to the array format required by the
     * Privacy Engine.
     * @param population the population in Jenetics representation
     * @return the population in array format required by Privacy Engine
     */
    protected Integer[][] convertPopulationToArray(Seq<Phenotype<EnumGene<Integer>, Integer>> population) {
        return population.stream()
                  .map(phenotype -> this.problem.decode(phenotype.genotype()))
                  .map(map ->
                       // 1. Get a flight list from the mapping of flights to slots, where the flights are
                       // ordered by their assigned time slot.
                       // 2. Replace the flights by their position in the problem's sequence of flights.
                       map.entrySet().stream()
                          .sorted(Entry.comparingByValue())
                          .map(Entry::getKey)
                          .map(flight -> this.problem.getFlights().indexOf(flight))
                          .toArray(Integer[]::new)
                  ).toArray(Integer[][]::new);
    }

    protected static double percentile(List<Double> values, double percentile) {
        values = new ArrayList<>(values);
        Collections.sort(values);
        int index = (int) Math.ceil((percentile / 100) * values.size());
        return values.get(index - 1);
    }

    static class PopulationEvaluation{
        protected List<Phenotype<EnumGene<Integer>, Integer>> evaluatedPopulation;
        protected Map<Phenotype<EnumGene<Integer>, Integer>, Integer> fitnessQuantilesPopulation;

        protected double maxFitness;
    }
}
