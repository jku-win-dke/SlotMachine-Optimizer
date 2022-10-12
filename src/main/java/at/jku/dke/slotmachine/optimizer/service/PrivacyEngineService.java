package at.jku.dke.slotmachine.optimizer.service;

import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.optimizer.optimization.jenetics.JeneticsOptimization;
import at.jku.dke.slotmachine.privacyEngine.dto.AboveIndividualsDTO;
import at.jku.dke.slotmachine.privacyEngine.dto.FitnessQuantilesDTO;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PrivacyEngineService {
	private static final Logger logger = LogManager.getLogger();

	private final RestTemplate restTemplate;
	
	public PrivacyEngineService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

	/**
	 * Invokes the Privacy Engine's computePopulationOrder function, which ranks the input population of solutions to
	 * the flight prioritization problem. Note that flights are referred to by their index number in the list of flights
	 * submitted for the optimization run.
	 * @param optimization the optimization run that invokes the privacy engine
	 * @param input the populations to be ranked
	 * @return the ranked population and the maximum fitness value
	 */
	public PopulationOrderDTO computePopulationOrder(Optimization optimization, Integer[][] input) {
		String url =  optimization.getPrivacyEngineEndpoint() + "/computePopulationOrder";

		RequestEntity<Integer[][]> request =
			RequestEntity.put(url)
						 .accept(MediaType.APPLICATION_JSON)
						 .body(input);

		logger.debug("Requesting computation of population order from Privacy Engine at URL: " + url);
		ResponseEntity<PopulationOrderDTO> response = this.restTemplate.exchange(request, PopulationOrderDTO.class);
		return response.getBody();
	}

    public FitnessQuantilesDTO computeFitnessQuantiles(JeneticsOptimization optimization, Integer[][] input) {

		return null;

	}

	/**
	 * Invokes the PE's /computeClassification endpoint that returns the top-individuals from the population.
	 *
	 * @param optimization the optimization
	 * @param input the population in the format required by the PE
	 * @return the DTO containing the top-individuals and additional information if available
	 */
	public AboveIndividualsDTO computeIndividualsAbove(JeneticsOptimization optimization, Integer[][] input) {
		String url =  optimization.getPrivacyEngineEndpoint() + "/computeClassification";

		RequestEntity<Integer[][]> request =
				RequestEntity.put(url)
						.accept(MediaType.APPLICATION_JSON)
						.body(input);

		logger.debug("Requesting computation of top individuals from Privacy Engine at URL: " + url);
		ResponseEntity<AboveIndividualsDTO> response = this.restTemplate.exchange(request, AboveIndividualsDTO.class);
		return response.getBody();
	}

	/**
	 * Invokes the PE's endpoint to calculate actual fitness values for all individuals from the population.
	 *
	 * @param optimization the optimization
	 * @param input the population in the format required by the PE
	 * @return fitness values for all individuals
	 */
    public Integer[] computeActualFitnessValues(JeneticsOptimization optimization, Integer[][] input) {
		String url =  optimization.getPrivacyEngineEndpoint() + "/computeFitnessClear";
		RequestEntity<Integer[][]> request =
				RequestEntity.put(url)
						.accept(MediaType.APPLICATION_JSON)
						.body(input);

		logger.debug("Requesting computation of actual fitness values for all individuals from Privacy Engine at URL: " + url);
		ResponseEntity<Integer[]> response = this.restTemplate.exchange(request, Integer[].class);
		return response.getBody();
    }
}
