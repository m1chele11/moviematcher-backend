package com.movieAI.moviematcher.controllers;


import com.movieAI.moviematcher.dto.PreferencesDTO;
import com.movieAI.moviematcher.service.PreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    @Autowired
    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    public ResponseEntity<?> savePreferences(@RequestBody PreferencesDTO preferencesDTO,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            preferenceService.savePreferences(userDetails.getUsername(), preferencesDTO);
            return ResponseEntity.ok("Preferences saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to save preferences: " + e.getMessage());
        }
    }



}
