package org.example.devopslearning.controllers;

import org.example.devopslearning.entities.CourseResource;
import org.example.devopslearning.repositories.CourseResourceRepositoryy;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student/courses")
public class StudentCourseSummaryController {

    private final CourseResourceRepositoryy resourceRepository;

    public StudentCourseSummaryController(CourseResourceRepositoryy resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    // Récupérer le dernier résumé d’un cours par courseId
    @GetMapping("/{courseId}/summary")
    public ResponseEntity<?> getCourseSummary(@PathVariable Long courseId) {

        Optional<CourseResource> resourceOpt = resourceRepository
                .findTopByCourse_IdAndSummaryIsNotNullOrderByCreatedAtDesc(courseId);

        if (resourceOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "title", "",
                    "summary", "",
                    "keywords", new String[]{}
            ));
        }

        CourseResource resource = resourceOpt.get();

        return ResponseEntity.ok(Map.of(
                "title", resource.getTitle(),
                "summary", resource.getSummary(),
                "keywords", resource.getKeywords() != null ? resource.getKeywords() : new String[]{}
        ));
    }
}