package com.movieAI.moviematcher.controllers;


import com.movieAI.moviematcher.model.Users;
import com.movieAI.moviematcher.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public Users register(@RequestBody Users user){
        return userService.register(user);
    }
}
