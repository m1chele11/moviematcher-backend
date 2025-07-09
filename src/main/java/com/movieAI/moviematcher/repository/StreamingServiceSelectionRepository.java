package com.movieAI.moviematcher.repository;

import com.movieAI.moviematcher.model.StreamingServiceSelection;
import com.movieAI.moviematcher.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;






@Repository
public interface StreamingServiceSelectionRepository extends JpaRepository<StreamingServiceSelection, Long> {

    // Find all streaming service selections for a specific user
    List<StreamingServiceSelection> findByUser(Users user);


    // Optional: find by user and userSlot (for multi-user)
  //  List<StreamingServiceSelection> findByUserAndUserSlot(Users user, int userSlot);

    void deleteByUser(Users user);

}

