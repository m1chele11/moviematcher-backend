package com.movieAI.moviematcher.service;

import com.movieAI.moviematcher.model.UserPrincipal;
import com.movieAI.moviematcher.model.Users;
import com.movieAI.moviematcher.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {


    private UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Fetch user from the database using the repository
        Users user = userRepository.findByUsername(username);

        if (user == null) {
            // If user is not found, throw an exception
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new UserPrincipal(user);
    }

}
