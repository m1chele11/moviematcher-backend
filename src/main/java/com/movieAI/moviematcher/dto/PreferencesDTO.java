package com.movieAI.moviematcher.dto;


import java.util.List;
import java.util.Map;




public class PreferencesDTO {

    private Map<String, Integer> user1Genres;
    private Map<String, Integer> user2Genres;
    private List<String> services;

    public PreferencesDTO() {}

    public Map<String, Integer> getUser1Genres() {
        return user1Genres;
    }

    public void setUser1Genres(Map<String, Integer> user1Genres) {
        this.user1Genres = user1Genres;
    }

    public Map<String, Integer> getUser2Genres() {
        return user2Genres;
    }

    public void setUser2Genres(Map<String, Integer> user2Genres) {
        this.user2Genres = user2Genres;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
}

