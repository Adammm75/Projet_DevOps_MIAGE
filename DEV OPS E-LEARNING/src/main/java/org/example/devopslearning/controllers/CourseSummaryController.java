package org.example.devopslearning.controllers;

import org.example.devopslearning.repositories.CourseResourceRepositoryy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseSummaryController {

    private final CourseResourceRepositoryy resourceRepository;

    public CourseSummaryController(CourseResourceRepositoryy resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping("/{courseId}/summary")
    public ResponseEntity<Map<String, String>> getCourseSummary(@PathVariable Long courseId) {
        var resource = resourceRepository
                .findTopByCourse_IdAndSummaryIsNotNullOrderByCreatedAtDesc(courseId);

        String summary = resource.map(r -> r.getSummary()).orElse("");

        return ResponseEntity.ok(Map.of("summary", summary));
    }
}
