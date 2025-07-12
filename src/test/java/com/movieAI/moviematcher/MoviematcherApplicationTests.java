package com.movieAI.moviematcher;

import com.movieAI.moviematcher.service.EnhancedRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//@SpringBootTest
//class MoviematcherApplicationTests {
//
//	@Autowired
//	private EnhancedRecommendationService enhancedRecommendationService;
//
//	@Test
//	void testStreamingAvailabilityForBatman() {
//		EnhancedRecommendationService.StreamingAvailabilityData data = enhancedRecommendationService.getStreamingAvailability("Batman & Robin");
//
//		System.out.println("🔎 TEST DEBUG OUTPUT");
//		System.out.println("Poster URL: " + data.getPosterUrl());
//		System.out.println("Streaming Platforms: " + data.getStreamingPlatforms());
//		System.out.println("Release Year: " + data.getReleaseYear());
//		System.out.println("IMDB ID: " + data.getImdbId());
//		System.out.println("Genres: " + data.getGenres());
//
//		assertNotNull(data); // Optional: basic assertion
//	}
//}

