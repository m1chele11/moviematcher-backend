package com.movieAI.moviematcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Enhanced service class that integrates both the Python recommendation microservice
 * and the RapidAPI Streaming Availability API to provide enriched movie recommendations.
 */
@Service
public class EnhancedRecommendationService {

    @Value("${recommendation.service.url:http://localhost:5001}")
    private String recommendationServiceUrl;

    @Value("${rapidapi.key:}")
    private String rapidApiKey;

    @Value("${rapidapi.streaming.url:https://streaming-availability.p.rapidapi.com}")
    private String rapidApiStreamingUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public EnhancedRecommendationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets movie recommendations and enriches them with streaming availability data
     */
    public EnhancedRecommendationResponse getEnhancedRecommendations(String movieTitle) {
        try {
            // Step 1: Get recommendations from Python microservice
            List<Map<String, Object>> basicRecommendations = getBasicRecommendations(movieTitle);

            // Step 2: Enrich each recommendation with streaming data
            List<EnhancedMovieRecommendation> enrichedRecommendations = enrichRecommendations(basicRecommendations);

            return new EnhancedRecommendationResponse(enrichedRecommendations);

        } catch (Exception e) {
            throw new RuntimeException("Error processing enhanced recommendation request: " + e.getMessage(), e);
        }
    }

    /**
     * Gets basic recommendations from the Python microservice
     */
    private List<Map<String, Object>> getBasicRecommendations(String movieTitle) throws Exception {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(recommendationServiceUrl + "/recommend")
                .queryParam("title", movieTitle)
                .build()
                .toUri();

        String response = restTemplate.getForObject(uri, String.class);
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

        if (responseMap.containsKey("error")) {
            throw new RuntimeException("Movie not found: " + responseMap.get("error"));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recommendations = (List<Map<String, Object>>) responseMap.get("recommendations");

        return recommendations != null ? recommendations : new ArrayList<>();
    }

    /**
     * Enriches basic recommendations with streaming availability data
     */
    private List<EnhancedMovieRecommendation> enrichRecommendations(List<Map<String, Object>> basicRecommendations) {
        // Use parallel processing for better performance
        List<CompletableFuture<EnhancedMovieRecommendation>> futures = basicRecommendations.stream()
                .map(this::enrichSingleRecommendationAsync)
                .collect(Collectors.toList());

        // Wait for all futures to complete and collect results
        return futures.stream()
                .map(this::getFutureResult)
                .collect(Collectors.toList());
    }

    /**
     * Asynchronously enriches a single recommendation
     */
    private CompletableFuture<EnhancedMovieRecommendation> enrichSingleRecommendationAsync(Map<String, Object> basicRec) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return enrichSingleRecommendation(basicRec);
            } catch (Exception e) {
                // Log error but don't fail the entire request
                System.err.println("Failed to enrich recommendation for " + basicRec.get("title") + ": " + e.getMessage());
                return createBasicEnhancedRecommendation(basicRec);
            }
        });
    }

    /**
     * Enriches a single recommendation with streaming data
     */
    private EnhancedMovieRecommendation enrichSingleRecommendation(Map<String, Object> basicRec) {
        String title = (String) basicRec.get("title");

        // Get streaming availability data from RapidAPI
        StreamingAvailabilityData streamingData = getStreamingAvailability(title);

        return new EnhancedMovieRecommendation(
                title,
                (String) basicRec.get("overview"),
                getDoubleValue(basicRec, "popularity"),
                getDoubleValue(basicRec, "similarity"),
                getDoubleValue(basicRec, "vote_average"),
                streamingData.getPosterUrl(),
                streamingData.getStreamingPlatforms(),
                streamingData.getReleaseYear(),
                streamingData.getImdbId(),
                streamingData.getGenres()
        );
    }

    /**
     * Creates a basic enhanced recommendation when streaming data is unavailable
     */
    private EnhancedMovieRecommendation createBasicEnhancedRecommendation(Map<String, Object> basicRec) {
        return new EnhancedMovieRecommendation(
                (String) basicRec.get("title"),
                (String) basicRec.get("overview"),
                getDoubleValue(basicRec, "popularity"),
                getDoubleValue(basicRec, "similarity"),
                getDoubleValue(basicRec, "vote_average"),
                null, // No poster URL
                new ArrayList<>(), // No streaming platforms
                null, // No release year
                null, // No IMDB ID
                new ArrayList<>() // No genres
        );
    }

    /**
     * Gets streaming availability data from RapidAPI
     */
    private StreamingAvailabilityData getStreamingAvailability(String title) {
        try {
            if (rapidApiKey == null || rapidApiKey.isEmpty()) {
                return new StreamingAvailabilityData(); // Return empty data if no API key
            }

            // Search for the movie first
            String searchUrl = rapidApiStreamingUrl + "/shows/search/title";
            URI searchUri = UriComponentsBuilder.fromUriString(searchUrl)
                    .queryParam("title", title)
                    .queryParam("country", "us")
                    .queryParam("show_type", "movie")
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", rapidApiKey);
            headers.set("X-RapidAPI-Host", "streaming-availability.p.rapidapi.com");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(searchUri, HttpMethod.GET, entity, String.class);

            return parseStreamingResponse(response.getBody());

        } catch (RestClientException e) {
            System.err.println("RapidAPI request failed for title: " + title + " - " + e.getMessage());
            return new StreamingAvailabilityData();
        } catch (Exception e) {
            System.err.println("Error processing streaming data for title: " + title + " - " + e.getMessage());
            return new StreamingAvailabilityData();
        }
    }

    /**
     * Parses the RapidAPI response to extract streaming availability data
     */
    private StreamingAvailabilityData parseStreamingResponse(String responseBody) {
        System.out.println("RapidAPI raw response: " + responseBody);
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultsNode = rootNode.path("results");

            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode firstResult = resultsNode.get(0);

                String posterUrl = extractPosterUrl(firstResult);
                List<String> streamingPlatforms = extractStreamingPlatforms(firstResult);
                Integer releaseYear = extractReleaseYear(firstResult);
                String imdbId = extractImdbId(firstResult);
                List<String> genres = extractGenres(firstResult);

                return new StreamingAvailabilityData(posterUrl, streamingPlatforms, releaseYear, imdbId, genres);
            }

            return new StreamingAvailabilityData();
        } catch (Exception e) {
            System.err.println("Error parsing streaming response: " + e.getMessage());
            return new StreamingAvailabilityData();
        }
    }

    private String extractPosterUrl(JsonNode movieNode) {
        JsonNode posterPath = movieNode.path("posterURLs").path("original");
        return posterPath.isTextual() ? posterPath.asText() : null;
    }

    private List<String> extractStreamingPlatforms(JsonNode movieNode) {
        List<String> platforms = new ArrayList<>();
        JsonNode streamingOptions = movieNode.path("streamingOptions").path("us");

        if (streamingOptions.isArray()) {
            for (JsonNode option : streamingOptions) {
                String serviceName = option.path("service").path("name").asText();
                if (!serviceName.isEmpty() && !platforms.contains(serviceName)) {
                    platforms.add(serviceName);
                }
            }
        }

        return platforms;
    }

    private Integer extractReleaseYear(JsonNode movieNode) {
        int year = movieNode.path("year").asInt(0);
        return year > 0 ? year : null;
    }

    private String extractImdbId(JsonNode movieNode) {
        String imdbId = movieNode.path("imdbId").asText();
        return imdbId.isEmpty() ? null : imdbId;
    }

    private List<String> extractGenres(JsonNode movieNode) {
        List<String> genres = new ArrayList<>();
        JsonNode genresNode = movieNode.path("genres");

        if (genresNode.isArray()) {
            for (JsonNode genre : genresNode) {
                String genreName = genre.path("name").asText();
                if (!genreName.isEmpty()) {
                    genres.add(genreName);
                }
            }
        }

        return genres;
    }

    /**
     * Helper method to safely extract double values from Map
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    /**
     * Helper method to safely get CompletableFuture result
     */
    private EnhancedMovieRecommendation getFutureResult(CompletableFuture<EnhancedMovieRecommendation> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error getting future result", e);
        }
    }

    // Data classes for structured responses
    public static class EnhancedRecommendationResponse {
        private List<EnhancedMovieRecommendation> recommendations;

        public EnhancedRecommendationResponse() {}

        public EnhancedRecommendationResponse(List<EnhancedMovieRecommendation> recommendations) {
            this.recommendations = recommendations;
        }

        public List<EnhancedMovieRecommendation> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<EnhancedMovieRecommendation> recommendations) {
            this.recommendations = recommendations;
        }
    }

    public static class EnhancedMovieRecommendation {
        private String title;
        private String overview;
        private Double popularity;
        private Double similarity;
        private Double voteAverage;
        private String posterUrl;
        private List<String> streamingPlatforms;
        private Integer releaseYear;
        private String imdbId;
        private List<String> genres;

        public EnhancedMovieRecommendation() {}

        public EnhancedMovieRecommendation(String title, String overview, Double popularity, Double similarity,
                                           Double voteAverage, String posterUrl, List<String> streamingPlatforms,
                                           Integer releaseYear, String imdbId, List<String> genres) {
            this.title = title;
            this.overview = overview;
            this.popularity = popularity;
            this.similarity = similarity;
            this.voteAverage = voteAverage;
            this.posterUrl = posterUrl;
            this.streamingPlatforms = streamingPlatforms;
            this.releaseYear = releaseYear;
            this.imdbId = imdbId;
            this.genres = genres;
        }

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }

        public Double getPopularity() { return popularity; }
        public void setPopularity(Double popularity) { this.popularity = popularity; }

        public Double getSimilarity() { return similarity; }
        public void setSimilarity(Double similarity) { this.similarity = similarity; }

        public Double getVoteAverage() { return voteAverage; }
        public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

        public List<String> getStreamingPlatforms() { return streamingPlatforms; }
        public void setStreamingPlatforms(List<String> streamingPlatforms) { this.streamingPlatforms = streamingPlatforms; }

        public Integer getReleaseYear() { return releaseYear; }
        public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

        public String getImdbId() { return imdbId; }
        public void setImdbId(String imdbId) { this.imdbId = imdbId; }

        public List<String> getGenres() { return genres; }
        public void setGenres(List<String> genres) { this.genres = genres; }
    }

    public static class StreamingAvailabilityData {
        private String posterUrl;
        private List<String> streamingPlatforms;
        private Integer releaseYear;
        private String imdbId;
        private List<String> genres;

        public StreamingAvailabilityData() {
            this.streamingPlatforms = new ArrayList<>();
            this.genres = new ArrayList<>();
        }

        public StreamingAvailabilityData(String posterUrl, List<String> streamingPlatforms,
                                         Integer releaseYear, String imdbId, List<String> genres) {
            this.posterUrl = posterUrl;
            this.streamingPlatforms = streamingPlatforms != null ? streamingPlatforms : new ArrayList<>();
            this.releaseYear = releaseYear;
            this.imdbId = imdbId;
            this.genres = genres != null ? genres : new ArrayList<>();
        }

        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

        public List<String> getStreamingPlatforms() { return streamingPlatforms; }
        public void setStreamingPlatforms(List<String> streamingPlatforms) { this.streamingPlatforms = streamingPlatforms; }

        public Integer getReleaseYear() { return releaseYear; }
        public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

        public String getImdbId() { return imdbId; }
        public void setImdbId(String imdbId) { this.imdbId = imdbId; }

        public List<String> getGenres() { return genres; }
        public void setGenres(List<String> genres) { this.genres = genres; }
    }
}