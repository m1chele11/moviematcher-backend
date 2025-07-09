package com.movieAI.moviematcher.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class MovieFetcherService {

    private static final Logger logger = Logger.getLogger(MovieFetcherService.class.getName());

    private final WebClient webClient;

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host:streaming-availability.p.rapidapi.com}")
    private String apiHost;

    @Value("${rapidapi.base-url:https://streaming-availability.p.rapidapi.com}")
    private String baseUrl;

    public MovieFetcherService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
                .build();
    }

    public Mono<String> fetchMovies(List<String> genres, List<String> platforms) {
        // Input validation
        if (genres == null || genres.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Genres list cannot be null or empty"));
        }

        if (platforms == null || platforms.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Platforms list cannot be null or empty"));
        }

        // Convert genre names to IDs and log
        List<String> genreIds = convertGenreNamesToIds(genres);
        logger.info("Converted genre names: " + genres + " to IDs: " + genreIds);

        if (genreIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException("No valid genre IDs found for given genre names"));
        }

        // Convert platform names to lowercase, comma-separated
        String genreQuery = String.join(",", genreIds);
        String platformQuery = platforms.stream()
                .map(String::toLowerCase)
                .collect(Collectors.joining(","));

        // Dummy title for now (RapidAPI requires this param)
        String titleSearch = "inception"; // optional: make this dynamic later

        // Build URI for request
        URI uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(apiHost)
                .path("/shows/search/title")
                .queryParam("title", titleSearch)
                .queryParam("country", "us")
                .queryParam("series_granularity", "show")
                .queryParam("show_type", "movie")
                .queryParam("output_language", "en")
                .queryParam("genres", genreQuery)
                .queryParam("catalogs", platformQuery)
                .build()
                .toUri();

        logger.info("Final RapidAPI request URI: " + uri.toString());

        // Make API call
        return webClient.get()
                .uri(uri)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", apiHost)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    logger.warning("Client error: " + response.statusCode());
                    return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    logger.warning("Server error: " + response.statusCode());
                    return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                })
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                .doOnSuccess(result -> logger.info("Successfully fetched movies"))
                .doOnError(error -> logger.severe("Error fetching movies: " + error.getMessage()))
                .onErrorResume(error -> {
                    logger.severe("Failed to fetch movies after retries: " + error.getMessage());
                    return Mono.just("{}");
                });
    }

    public List<String> convertGenreNamesToIds(List<String> genreNames) {
        List<String> ids = genreNames.stream()
                .map(name -> {
                    String id = genreNameToIdCache.get(name.toLowerCase());
                    logger.info("Converted genre name '" + name + "' to id '" + id + "'");
                    return id;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.info("Final genre IDs list: " + ids);
        return ids;
    }


    // Alternative method that returns a structured response
    public Mono<MovieResponse> fetchMoviesStructured(List<String> genres, List<String> platforms) {
        return fetchMovies(genres, platforms)
                .map(this::parseMovieResponse)
                .onErrorReturn(new MovieResponse("Error fetching movies", null));
    }

    public Mono<List<Genre>> fetchGenres() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(apiHost)
                        .path("/genres")
                        .queryParam("output_language", "en")
                        .build())
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", apiHost)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Genre>>() {})
                .doOnError(error -> logger.severe("Error fetching genres: " + error.getMessage()));
    }

    private Map<String, String> genreNameToIdCache = new ConcurrentHashMap<>();

    public Mono<Void> cacheGenres() {
        return fetchGenres()
                .doOnNext(genres -> {
                    for (Genre genre : genres) {
                        genreNameToIdCache.put(genre.getName().toLowerCase(), genre.getId());
                    }
                    logger.info("Cached " + genreNameToIdCache.size() + " genres");
                })
                .then();
    }

//    public List<String> convertGenreNamesToIds(List<String> genreNames) {
//        return genreNames.stream()
//                .map(name -> genreNameToIdCache.get(name.toLowerCase()))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }



    public static class Genre {
        private String id;
        private String name;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }


    private MovieResponse parseMovieResponse(String jsonResponse) {
        // Basic parsing - you might want to use Jackson ObjectMapper for proper JSON parsing
        if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.equals("{}")) {
            return new MovieResponse("No movies found", null);
        }

        // For now, return the raw JSON. In production, you'd parse this into POJOs
        return new MovieResponse("Success", jsonResponse);
    }

    // Inner class for structured response
    public static class MovieResponse {
        private final String status;
        private final String data;

        public MovieResponse(String status, String data) {
            this.status = status;
            this.data = data;
        }

        public String getStatus() { return status; }
        public String getData() { return data; }
    }
}

