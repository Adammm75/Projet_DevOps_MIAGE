package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CourseAccessService;
import org.example.devopslearning.services.CoursService;  // ✅ Ajouté
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseAccessService courseAccessService;
    private final UserService userService;
    private final CoursService coursService;  // ✅ Ajouté

    @GetMapping
    public String list(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());
        List<Cours> cours = courseAccessService.getCoursAccessibles(student.getId());
        model.addAttribute("courses", cours);
        return "courses/student-course-list";
    }

    @GetMapping("/{courseId}")
    public String details(@PathVariable Long courseId, Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        // Sécurité : empêche accès à un cours non autorisé
        if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
            return "redirect:/student/courses?forbidden";
        }

        model.addAttribute("course", courseAccessService.getCourseDetails(courseId));
        model.addAttribute("resources", coursService.getResources(courseId));
        model.addAttribute("assignments", courseAccessService.getAssignments(courseId));
        model.addAttribute("qcms", courseAccessService.getQcms(courseId));
        return "courses/student-course-details";
    }
}