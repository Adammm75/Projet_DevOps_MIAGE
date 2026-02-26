package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service de statistiques globales pour l'administrateur.
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final CoursRepository coursRepository;
    private final UserRepository userRepository;
    private final InscriptionsClassRepository inscriptionsRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final QcmTentativeRepository tentativeRepository;
    private final ResourceConsultationRepository consultationRepository;
    private final UserCourseCompletionRepository completionRepository;
    private final CourseResourceRepository resourceRepository;
    private final AssignmentRepository assignmentRepository;

    public Map<String, Object> buildAdminStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // --- Compteurs globaux ---
        long totalCourses = coursRepository.count();
        // ✅ CORRIGÉ : utilise findByRoleName() au lieu de countByRole()
        long totalStudents = userRepository.findByRoleName("ROLE_STUDENT").size();
        long totalTeachers = userRepository.findByRoleName("ROLE_TEACHER").size();
        long totalEnrollments = inscriptionsRepository.count();
        long totalSubmissions = submissionRepository.count();
        long totalQcmAttempts = tentativeRepository.count();
        long totalConsultations = consultationRepository.count();
        // ✅ CORRIGÉ : utilise countAllCompleted() au lieu de countByCompleted(true)
        long totalCompletions = completionRepository.countAllCompleted();
        long totalResources = resourceRepository.count();
        long totalAssignments = assignmentRepository.count();

        stats.put("totalCourses", totalCourses);
        stats.put("totalStudents", totalStudents);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalEnrollments", totalEnrollments);
        stats.put("totalSubmissions", totalSubmissions);
        stats.put("totalQcmAttempts", totalQcmAttempts);
        stats.put("totalConsultations", totalConsultations);
        stats.put("totalCompletions", totalCompletions);
        stats.put("totalResources", totalResources);
        stats.put("totalAssignments", totalAssignments);

        // --- Taux globaux ---
        double completionRate = totalStudents > 0
                ? Math.round((double) totalCompletions / (totalStudents * Math.max(totalCourses, 1)) * 100.0 * 10) / 10.0
                : 0.0;
        stats.put("completionRate", completionRate);

        double submissionRate = totalAssignments > 0 && totalStudents > 0
                ? Math.round((double) totalSubmissions / (totalAssignments * totalStudents) * 100.0 * 10) / 10.0
                : 0.0;
        stats.put("submissionRate", Math.min(submissionRate, 100.0));

        // --- Étudiants actifs ---
        // ✅ CORRIGÉ : utilise countActiveStudents() défini dans UserRepository
        long activeStudents = userRepository.countActiveStudents();
        long inactiveStudents = totalStudents - activeStudents;
        stats.put("activeStudents", activeStudents);
        stats.put("inactiveStudents", inactiveStudents);
        double activeRate = totalStudents > 0
                ? Math.round((double) activeStudents / totalStudents * 100.0 * 10) / 10.0
                : 0.0;
        stats.put("activeRate", activeRate);

        return stats;
    }
}