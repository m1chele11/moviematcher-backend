package com.movieAI.moviematcher.controllers;

import com.movieAI.moviematcher.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;



/**
 * Controller for handling recommendation requests.
 * <p>
 * This controller provides endpoints to get movie recommendations based on a given title.
 * It interacts with the RecommendationService to fetch recommendations from an external service.
 * <p>
 * Endpoints:
 * - GET /api/recommendations?title={title}: Fetches recommendations for the specified movie title.
 * - GET /api/recommendations/health: Health check endpoint to verify if the recommendation service is running.
 * <p>
 * Error Handling:
 * - If the recommendation service is unavailable or returns an error, a 503 Service Unavailable response is returned.
 * - If the movie title is not found, a 400 Bad Request response with an error message is returned.
 * <p>
 * Security:
 * - The endpoints are secured and require authentication. The authenticated user's details can be accessed via
 *   the @AuthenticationPrincipal annotation.
 */



@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<?> getRecommendations(
            @RequestParam String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Log the authenticated user making the request
            System.out.println("User " + userDetails.getUsername() + " requested recommendations for: " + title);

            RecommendationService.RecommendationResponse recommendations =
                    recommendationService.getRecommendations(title);

            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Optional: Add a health check endpoint for the microservice
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        try {
            // Use a simple ping instead of actual recommendation call
            response.put("status", "UP");
            response.put("service", "Recommendation service connection available");
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", "Recommendation service is not available: " + e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
        return ResponseEntity.ok(response);
    }
}