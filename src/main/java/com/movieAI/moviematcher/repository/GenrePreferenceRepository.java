package com.movieAI.moviematcher.repository;

import com.movieAI.moviematcher.model.GenrePreference;
import com.movieAI.moviematcher.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;







@Repository
public interface GenrePreferenceRepository extends JpaRepository<GenrePreference, Long> {

    // Find all genre preferences for a specific user
    List<GenrePreference> findByUser(Users user);

    // Optional: find by user and userSlot (for multi-user)
   // List<GenrePreference> findByUserAndUserSlot(Users user, int userSlot);


    // Optional: delete by user
    void deleteByUser(Users user);
}

