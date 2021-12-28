package at.jku.dke.slotmachine.optimizer.service;

import at.jku.dke.slotmachine.optimizer.optimization.Optimization;
import at.jku.dke.slotmachine.privacyEngine.dto.PopulationOrderDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;

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
	public PopulationOrderDTO computePopulationOrder(Optimization optimization, int[][] input) {
		String url =  optimization.getPrivacyEngineEndpoint() + "/computePopulationOrder";

		RequestEntity<int[][]> request =
			RequestEntity.put(url)
						 .accept(MediaType.APPLICATION_JSON)
						 .body(input);

		logger.info("Requesting computation of population order from Privacy Engine at URL: " + url);
		ResponseEntity<PopulationOrderDTO> response = this.restTemplate.exchange(request, PopulationOrderDTO.class);
		
		return response.getBody();
	}
}
