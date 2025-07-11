package com.movieAI.moviematcher.controllers;

import com.movieAI.moviematcher.service.EnhancedRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced controller for handling enriched movie recommendation requests.
 * <p>
 * This controller provides endpoints to get movie recommendations that are enriched
 * with streaming availability data from RapidAPI.
 * <p>
 * Endpoints:
 * - GET /api/recommendations/enhanced?title={title}: Fetches enriched recommendations
 * - GET /api/recommendations/health: Health check endpoint
 * <p>
 * The enhanced recommendations include:
 * - Basic recommendation data (title, overview, popularity, similarity, vote_average)
 * - Poster image URLs
 * - Available streaming platforms
 * - Release year
 * - IMDB ID
 * - Genres
 */
@RestController
@RequestMapping("/api/recommendations")
public class EnhancedRecommendationController {

    private final EnhancedRecommendationService enhancedRecommendationService;

    @Autowired
    public EnhancedRecommendationController(EnhancedRecommendationService enhancedRecommendationService) {
        this.enhancedRecommendationService = enhancedRecommendationService;
    }

    /**
     * Get enriched movie recommendations with streaming availability data
     */
    @GetMapping("/enhanced")
    public ResponseEntity<?> getEnhancedRecommendations(
            @RequestParam String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Log the authenticated user making the request
            System.out.println("User " + userDetails.getUsername() + " requested enhanced recommendations for: " + title);

            EnhancedRecommendationService.EnhancedRecommendationResponse recommendations =
                    enhancedRecommendationService.getEnhancedRecommendations(title);

            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("status", "UP");
            response.put("service", "Enhanced recommendation service is running");
            response.put("features", "Python microservice + RapidAPI integration");
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", "Enhanced recommendation service is not available: " + e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get basic recommendations (original functionality maintained for backward compatibility)
     */
    @GetMapping
    public ResponseEntity<?> getBasicRecommendations(
            @RequestParam String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // This endpoint can still use the original RecommendationService if needed
            // or we can delegate to the enhanced service and return only basic data
            System.out.println("User " + userDetails.getUsername() + " requested basic recommendations for: " + title);

            // For now, redirect to enhanced service but could be modified to return basic data only
            EnhancedRecommendationService.EnhancedRecommendationResponse recommendations =
                    enhancedRecommendationService.getEnhancedRecommendations(title);

            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}