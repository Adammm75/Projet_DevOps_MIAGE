package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.CourseProgress;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CourseProgressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/progress")
public class CourseProgressController {

    private final CourseProgressService progressService;

    @PostMapping("/{userId}/{courseId}")
    public CourseProgress updateProgress(
            @PathVariable Long userId,
            @PathVariable Long courseId
    ) {

        User user = new User();
        user.setId(userId);

        Cours course = new Cours();
        course.setId(courseId);

        return progressService.updateProgress(user, course);
    }
}