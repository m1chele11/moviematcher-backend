package com.movieAI.moviematcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;



/**
 * Service class to interact with the recommendation microservice.
 * This class handles making HTTP requests to the microservice and processing the responses.
 *
 * The recommendation microservice is expected to provide movie recommendations based on a given movie title.
 * It should return a JSON response containing a list of recommended movies or an error message if the movie is not found.
 *
 * Example response from the microservice:
 * {
 *   "recommendations": [
 *     {"title": "Recommended Movie 1", "year": 2020, "rating": 8.5},
 *     {"title": "Recommended Movie 2", "year": 2019, "rating": 7.8}
 *   ]
 * }
 *
 * In case of an error (e.g., movie not found), the response might look like:
 * {
 *   "error": "Movie not found"
 * }
 *
 * The URL of the recommendation microservice can be configured via the application properties.
 */


@Service
public class RecommendationService {

    @Value("${recommendation.service.url:http://localhost:5001}")
    private String recommendationServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RecommendationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public RecommendationResponse getRecommendations(String movieTitle) {
        try {
            // Build the URL with query parameter
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(recommendationServiceUrl + "/recommend")
                    .queryParam("title", movieTitle)
                    .build()
                    .toUri();

            // Log the request URL
            String response = restTemplate.getForObject(uri, String.class);

            // Parse the JSON response
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            if (responseMap.containsKey("error")) {
                throw new RuntimeException("Movie not found: " + responseMap.get("error"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recommendations = (List<Map<String, Object>>) responseMap.get("recommendations");

            return new RecommendationResponse(recommendations);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to connect to recommendation service: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error processing recommendation request: " + e.getMessage());
        }
    }

    // Inner class for response structure
    public static class RecommendationResponse {
        private List<Map<String, Object>> recommendations;

        public RecommendationResponse() {}

        public RecommendationResponse(List<Map<String, Object>> recommendations) {
            this.recommendations = recommendations;
        }

        public List<Map<String, Object>> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<Map<String, Object>> recommendations) {
            this.recommendations = recommendations;
        }
    }
}