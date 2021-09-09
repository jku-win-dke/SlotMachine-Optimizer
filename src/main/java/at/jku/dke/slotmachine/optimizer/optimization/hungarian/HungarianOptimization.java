package at.jku.dke.slotmachine.optimizer.optimization.hungarian;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HungarianOptimization extends Optimization {
	private static final Logger logger = LogManager.getLogger();

	private HungarianOptimizationStatistics statistics;
	
    public HungarianOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);

        this.statistics = new HungarianOptimizationStatistics();
    }

    @Override
    public Map<Flight, Slot> run() {
    	logger.info("Running optimization using Hungarian Algorithm ...");
    	Flight[] flights = this.getFlights();
    	Slot[] slots = this.getSlots();
    	logger.debug("Optimization flights: " + flights.length + " | slots: " + slots.length);
    	
    	// create cost matrix
    	// slots are with index i (so some slots can be unassigned)
    	// flights are with index j
    	//  -> at [i][j] is the weight to assign flight j to slot i
    	double[][] costMatrix = new double[slots.length][flights.length];
    	for (int i = 0; i < slots.length; i++) {
    		for (int j = 0; j < flights.length; j++) {
    			flights[j].computeWeightMap(slots);
    			costMatrix[i][j] = flights[j].getWeight(slots[i]);
    		}
    	}
    	
    	//logger.debug - print costmatrix
    	logger.debug("costMatrix[" + costMatrix.length + "][" + costMatrix[0].length + "]");
    	if (logger.isDebugEnabled()) {
	    	String out = "";
	    	for (int k = 0; k < costMatrix.length; k++) {
	    		for (int l = 0; l < costMatrix[k].length; l++) {
	    			out = out + ("[" + costMatrix[k][l] + "]");
	    		}
	    		out = out + "\n";
	    	}
	    	logger.debug(out);
    	}
    	
    	// Hungarian algorithm cannot work with negative values
    	// See https://math.stackexchange.com/q/2036640 & https://en.wikipedia.org/wiki/Hungarian_algorithm
    	// TODO Can this implementation work with negative numbers or is it better to adjust cost matrix?
    	
    	// adjusting cost matrix
   		// find minimum value
    	double minValue = Double.MAX_VALUE;
    	for (int i = 0; i < costMatrix.length; i++) {
    		for (int j = 0; j < costMatrix[i].length; j++) {
    			if (minValue > costMatrix[i][j]) {
    				minValue = costMatrix[i][j];
    			}
    		}
    	}
    	
    	for (int i = 0; i < costMatrix.length; i++) {
    		for (int j = 0; j < costMatrix[i].length; j++) {
    			if (minValue < costMatrix[i][j]) {
    				costMatrix[i][j] = Math.abs(minValue) + costMatrix[i][j];
    			}
    		}
    	}
    	
    	// TODO Adjust cost matrix or adjust Hungarian algorithm implementation?
    	// (Hungarian algorithm tries to minimize cost, but here the goal is to maximize utility)
    	// https://stackoverflow.com/a/17520780
    	
    	// Adjusting cost matrix
		// find maximum value
    	double maxValue = Double.MIN_VALUE;
    	for (int i = 0; i < costMatrix.length; i++) {
    		for (int j = 0; j < costMatrix[i].length; j++) {
    			if (maxValue < costMatrix[i][j]) {
    				maxValue = costMatrix[i][j];
    			}
    		}
    	}
    	
    	for (int i = 0; i < costMatrix.length; i++) {
    		for (int j = 0; j < costMatrix[i].length; j++) {
    			costMatrix[i][j] = maxValue - costMatrix[i][j];
    		}
    	}
    	
    	// use Hungarian algorithm
    	HungarianAlgorithm ha = new HungarianAlgorithm(costMatrix);
    	// result[3] = 51: flights[51] at slots[3]
    	int[] result = ha.execute();
    	
    	if (logger.isDebugEnabled()) {
    		String out = "";
    		for (int i = 0; i < result.length; i++) {
    			out = out + "[" + result[i] + "]";
    		}
    		logger.debug(out);
    	}

    	int sumOfWeights = 0;
    	Map<Flight, Slot> resultMap = new HashMap<Flight, Slot>();
    	for (int i = 0; i < result.length; i++) {
    		resultMap.put(flights[result[i]], slots[i]);
    		logger.debug("Slot " + slots[i].getTime().toString() + ": " + flights[result[i]].getFlightId() 
    				+ " | weight: " + flights[result[i]].getWeight(slots[i]));

    		sumOfWeights = sumOfWeights + flights[result[i]].getWeight(slots[i]);
    	}
    	logger.info("Finished optimization using Hungarian algorithm for " + this.getOptId() + " with a fitness value of " + sumOfWeights);

    	if(this.getStatistics() == null) {
    		this.statistics = new HungarianOptimizationStatistics();
    	}

    	this.getStatistics().setSolutionFitness(sumOfWeights);

        return resultMap;
    }

    // no configuration used
    @Override
    public HungarianOptimizationConfiguration getDefaultConfiguration() {
        return new HungarianOptimizationConfiguration();
    }

    // no configuration used
    @Override
    public HungarianOptimizationConfiguration getConfiguration() {
        return new HungarianOptimizationConfiguration();
    }

    // no configuration used
    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
    	// do nothing
    }

    @Override
    public HungarianOptimizationStatistics getStatistics() {
    	return this.statistics;
    }
}
