package at.jku.dke.slotmachine.optimizer.optimization.jenetics;

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

        //double minFitness = evaluatedPopulation.get(0).fitness(); //without fitness
        double maxFitness = evaluatedPopulation.get(evaluatedPopulation.size()-1).fitness();
        double minFitness = maxFitness - (2 * Math.abs(maxFitness));
        logger.debug("generation: " + population.get(0).generation() + " | minFitness: " + minFitness + " | maxFitness: " + maxFitness);;
        double difference = (maxFitness - minFitness);
        
        for(Phenotype<EnumGene<Integer>, Integer> phenotype : evaluatedPopulation) {
            // function f(i) = ((1/0,948683298) * (x/sqrt(1+x^2)) * (difference/2)) + minFitness + (difference/2)   
        	// function f(i) is used to give every individual a value between maximum fitness and minimum fitness, 
        	//   according to their position in the sorted list (f(i) is modeled similar to sigmoid functions)
            int i = evaluatedPopulation.indexOf(phenotype);
            double x = ((i+1.0) * (6.0/(double) evaluatedPopulation.size())) - 3.0;
            
            double fitness = ((1.0/0.948683298) * (x/Math.sqrt(1 + x*x)) * (difference/2.0)) + minFitness + (difference/2.0);
            
            Phenotype<EnumGene<Integer>, Integer> evaluatedPhenotype =
                    phenotype.withFitness((int) fitness);
            if (population.get(0).generation() == 1 && i % 20 == 0 && logger.isDebugEnabled()) {
	            logger.debug("generation: " + population.get(0).generation() + " | minFitness: " + minFitness + " | maxFitness: " + maxFitness +
	            		" | index: " + i + " | x: " + x + " | popSize: " + evaluatedPopulation.size() + " | 'new fitness': " + fitness);
            }
            relativeFitnessPopulation.add(evaluatedPhenotype);
        }

        
        logger.debug("Population evaluated.");

        return ISeq.of(relativeFitnessPopulation);
    }
}
