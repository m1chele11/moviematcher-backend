package com.movieAI.moviematcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.List;

/**
 * Service for fetching movie data from RapidAPI Streaming Availability API.
 * <p>
 * This service provides methods to:
 * - Search for movies by title
 * - Get streaming availability information
 * - Extract movie metadata (poster URLs, genres, etc.)
 * <p>
 * The service handles API rate limiting, error responses, and provides fallback behavior
 * when the API is unavailable.
 */
@Service
public class RapidApiMovieFetcher {

    @Value("${rapidapi.key:}")
    private String rapidApiKey;

    @Value("${rapidapi.streaming.url:https://streaming-availability.p.rapidapi.com}")
    private String rapidApiStreamingUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RapidApiMovieFetcher(@Qualifier("externalApiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Searches for a movie by title and returns detailed information including streaming availability
     */
    public MovieDetails searchMovie(String title) {
        try {
            if (rapidApiKey == null || rapidApiKey.isEmpty()) {
                throw new IllegalStateException("RapidAPI key is not configured");
            }

            String response = performMovieSearch(title);
            return parseMovieResponse(response);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch movie data from RapidAPI: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing movie search: " + e.getMessage(), e);
        }
    }

    /**
     * Performs the actual API call to search for movies
     */
    private String performMovieSearch(String title) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(rapidApiStreamingUrl + "/search/title")
                .queryParam("title", title)
                .queryParam("country", "us")
                .queryParam("show_type", "movie")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", "streaming-availability.p.rapidapi.com");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    /**
     * Parses the movie response from RapidAPI to extract relevant details
     */
    private MovieDetails parseMovieResponse(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.path("result");

        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstResult = resultsNode.get(0);

            String title = firstResult.path("title").asText(null);
            String overview = firstResult.path("overview").asText(null);
            String posterUrl = firstResult.path("posterURLs").path("original").asText(null);
            int releaseYear = firstResult.path("year").asInt(0);
            String imdbId = firstResult.path("imdbId").asText(null);
            List<String> genres = extractGenres(firstResult);
            List<String> streamingPlatforms = extractStreamingPlatforms(firstResult);

            return new MovieDetails(title, overview, posterUrl, releaseYear, imdbId, genres, streamingPlatforms);
        }

        return new MovieDetails(); // empty details if no result found
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

    private List<String> extractStreamingPlatforms(JsonNode movieNode) {
        List<String> platforms = new ArrayList<>();
        JsonNode streamingOptionsNode = movieNode.path("streamingOptions").path("us");
        if (streamingOptionsNode.isArray()) {
            for (JsonNode option : streamingOptionsNode) {
                String serviceName = option.path("service").path("name").asText();
                if (!serviceName.isEmpty() && !platforms.contains(serviceName)) {
                    platforms.add(serviceName);
                }
            }
        }
        return platforms;
    }

    /**
     * Data class representing movie details with streaming info
     */
    public static class MovieDetails {
        private String title;
        private String overview;
        private String posterUrl;
        private int releaseYear;
        private String imdbId;
        private List<String> genres;
        private List<String> streamingPlatforms;

        public MovieDetails() {
            this.genres = new ArrayList<>();
            this.streamingPlatforms = new ArrayList<>();
        }

        public MovieDetails(String title, String overview, String posterUrl, int releaseYear,
                            String imdbId, List<String> genres, List<String> streamingPlatforms) {
            this.title = title;
            this.overview = overview;
            this.posterUrl = posterUrl;
            this.releaseYear = releaseYear;
            this.imdbId = imdbId;
            this.genres = genres != null ? genres : new ArrayList<>();
            this.streamingPlatforms = streamingPlatforms != null ? streamingPlatforms : new ArrayList<>();
        }

        // Getters and setters...

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }

        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

        public int getReleaseYear() { return releaseYear; }
        public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

        public String getImdbId() { return imdbId; }
        public void setImdbId(String imdbId) { this.imdbId = imdbId; }

        public List<String> getGenres() { return genres; }
        public void setGenres(List<String> genres) { this.genres = genres; }

        public List<String> getStreamingPlatforms() { return streamingPlatforms; }
        public void setStreamingPlatforms(List<String> streamingPlatforms) { this.streamingPlatforms = streamingPlatforms; }
    }
}
