package com.movieAI.moviematcher.service;


import com.movieAI.moviematcher.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.movieAI.moviematcher.model.Users;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Users register(Users user){
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEmail(user.getEmail());
        return userRepository.save(user);
    }

    public String login(Users user){
        return "Login successful for user: " + user.getUsername();
    }

    public String verify(Users user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                // IMPORTANT: Fetch the full user from database to get the email
                Users fullUser = userRepository.findByUsername(user.getUsername());

                // Debug logging
               // System.out.println("User from request: " + user.getUsername() + ", email: " + user.getEmail());
                //System.out.println("User from database: " + fullUser.getUsername() + ", email: " + fullUser.getEmail());

                // Use the email from the database user, not the request user
                return jwtService.generateToken(fullUser.getUsername(), fullUser.getEmail());
            }
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials", e);
        }

        return "bad";
    }
}
