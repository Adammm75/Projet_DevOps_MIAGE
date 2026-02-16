package org.example.devopslearning.controllers;

import org.example.devopslearning.entities.CourseResource;
import org.example.devopslearning.repositories.CourseResourceRepositoryy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
public class RecourcesController {

    private final CourseResourceRepositoryy repo;

    public RecourcesController(CourseResourceRepositoryy repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CourseResource> listAll() {
        return repo.findAll();
    }

    @GetMapping("/course/{courseId}")
    public List<CourseResource> listByCourse(@PathVariable Long courseId) {
        return repo.findByCourseId(courseId);
    }

    @GetMapping("/{id}")
    public CourseResource getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }
}
