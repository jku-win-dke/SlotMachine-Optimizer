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

public class BatchEvaluatorBackup implements Evaluator<EnumGene<Integer>, Integer> {
    private static final Logger logger = LogManager.getLogger();

	private JeneticsOptimization optimization; // used to register new solutions
    private SlotAllocationProblem problem;

    public BatchEvaluatorBackup(SlotAllocationProblem problem, JeneticsOptimization optimization) {
		this.problem = problem;
		this.optimization = optimization;
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
        double maxFitness = evaluatedPopulation.get(population.size()-1).fitness();
        double minFitness = maxFitness - (2 * Math.abs(maxFitness)) - (Math.abs(maxFitness)*0.0001);
        logger.debug("generation: " + getGeneration(population) + " | minFitness: " + minFitness + " | maxFitness: " + maxFitness);
        //double difference = (maxFitness - minFitness); //for sigmoid function
        
        //function (f(i) = a * ln(b * i) //logarithmic function
        //	a = (minFitness - maxFitness)/(ln(1/populationSize))
        //	b = e^c
        //	c = (0 - (minFitness * ln(populationSize))/(minFitness - maxFitness)
        double a = minFitness - maxFitness;
        a = a / (Math.log((double) (1 / (double) population.size()))); // Math.log = ln
        double c = 0 - (minFitness * Math.log(population.size()));
        c = c / (minFitness - maxFitness);
        double b = Math.pow(Math.E, c); //e^c
        
        //logger.debug("f(i) = a * ln(b * i) / b = e^c / a= " + a + " | b= " + b + " | c= " + c);
        
        List<Double> actualfitnessValuesGen = new LinkedList<Double>();
        List<Double> sigmoidfitnessValuesGen = new LinkedList<Double>();
        
        for(Phenotype<EnumGene<Integer>, Integer> phenotype : evaluatedPopulation) {
            // function f(i) = ((1/0,948683298) * (x/sqrt(1+x^2)) * (difference/2)) + minFitness + (difference/2)  (sigmoid function, commented)
        	//CURRENTLY logarithmic function is used, see above
        	// function f(i) is used to give every individual a value between maximum fitness and minimum fitness, 
        	//   according to their position in the sorted list (f(i) is modeled similar to sigmoid functions)
            int i = evaluatedPopulation.indexOf(phenotype);
            
            if ((getGeneration(population) == 50 || getGeneration(population) == 150 || getGeneration(population) == 200 || 
            		getGeneration(population) == 2 ) && logger.isDebugEnabled()) {
            	actualfitnessValuesGen.add((double) phenotype.fitness());
            }
            
            // for sigmoid function
            //double x = ((i+1.0) * (6.0/(double) evaluatedPopulation.size())) - 3.0;
            //
            //double fitness = ((1.0/0.948683298) * (x/Math.sqrt(1 + x*x)) * (difference/2.0)) + minFitness + (difference/2.0);
            double fitness = a * Math.log(b * i);
            
            Phenotype<EnumGene<Integer>, Integer> evaluatedPhenotype =
                    phenotype.withFitness((int) fitness);
            // for sigmoid function, debugging information
            /*if (getGeneration(population) == 1 && i % 20 == 0 && logger.isDebugEnabled()) {
	            logger.debug("generation: " + getGeneration(population) + " | minFitness: " + minFitness + " | maxFitness: " + maxFitness +
	            		" | index: " + i + " | x: " + x + " | popSize: " + evaluatedPopulation.size() + " | 'new fitness': " + fitness);
            }*/

            if ((getGeneration(population) == 50 || getGeneration(population) == 150 || getGeneration(population) == 200 || 
            		getGeneration(population) == 2 ) && logger.isDebugEnabled()) {
            	sigmoidfitnessValuesGen.add((double) evaluatedPhenotype.fitness());
            }
            
            relativeFitnessPopulation.add(evaluatedPhenotype);
        }

        // information for info/debug to create diagrams with distribution of fitness in complete population
        if (getGeneration(population) == 50 && logger.isDebugEnabled()) {
        	String genFitness = "generation 50, actual fitness values: ";
        	for (Double actualFitness : actualfitnessValuesGen) {
        		genFitness = genFitness + actualFitness + ", ";
        	}
        	String gensigmoidFitness = "generation 50, logarithmic fitness values: ";
        	for (Double actualFitness : sigmoidfitnessValuesGen) {
        		gensigmoidFitness = gensigmoidFitness + actualFitness + ", ";
        	}
        	logger.debug(genFitness);
        	logger.debug(gensigmoidFitness);
        } else if (getGeneration(population) == 150 && logger.isDebugEnabled()) {
        	String genFitness = "generation 150, actual fitness values: ";
        	for (Double actualFitness : actualfitnessValuesGen) {
        		genFitness = genFitness + actualFitness + ", ";
        	}
        	String gensigmoidFitness = "generation 150, logarithmic fitness values: ";
        	for (Double actualFitness : sigmoidfitnessValuesGen) {
        		gensigmoidFitness = gensigmoidFitness + actualFitness + ", ";
        	}
        	logger.debug(genFitness);
        	logger.debug(gensigmoidFitness);
        } else if (getGeneration(population) == 200 && logger.isDebugEnabled()) {
        	String genFitness = "generation 200, actual fitness values: ";
        	for (Double actualFitness : actualfitnessValuesGen) {
        		genFitness = genFitness + actualFitness + ", ";
        	}
        	String gensigmoidFitness = "generation 200, logarithmic fitness values: ";
        	for (Double actualFitness : sigmoidfitnessValuesGen) {
        		gensigmoidFitness = gensigmoidFitness + actualFitness + ", ";
        	}
        	logger.debug(genFitness);
        	logger.debug(gensigmoidFitness);
        } else if (getGeneration(population) == 2 && logger.isDebugEnabled()) {
        	String genFitness = "generation 2, actual fitness values: ";
        	for (Double actualFitness : actualfitnessValuesGen) {
        		genFitness = genFitness + actualFitness + ", ";
        	}
        	String gensigmoidFitness = "generation 2, logarithmic fitness values: ";
        	for (Double actualFitness : sigmoidfitnessValuesGen) {
        		gensigmoidFitness = gensigmoidFitness + actualFitness + ", ";
        	}
        	logger.debug(genFitness);
        	logger.debug(gensigmoidFitness);
        }
        
        logger.debug("Population evaluated.");

        return ISeq.of(relativeFitnessPopulation);
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
