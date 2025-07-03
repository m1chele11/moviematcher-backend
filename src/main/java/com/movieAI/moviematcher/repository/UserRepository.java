package com.movieAI.moviematcher.repository;


import com.movieAI.moviematcher.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    Users findByUsername(String username);
}
