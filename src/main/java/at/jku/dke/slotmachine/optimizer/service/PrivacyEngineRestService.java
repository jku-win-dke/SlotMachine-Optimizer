package at.jku.dke.slotmachine.optimizer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.jku.dke.slotmachine.optimizer.domain.PopulationOrder;
import at.jku.dke.slotmachine.optimizer.service.dto.OptimizationDTO;

@Service
public class PrivacyEngineRestService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private final RestTemplate restTemplate;
	private String host = null;
	private ObjectMapper objectMapper = null;
	
	public PrivacyEngineRestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        String hostName = "localhost"; //TODO set correct hostname
    	Integer hostPort = 8089; //TODO set correct port

		host = "http://" + hostName + ":" + hostPort;
		logger.info("Use Privacy Engine host: " + host);		
    }
	
	// create the weightMap neede for the createClearSession-Request
	public void createClearSession(OptimizationDTO optDto) {		
		int[][] weightMap = new int[optDto.getFlights().length][optDto.getFlights()[0].getWeightMap().length];
		for (int i = 0; i < weightMap.length; i++) {
			weightMap[i] = optDto.getFlights()[i].getWeightMap();
		}
		createClearSessionRequest(weightMap);
		return;
	}
	
	private void createClearSessionRequest(int[][] weightMap) {
		logger.info("createClearSession");
		String url = host + "/session_clear";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<int[][]> request = new HttpEntity<int[][]>(weightMap, headers);
		try {
			restTemplate.put(url, request);
		} catch(RestClientException rce) {
			logger.info("Session has not been created.");
			return;
		}
		logger.info("Session has been created.");
		return;
	}
	
	public PopulationOrder computatePopulationOrder(int[][] input) {
		String url = host + "/computate_population_order";
		
		// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html
		// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/RequestEntity.html
		RequestEntity<int[][]> request = RequestEntity
				.put(url)
				.accept(MediaType.APPLICATION_JSON)
				.body(input);
		ResponseEntity<PopulationOrder> response = restTemplate.exchange(request, PopulationOrder.class);
		
		return response.getBody();
	}
}
