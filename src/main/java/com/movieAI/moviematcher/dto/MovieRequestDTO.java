package com.movieAI.moviematcher.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;






public class MovieRequestDTO {

    @NotNull(message = "Genres cannot be null")
    @NotEmpty(message = "At least one genre must be specified")
    @Size(max = 10, message = "Maximum 10 genres allowed")
    private List<String> genres;

    @NotNull(message = "Platforms cannot be null")
    @NotEmpty(message = "At least one platform must be specified")
    @Size(max = 10, message = "Maximum 10 platforms allowed")
    private List<String> platforms;

    // Default constructor
    public MovieRequestDTO() {}

    // Constructor with parameters
    public MovieRequestDTO(List<String> genres, List<String> platforms) {
        this.genres = genres;
        this.platforms = platforms;
    }

    // Getters and setters
    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    @Override
    public String toString() {
        return "MovieRequestDTO{" +
                "genres=" + genres +
                ", platforms=" + platforms +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MovieRequestDTO that = (MovieRequestDTO) obj;
        return genres.equals(that.genres) && platforms.equals(that.platforms);
    }

    @Override
    public int hashCode() {
        return genres.hashCode() + platforms.hashCode();
    }
}
