package com.movieAI.moviematcher.component;

import com.movieAI.moviematcher.service.MovieFetcherService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private final MovieFetcherService movieFetcherService;

    public StartupRunner(MovieFetcherService movieFetcherService) {
        this.movieFetcherService = movieFetcherService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        movieFetcherService.cacheGenres()
                .doOnSuccess(v -> System.out.println("Genres cached successfully at startup"))
                .doOnError(e -> System.err.println("Failed to cache genres at startup: " + e.getMessage()))
                .subscribe();
    }
}