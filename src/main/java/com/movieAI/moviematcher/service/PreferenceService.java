package com.movieAI.moviematcher.service;

import com.movieAI.moviematcher.dto.PreferencesDTO;
import com.movieAI.moviematcher.model.GenrePreference;
import com.movieAI.moviematcher.model.StreamingServiceSelection;
import com.movieAI.moviematcher.model.Users;
import com.movieAI.moviematcher.repository.GenrePreferenceRepository;
import com.movieAI.moviematcher.repository.StreamingServiceSelectionRepository;
import com.movieAI.moviematcher.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;




@Service
public class PreferenceService {

    private final UserRepository userRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final StreamingServiceSelectionRepository streamingServiceSelectionRepository;

    public PreferenceService(UserRepository userRepository,
                             GenrePreferenceRepository genrePreferenceRepository,
                             StreamingServiceSelectionRepository streamingServiceSelectionRepository) {
        this.userRepository = userRepository;
        this.genrePreferenceRepository = genrePreferenceRepository;
        this.streamingServiceSelectionRepository = streamingServiceSelectionRepository;
    }

    @Transactional
    public void savePreferences(String username, PreferencesDTO preferencesDTO) {
        Users currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found: " + username);
        }

        // Delete existing genre prefs and streaming selections for user_slot 1 and 2
        genrePreferenceRepository.deleteByUserAndUserSlot(currentUser, 1);
        genrePreferenceRepository.deleteByUserAndUserSlot(currentUser, 2);
        streamingServiceSelectionRepository.deleteByUser(currentUser);

        List<GenrePreference> genrePrefsToSave = new ArrayList<>();

        Map<String, Integer> user1Genres = preferencesDTO.getUser1Genres();
        if (user1Genres != null) {
            user1Genres.forEach((genre, rank) -> {
                GenrePreference gp = new GenrePreference();
                gp.setUser(currentUser);
                gp.setGenreName(genre);
                gp.setRanking(rank);
                gp.setUserSlot(1);
                genrePrefsToSave.add(gp);
            });
        }

        Map<String, Integer> user2Genres = preferencesDTO.getUser2Genres();
        if (user2Genres != null) {
            user2Genres.forEach((genre, rank) -> {
                GenrePreference gp = new GenrePreference();
                gp.setUser(currentUser);
                gp.setGenreName(genre);
                gp.setRanking(rank);
                gp.setUserSlot(2);
                genrePrefsToSave.add(gp);
            });
        }

        genrePreferenceRepository.saveAll(genrePrefsToSave);

        List<String> services = preferencesDTO.getServices();
        if (services != null) {
            List<StreamingServiceSelection> serviceSelections = new ArrayList<>();
            services.forEach(serviceName -> {
                StreamingServiceSelection sss = new StreamingServiceSelection();
                sss.setUser(currentUser);
                sss.setServiceName(serviceName);
                serviceSelections.add(sss);
            });
            streamingServiceSelectionRepository.saveAll(serviceSelections);
        }
    }



//    @Transactional
//    public void savePreferences(String username, PreferencesDTO preferencesDTO) {
//        // Extract current authenticated username
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String currentUsername;
//        if (principal instanceof UserDetails) {
//            currentUsername = ((UserDetails) principal).getUsername();
//        } else {
//            currentUsername = principal.toString();
//        }
//
//        // Fetch current user entity
//        Users currentUser = userRepository.findByUsername(currentUsername);
//        if (currentUser == null) {
//            throw new RuntimeException("User not found: " + currentUsername);
//        }
//
//        // Optional: Delete previous preferences for current user
//        genrePreferenceRepository.deleteByUser(currentUser);
//        streamingServiceSelectionRepository.deleteByUser(currentUser);
//
//        // Save Genre Preferences for userSlot = 1 (assuming current user)
//        Map<String, Integer> user1Genres = preferencesDTO.getUser1Genres();
//        List<GenrePreference> genrePrefsToSave = new ArrayList<>();
//        if (user1Genres != null) {
//            user1Genres.forEach((genre, rank) -> {
//                GenrePreference gp = new GenrePreference();
//                gp.setUser(currentUser);
//                gp.setGenreName(genre);
//                gp.setRanking(rank);
//                gp.setUserSlot(1); // userSlot 1 for current user
//                genrePrefsToSave.add(gp);
//            });
//        }
//
//        // Save Genre Preferences for userSlot = 2 if present
//        Map<String, Integer> user2Genres = preferencesDTO.getUser2Genres();
//        if (user2Genres != null) {
//            user2Genres.forEach((genre, rank) -> {
//                GenrePreference gp = new GenrePreference();
//                gp.setUser(currentUser); // For now same user, or update logic if multiple users
//                gp.setGenreName(genre);
//                gp.setRanking(rank);
//                gp.setUserSlot(2); // userSlot 2 for second user preferences
//                genrePrefsToSave.add(gp);
//            });
//        }
//
//        genrePreferenceRepository.saveAll(genrePrefsToSave);
//
//        // Save Streaming Service Selections
//        List<String> services = preferencesDTO.getServices();
//        List<StreamingServiceSelection> serviceSelections = new ArrayList<>();
//        if (services != null) {
//            services.forEach(serviceName -> {
//                StreamingServiceSelection sss = new StreamingServiceSelection();
//                sss.setUser(currentUser);
//                sss.setServiceName(serviceName);
//                serviceSelections.add(sss);
//            });
//        }
//        streamingServiceSelectionRepository.saveAll(serviceSelections);
//    }
}
