package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.StudentBadge;
import org.example.devopslearning.entities.StudentPoints;
import org.example.devopslearning.entities.StudentPoints.AcademicLevel;
import org.example.devopslearning.repositories.StudentBadgeRepository;
import org.example.devopslearning.repositories.StudentPointsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST exposant les badges et niveaux académiques.
 *
 * Routes :
 *   GET /api/badges/my/{courseId}          → badges + niveau de l'étudiant connecté
 *   GET /api/badges/course/{courseId}/stats → stats groupe pour l'enseignant
 *   GET /api/badges/course/{courseId}/struggling → étudiants en difficulté (niveau DÉCOUVERTE)
 */
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class StudentBadgeController {

    private final StudentBadgeRepository studentBadgeRepository;
    private final StudentPointsRepository studentPointsRepository;

    // -------------------------------------------------------------------------
    //  CÔTÉ ÉTUDIANT
    // -------------------------------------------------------------------------

    /**
     * Retourne les badges et le niveau de l'étudiant connecté pour un cours.
     */
    @GetMapping("/my/{courseId}")
    public ResponseEntity<Map<String, Object>> getMyProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long studentId = extractUserId(userDetails);

        StudentPoints sp = studentPointsRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        List<StudentBadge> badges = studentBadgeRepository.findByStudentIdAndCourseId(studentId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("totalPoints",    sp != null ? sp.getTotalPoints() : 0);
        response.put("academicLevel",  sp != null ? sp.getAcademicLevel() : AcademicLevel.DECOUVERTE);
        response.put("badges", badges.stream().map(b -> Map.of(
                "code",    b.getBadge().getCode(),
                "label",   b.getBadge().getLabel(),
                "iconUrl", b.getBadge().getIconUrl() != null ? b.getBadge().getIconUrl() : "",
                "earnedAt", b.getEarnedAt().toString()
        )).toList());

        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    //  CÔTÉ ENSEIGNANT
    // -------------------------------------------------------------------------

    /**
     * Statistiques de progression du groupe (nb étudiants par niveau).
     */
    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats(@PathVariable Long courseId) {

        List<Object[]> levelCounts = studentPointsRepository.countByLevelForCourse(courseId);
        List<StudentPoints> allPoints = studentPointsRepository.findByCourseIdOrderByPointsDesc(courseId);

        // Compteurs par niveau
        Map<String, Long> levelDistribution = new HashMap<>();
        for (AcademicLevel level : AcademicLevel.values()) {
            levelDistribution.put(level.name(), 0L);
        }
        for (Object[] row : levelCounts) {
            levelDistribution.put(((AcademicLevel) row[0]).name(), (Long) row[1]);
        }

        // Classement complet
        List<Map<String, Object>> ranking = allPoints.stream().map(sp -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId",     sp.getStudent().getId());
            entry.put("studentName",   sp.getStudent().getFirstName() + " " + sp.getStudent().getLastName());
            entry.put("totalPoints",   sp.getTotalPoints());
            entry.put("academicLevel", sp.getAcademicLevel().name());
            return entry;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("levelDistribution", levelDistribution);
        response.put("ranking", ranking);
        response.put("totalStudents", allPoints.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Liste des étudiants en difficulté (niveau DÉCOUVERTE) pour un cours.
     */
    @GetMapping("/course/{courseId}/struggling")
    public ResponseEntity<List<Map<String, Object>>> getStrugglingStudents(@PathVariable Long courseId) {

        List<StudentPoints> struggling = studentPointsRepository
                .findByCourseIdAndAcademicLevel(courseId, AcademicLevel.DECOUVERTE);

        List<Map<String, Object>> result = struggling.stream().map(sp -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId",   sp.getStudent().getId());
            entry.put("studentName", sp.getStudent().getFirstName() + " " + sp.getStudent().getLastName());
            entry.put("totalPoints", sp.getTotalPoints());
            return entry;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // -------------------------------------------------------------------------
    //  Utilitaire
    // -------------------------------------------------------------------------

    private Long extractUserId(UserDetails userDetails) {
        // Adapter selon votre implémentation de UserDetails (CustomUserDetailsService)
        if (userDetails instanceof org.example.devopslearning.entities.User user) {
            return user.getId();
        }
        throw new RuntimeException("Impossible d'extraire l'ID utilisateur");
    }
}