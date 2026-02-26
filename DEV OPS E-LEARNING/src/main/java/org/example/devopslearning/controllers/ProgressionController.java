package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProgressionController {

    private final UserService userService;
    private final ProgressionService progressionService;
    private final AdminStatsService adminStatsService;
    private final CoursService coursService;

    // ========================================
    // ÉTUDIANT — Mon avancement
    // ========================================

    @GetMapping("/student/progression")
    public String studentProgression(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<ProgressionService.CourseProgressDTO> progressions =
                progressionService.getStudentProgression(student.getId());

        int globalPercent = progressionService.getGlobalProgressPercent(student.getId());
        BigDecimal globalAverage = progressionService.getGlobalAverage(student.getId());

        long coursesCompleted  = progressions.stream().filter(ProgressionService.CourseProgressDTO::isCourseCompleted).count();
        long coursesInProgress = progressions.stream().filter(p -> p.getProgressPercent() > 0 && !p.isCourseCompleted()).count();
        long coursesNotStarted = progressions.stream().filter(p -> p.getProgressPercent() == 0).count();
        long totalLate         = progressions.stream().mapToLong(ProgressionService.CourseProgressDTO::getLateAssignments).sum();

        model.addAttribute("student", student);
        model.addAttribute("progressions", progressions);
        model.addAttribute("globalPercent", globalPercent);
        model.addAttribute("globalAverage", globalAverage);
        model.addAttribute("totalCourses", progressions.size());
        model.addAttribute("coursesCompleted", coursesCompleted);
        model.addAttribute("coursesInProgress", coursesInProgress);
        model.addAttribute("coursesNotStarted", coursesNotStarted);
        model.addAttribute("totalLate", totalLate);

        return "progression/student-progression";
    }

    // ========================================
    // ENSEIGNANT — Suivi de la classe
    // ========================================

    @GetMapping("/teacher/progression")
    public String teacherProgression(Authentication auth,
                                     @RequestParam(required = false) Long courseId,
                                     @RequestParam(defaultValue = "all") String filter,
                                     Model model) {
        User teacher = userService.findByEmail(auth.getName());
        List<Cours> courses = coursService.getCoursesByTeacher(teacher);

        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courses);
        model.addAttribute("selectedFilter", filter);

        if (courseId != null) {
            Cours selectedCourse = coursService.getById(courseId);
            List<ProgressionService.StudentProgressDTO> allProgressions =
                    progressionService.getCourseProgressionForTeacher(courseId);

            List<ProgressionService.StudentProgressDTO> filtered = switch (filter) {
                case "EN_AVANCE" -> allProgressions.stream().filter(s -> "EN_AVANCE".equals(s.getStatus())).toList();
                case "INACTIF"   -> allProgressions.stream().filter(s -> "INACTIF".equals(s.getStatus())).toList();
                case "EN_COURS"  -> allProgressions.stream().filter(s -> "EN_COURS".equals(s.getStatus())).toList();
                case "TERMINE"   -> allProgressions.stream().filter(s -> "TERMINE".equals(s.getStatus())).toList();
                default          -> allProgressions;
            };

            int avgProgress = progressionService.getAverageCourseProgression(courseId);
            Map<String, Long> stats = progressionService.getProgressionStats(courseId);
            Double classAverage = progressionService.getCourseClassAverage(courseId);

            // Nombre d'étudiants en retard (au moins 1 devoir manqué en retard)
            long atRiskCount = allProgressions.stream()
                    .filter(s -> s.getLateAssignments() > 0).count();

            model.addAttribute("selectedCourse", selectedCourse);
            model.addAttribute("studentProgressions", filtered);
            model.addAttribute("allStudentProgressions", allProgressions);
            model.addAttribute("avgProgress", avgProgress);
            model.addAttribute("progressionStats", stats);
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("classAverage", classAverage);
            model.addAttribute("atRiskCount", atRiskCount);
        }

        return "progression/teacher-progression";
    }

    // ========================================
    // ADMIN — Vue globale plateforme
    // ========================================

    @GetMapping("/admin/progression")
    public String adminProgression(Authentication auth, Model model) {
        Map<String, Object> stats = adminStatsService.buildAdminStats();
        model.addAllAttributes(stats);
        return "progression/admin-progression";
    }
}