package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.AssignmentService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/student/assignments")
@RequiredArgsConstructor
public class StudentAssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    @GetMapping("/course/{courseId}")
    public String listByCourse(@PathVariable Long courseId, Model model) {
        model.addAttribute("assignments", assignmentService.listByCourse(courseId));
        return "student-list";
    }

    @PostMapping("/{assignmentId}/submit")
    public String submit(@PathVariable Long assignmentId,
                         @RequestParam("file") MultipartFile file,
                         Authentication auth) {

        User student = userService.findByEmail(auth.getName());
        assignmentService.submitAssignment(assignmentId, student, file);
        return "redirect:/student/assignments/course/" + assignmentService.getCourseId(assignmentId);
    }
}
