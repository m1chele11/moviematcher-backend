package com.movieAI.moviematcher.controllers;


import com.movieAI.moviematcher.model.Student;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;



@RestController
public class StudentController {



    private List<Student> students = new ArrayList<>(List.of(
            new Student(1, "bob", 69),
            new Student(2, "alice", 95)
    ));

    @GetMapping("/students")
    public List<Student> getStudents() {
        return students;
    }

}
