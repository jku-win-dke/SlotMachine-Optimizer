package at.jku.dke.slotmachine.optimizer.optimization.hungarian;

import at.jku.dke.slotmachine.optimizer.domain.Flight;
import at.jku.dke.slotmachine.optimizer.domain.Slot;
import at.jku.dke.slotmachine.optimizer.optimization.InvalidOptimizationParameterTypeException;
import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationConfiguration;
import at.jku.dke.slotmachine.optimizer.optimization.OptimizationStatistics;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HungarianOptimization extends Optimization {
	private static final Logger logger = LogManager.getLogger();
	
    public HungarianOptimization(Flight[] flights, Slot[] slots) {
        super(flights, slots);
    }

    @Override
    public Map<Flight, Slot> run() {
    	logger.info("Running optimization using Hungarian Algorithm ...");
    	Flight[] flights = this.getFlights();
    	Slot[] slots = this.getSlots();
    	logger.debug("Optimization flights: " + flights.length + " | slots: " + slots.length);
    	
    	//create cost matrix
    	// slots are with index i (so some slots can be unassigned)
    	// flights are with index j
    	//  -> at [i][j] is the weight to assign flight j to slot i
    	double[][] costmatrix = new double[slots.length][flights.length];
    	for (int i = 0; i < slots.length; i++) {
    		for (int j = 0; j < flights.length; j++) {
    			flights[j].computeWeightMap(slots);
    			costmatrix[i][j] = flights[j].getWeight(slots[i]);
    		}
    	}
    	
    	//logger.debug - print costmatrix
    	logger.debug("costmatrix[" + costmatrix.length + "][" + costmatrix[0].length + "]");
    	if (logger.isDebugEnabled()) {
	    	String out = "";
	    	for (int k = 0; k < costmatrix.length; k++) {
	    		for (int l = 0; l < costmatrix[k].length; l++) {
	    			out = out + ("[" + costmatrix[k][l] + "]");
	    		}
	    		out = out + "\n";
	    	}
	    	logger.debug(out);
    	}
    	
    	//hungarian algorithm cannot work with negative values
    	// according to https://math.stackexchange.com/q/2036640 & https://en.wikipedia.org/wiki/Hungarian_algorithm -> No
    	//TODO can this hungarian algorithm implementation work with negative numbers or is it better to adjust cost matrix?
    	
    	//adjusting costmatrix
    		// find minimum value
    	double minValue = Double.MAX_VALUE;
    	for (int i = 0; i < costmatrix.length; i++) {
    		for (int j = 0; j < costmatrix[i].length; j++) {
    			if (minValue > costmatrix[i][j]) {
    				minValue = costmatrix[i][j];
    			}
    		}
    	}
    	
    	for (int i = 0; i < costmatrix.length; i++) {
    		for (int j = 0; j < costmatrix[i].length; j++) {
    			if (minValue < costmatrix[i][j]) {
    				costmatrix[i][j] = minValue + costmatrix[i][j];
    			}
    		}
    	}
    	
    	//TODO adjust costmatrix or adjust hungarian algorithm implementation? 
    	// (as usually, hungarian algorithm tries to minimize cost, but here the goal is to maximize the weights)
    	// https://stackoverflow.com/a/17520780
    	
    	//adjusting costmatrix
    		// find maximum value
    	double maxValue = Double.MIN_VALUE;
    	for (int i = 0; i < costmatrix.length; i++) {
    		for (int j = 0; j < costmatrix[i].length; j++) {
    			if (maxValue < costmatrix[i][j]) {
    				maxValue = costmatrix[i][j];
    			}
    		}
    	}
    	
    	for (int i = 0; i < costmatrix.length; i++) {
    		for (int j = 0; j < costmatrix[i].length; j++) {
    			costmatrix[i][j] = maxValue - costmatrix[i][j];
    		}
    	}
    	
    	// use hungarian algorithm
    	HungarianAlgorithm ha = new HungarianAlgorithm(costmatrix);
    	// result[3] = 51: flights[51] at slots[3]
    	int[] result = ha.execute();
    	
    	if (logger.isDebugEnabled()) {
    		String out = "";
    		for (int i = 0; i < result.length; i++) {
    			out = out + "[" + result[i] + "]";
    		}
    		logger.debug(out);
    	}

    	// TODO move sumOfWeights to statistics perhaps?	
    	double sumOfWeights = 0;
    	Map<Flight, Slot> resultMap = new HashMap<Flight, Slot>();
    	for (int i = 0; i < result.length; i++) {
    		resultMap.put(flights[result[i]], slots[i]);
    		logger.debug("Slot " + slots[i].getTime().toString() + ": " + flights[result[i]].getFlightId() 
    				+ " | weight: " + flights[result[i]].getWeight(slots[i]));
        	if (logger.isDebugEnabled()) {
        		sumOfWeights = sumOfWeights + flights[result[i]].getWeight(slots[i]);
        	}
    	}
    	logger.debug("Finished optimization for " + this.getOptId() + " has a fitness value of " + sumOfWeights);
    	
        return resultMap;
    }

    // no configuration used
    @Override
    public HungarianOptimizationConfiguration getDefaultConfiguration() {
        return null;
    }

    // no configuration used
    @Override
    public HungarianOptimizationConfiguration getConfiguration() {
        return null;
    }

    // no configuration used
    @Override
    public void newConfiguration(Map<String, Object> parameters) throws InvalidOptimizationParameterTypeException {
    	return;
    }

    @Override
    public HungarianOptimizationStatistics getStatistics() {
        return null;
    }
}
