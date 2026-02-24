package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.enums.StepName;
import org.example.devopslearning.entities.UserCourseStep;
import org.example.devopslearning.services.CourseStepService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/steps")
public class CourseStepRestController {

    private final CourseStepService courseStepService;

    // Initialiser les étapes
    @PostMapping("/init")
    public List<UserCourseStep> initialize(
            @RequestParam Long userId,
            @RequestParam Long courseId
    ) {
        return courseStepService.initializeStepsIfNeeded(userId, courseId);
    }

    // Marquer une étape comme DONE
    @PostMapping("/complete")
    public String complete(
            @RequestParam Long userId,
            @RequestParam Long courseId,
            @RequestParam StepName stepName
    ) {
        courseStepService.markStepAsDone(userId, courseId, stepName);
        return "Step updated successfully";
    }

    // Voir les étapes
    @GetMapping
    public List<UserCourseStep> getSteps(
            @RequestParam Long userId,
            @RequestParam Long courseId
    ) {
        return courseStepService.initializeStepsIfNeeded(userId, courseId);
    }

    // Voir progression
    @GetMapping("/progress")
    public double getProgress(
            @RequestParam Long userId,
            @RequestParam Long courseId
    ) {
        return courseStepService.calculateProgress(userId, courseId);
    }
}