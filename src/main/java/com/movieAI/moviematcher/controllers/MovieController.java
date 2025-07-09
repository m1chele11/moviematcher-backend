package com.movieAI.moviematcher.controllers;

import com.movieAI.moviematcher.dto.MovieRequestDTO;
import com.movieAI.moviematcher.service.MovieFetcherService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*") // Configure this properly for production
public class MovieController {

    private static final Logger logger = Logger.getLogger(MovieController.class.getName());

    private final MovieFetcherService movieFetcherService;

    public MovieController(MovieFetcherService movieFetcherService) {
        this.movieFetcherService = movieFetcherService;
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<String>> searchMovies(@Valid @RequestBody MovieRequestDTO request) {
        logger.info("Received movie search request: " + request.toString());

        return movieFetcherService.fetchMovies(request.getGenres(), request.getPlatforms())
                .map(result -> {
                    if (result.equals("{}")) {
                        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body("No movies found for the given criteria");
                    }
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(error -> {
                    logger.severe("Error in searchMovies: " + error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error fetching movies: " + error.getMessage()));
                });
    }

    @PostMapping("/search/structured")
    public Mono<ResponseEntity<MovieFetcherService.MovieResponse>> searchMoviesStructured(
            @Valid @RequestBody MovieRequestDTO request) {
        logger.info("Received structured movie search request: " + request.toString());

        return movieFetcherService.fetchMoviesStructured(request.getGenres(), request.getPlatforms())
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    logger.severe("Error in searchMoviesStructured: " + error.getMessage());
                    MovieFetcherService.MovieResponse errorResponse =
                            new MovieFetcherService.MovieResponse("Error", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse));
                });
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<String>> searchMoviesWithParams(
            @RequestParam List<String> genres,
            @RequestParam List<String> platforms) {
        logger.info("Received GET movie search request - genres: " + genres + ", platforms: " + platforms);

        return movieFetcherService.fetchMovies(genres, platforms)
                .map(result -> {
                    if (result.equals("{}")) {
                        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body("No movies found for the given criteria");
                    }
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(error -> {
                    logger.severe("Error in searchMoviesWithParams: " + error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error fetching movies: " + error.getMessage()));
                });
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return Mono.just(ResponseEntity.ok("Movie service is running"));
    }

    // Exception handler for validation errors
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleException(Exception ex) {
        logger.severe("Unhandled exception: " + ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request: " + ex.getMessage()));
    }
}


//@RestController
//@RequestMapping("/api/movies")
//public class MovieController {
//
//    private final MovieFetcherService movieFetcherService;
//
//    public MovieController(MovieFetcherService movieFetcherService) {
//        this.movieFetcherService = movieFetcherService;
//    }
//
//    @PostMapping
//    public Mono<String> getMovies(@RequestBody MovieRequestDTO request) {
//        return movieFetcherService.fetchMovies(request.getGenres(), request.getPlatforms());
//    }
//}

