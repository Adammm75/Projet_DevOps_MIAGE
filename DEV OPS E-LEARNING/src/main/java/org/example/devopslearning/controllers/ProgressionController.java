package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.entities.StudentPoints.AcademicLevel;
import org.example.devopslearning.repositories.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/progression")
@RequiredArgsConstructor
public class ProgressionController {

    private final StudentPointsRepository studentPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final StudentBadgeRepository studentBadgeRepository;
    private final CoursRepository coursRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    // -------------------------------------------------------------------------
    // ✅ NOUVELLE ROUTE — page de sélection de cours
    // Appelée depuis le dashboard : /progression/student
    // -------------------------------------------------------------------------

    @GetMapping("/student")
    public String selectCourse(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        Long studentId = extractUserId(userDetails);

        // Récupère tous les cours auxquels l'étudiant est inscrit
        List<Cours> cours = courseEnrollmentRepository
                .findByStudentId(studentId)
                .stream()
                .map(CourseEnrollment::getCourse)
                .collect(Collectors.toList());

        model.addAttribute("cours", cours);
        return "progression/student-progression-select";
    }

    // -------------------------------------------------------------------------
    // VUE ÉTUDIANT pour un cours précis
    // Appelée après sélection : /progression/student/{courseId}
    // -------------------------------------------------------------------------

    @GetMapping("/student/{courseId}")
    public String studentProgression(@PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Long studentId = extractUserId(userDetails);

        Cours course = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        StudentPoints sp = studentPointsRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        List<StudentBadge> badges = studentBadgeRepository
                .findByStudentIdAndCourseId(studentId, courseId);

        List<PointTransaction> transactions = pointTransactionRepository
                .findByStudentIdAndCourseIdOrderByEarnedAtDesc(studentId, courseId);

        int totalPoints = sp != null ? sp.getTotalPoints() : 0;
        int progressPercent;
        String nextLevelLabel;
        if (totalPoints < 100) {
            progressPercent = totalPoints;
            nextLevelLabel = "Prochain niveau : Compréhension (100 pts)";
        } else if (totalPoints < 250) {
            progressPercent = (int) ((totalPoints - 100) / 1.5);
            nextLevelLabel = "Prochain niveau : Maîtrise (250 pts)";
        } else {
            progressPercent = 100;
            nextLevelLabel = "🎉 Niveau maximum atteint !";
        }

        model.addAttribute("course", course);
        model.addAttribute("studentPoints", sp);
        model.addAttribute("badges", badges);
        model.addAttribute("transactions", transactions);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("nextLevelLabel", nextLevelLabel);

        return "progression/student-progression";
    }

    // -------------------------------------------------------------------------
    // VUE ENSEIGNANT
    // -------------------------------------------------------------------------

    @GetMapping("/teacher/{courseId}")
    public String teacherProgression(@PathVariable Long courseId, Model model) {

        Cours course = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        List<StudentPoints> ranking = studentPointsRepository
                .findByCourseIdOrderByPointsDesc(courseId);

        Map<String, Long> levelCounts = new HashMap<>();
        for (AcademicLevel level : AcademicLevel.values()) {
            levelCounts.put(level.name(), 0L);
        }
        for (Object[] row : studentPointsRepository.countByLevelForCourse(courseId)) {
            levelCounts.put(((AcademicLevel) row[0]).name(), (Long) row[1]);
        }

        List<StudentPoints> struggling = studentPointsRepository
                .findByCourseIdAndAcademicLevel(courseId, AcademicLevel.DECOUVERTE);

        List<StudentBadge> allBadges = studentBadgeRepository.findByCourseId(courseId);
        Map<Long, List<StudentBadge>> badgesByStudent = allBadges.stream()
                .collect(Collectors.groupingBy(sb -> sb.getStudent().getId()));

        model.addAttribute("course", course);
        model.addAttribute("ranking", ranking);
        model.addAttribute("levelCounts", levelCounts);
        model.addAttribute("strugglingStudents", struggling);
        model.addAttribute("badgesByStudent", badgesByStudent);

        return "progression/teacher-progression";
    }

    // -------------------------------------------------------------------------
    // Utilitaire
    // -------------------------------------------------------------------------

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof User user) {
            return user.getId();
        }
        throw new RuntimeException("Impossible d'extraire l'ID utilisateur");
    }
}