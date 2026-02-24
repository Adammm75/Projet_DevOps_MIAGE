package org.example.devopslearning.controllers;

import org.example.devopslearning.repositories.CourseResourceRepositoryy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
public class SearchController {

    private final CourseResourceRepositoryy resourceRepository;

    public SearchController(CourseResourceRepositoryy resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {

        var results = resourceRepository.searchByKeyword(q);

        var response = results.stream().map(r -> Map.of(
                "title", r.getCourse().getTitle(),
                "link", "/"
                        + r.getCourse().getId()))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(response);
    }
}