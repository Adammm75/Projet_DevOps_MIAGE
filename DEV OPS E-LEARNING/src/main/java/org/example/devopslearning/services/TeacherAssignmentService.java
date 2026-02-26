package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AssignmentRepository;
import org.example.devopslearning.repositories.AssignmentSubmissionRepository;
import org.example.devopslearning.repositories.CoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CoursRepository coursRepository;

    // ========================================
    // MÉTHODES DE BASE - CRUD
    // ========================================

    /**
     * Liste tous les devoirs d'un cours
     */
    public List<Assignment> listByCourse(Long courseId) {
        return assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);
    }

    /**
     * Liste tous les devoirs d'un enseignant
     */
    public List<Assignment> listByTeacher(User teacher) {
        return assignmentRepository.findByCreatedBy(teacher);
    }

    /**
     * Récupère un devoir par son ID
     */
    public Assignment getById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));
    }

    /**
     * Récupère un devoir par son ID (alias)
     */
    public Assignment getAssignmentById(Long assignmentId) {
        return getById(assignmentId);
    }

    /**
     * Crée un nouveau devoir
     */
    @Transactional
    public Assignment create(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    /**
     * Crée un nouveau devoir avec paramètres
     */
    @Transactional
    public Assignment createAssignment(Long courseId, String title, String description,
                                       LocalDateTime dueDate, BigDecimal maxGrade, User teacher) {
        Cours course = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate.atZone(ZoneId.systemDefault()).toInstant());
        assignment.setMaxGrade(maxGrade != null ? maxGrade : BigDecimal.valueOf(20));
        assignment.setCreatedBy(teacher);  // ✅ AJOUTÉ
        assignment.setCreatedAt(Instant.now());

        return assignmentRepository.save(assignment);
    }

    /**
     * Met à jour un devoir
     */
    @Transactional
    public Assignment update(Long id, Assignment updatedAssignment) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));

        assignment.setTitle(updatedAssignment.getTitle());
        assignment.setDescription(updatedAssignment.getDescription());
        assignment.setDueDate(updatedAssignment.getDueDate());
        assignment.setMaxGrade(updatedAssignment.getMaxGrade());

        return assignmentRepository.save(assignment);
    }

    /**
     * Met à jour un devoir avec paramètres
     */
    @Transactional
    public Assignment updateAssignment(Long assignmentId, String title, String description,
                                       LocalDateTime dueDate, BigDecimal maxGrade) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));

        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate.atZone(ZoneId.systemDefault()).toInstant());
        assignment.setMaxGrade(maxGrade);

        return assignmentRepository.save(assignment);
    }

    /**
     * Supprime un devoir
     */
    @Transactional
    public void delete(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    /**
     * Supprime un devoir (alias)
     */
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        delete(assignmentId);
    }

    // ========================================
    // GESTION DES SOUMISSIONS
    // ========================================

    /**
     * Récupère toutes les soumissions d'un devoir
     */
    public List<AssignmentSubmission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    /**
     * Liste les soumissions d'un devoir (alias)
     */
    public List<AssignmentSubmission> listSubmissions(Long assignmentId) {
        return getSubmissionsByAssignment(assignmentId);
    }

    /**
     * Récupère une soumission par son ID
     */
    public AssignmentSubmission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable"));
    }

    /**
     * Compte le nombre de soumissions d'un devoir
     */
    public long countSubmissions(Long assignmentId) {
        return submissionRepository.countByAssignmentId(assignmentId);
    }

    /**
     * Compte le nombre de soumissions corrigées d'un devoir
     */
    public long countGradedSubmissions(Long assignmentId) {
        return submissionRepository.countGradedByAssignmentId(assignmentId);
    }

    /**
     * Compte le nombre de soumissions en attente de correction pour un enseignant
     */
    public long countPendingSubmissionsByTeacher(User teacher) {
        List<Assignment> assignments = assignmentRepository.findByCreatedBy(teacher);

        return assignments.stream()
                .flatMap(a -> submissionRepository.findPendingGradesByAssignmentId(a.getId()).stream())
                .count();
    }

    // ========================================
    // CORRECTION DES DEVOIRS
    // ========================================

    /**
     * Enregistre la note d'une soumission
     */
    @Transactional
    public void gradeSubmission(Long submissionId, BigDecimal grade, String feedback, User gradedBy) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable"));

        submission.setGrade(grade);
        submission.setFeedback(feedback);
        submission.setGradedAt(Instant.now());
        submission.setGradedBy(gradedBy);

        submissionRepository.save(submission);
    }

    /**
     * Met à jour une note existante
     */
    @Transactional
    public void updateGrade(Long submissionId, BigDecimal grade, String feedback) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable"));

        submission.setGrade(grade);
        submission.setFeedback(feedback);
        submission.setGradedAt(Instant.now());

        submissionRepository.save(submission);
    }

    /**
     * Supprime une note (remet à null)
     */
    @Transactional
    public void deleteGrade(Long submissionId) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable"));

        submission.setGrade(null);
        submission.setFeedback(null);
        submission.setGradedAt(null);
        submission.setGradedBy(null);

        submissionRepository.save(submission);
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Récupère les statistiques de tous les devoirs d'un cours
     */
    public List<AssignmentStats> getAssignmentsStatsForCourse(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);

        return assignments.stream()
                .map(this::getAssignmentStats)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques d'un devoir
     */
    public AssignmentStats getAssignmentStats(Assignment assignment) {
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignment.getId());

        long totalSubmissions = submissions.size();
        long gradedSubmissions = submissions.stream()
                .filter(s -> s.getGrade() != null)
                .count();
        long pendingSubmissions = totalSubmissions - gradedSubmissions;

        // Calculer la moyenne des notes
        BigDecimal averageGrade = BigDecimal.ZERO;
        if (gradedSubmissions > 0) {
            List<BigDecimal> grades = submissions.stream()
                    .filter(s -> s.getGrade() != null)
                    .map(AssignmentSubmission::getGrade)
                    .collect(Collectors.toList());

            BigDecimal sum = grades.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageGrade = sum.divide(BigDecimal.valueOf(gradedSubmissions), 2, RoundingMode.HALF_UP);
        }

        // Normaliser la moyenne sur 20
        BigDecimal normalizedAverage = BigDecimal.ZERO;
        if (averageGrade.compareTo(BigDecimal.ZERO) > 0 && assignment.getMaxGrade().compareTo(BigDecimal.ZERO) > 0) {
            normalizedAverage = averageGrade
                    .divide(assignment.getMaxGrade(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(20));
        }

        return AssignmentStats.builder()
                .assignment(assignment)
                .totalSubmissions(totalSubmissions)
                .gradedSubmissions(gradedSubmissions)
                .pendingSubmissions(pendingSubmissions)
                .averageGrade(averageGrade)
                .normalizedAverage(normalizedAverage)
                .build();
    }

    /**
     * Récupère toutes les soumissions non corrigées d'un enseignant
     */
    public List<AssignmentSubmission> getPendingSubmissionsForTeacher(User teacher) {
        List<Assignment> assignments = assignmentRepository.findByCreatedBy(teacher);

        return assignments.stream()
                .flatMap(a -> submissionRepository.findPendingGradesByAssignmentId(a.getId()).stream())
                .collect(Collectors.toList());
    }

    /**
     * Calcule le pourcentage de devoirs corrigés pour un cours
     */
    public double getGradingProgressForCourse(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);

        long totalSubmissions = 0;
        long gradedSubmissions = 0;

        for (Assignment assignment : assignments) {
            totalSubmissions += submissionRepository.countByAssignmentId(assignment.getId());
            gradedSubmissions += submissionRepository.countGradedByAssignmentId(assignment.getId());
        }

        if (totalSubmissions == 0) return 0.0;

        return (double) gradedSubmissions / totalSubmissions * 100;
    }

    // ========================================
    // DTO - CLASSES INTERNES
    // ========================================

    /**
     * DTO pour les statistiques d'un devoir
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignmentStats {
        private Assignment assignment;
        private long totalSubmissions;
        private long gradedSubmissions;
        private long pendingSubmissions;
        private BigDecimal averageGrade;
        private BigDecimal normalizedAverage;

        public double getGradingPercentage() {
            if (totalSubmissions == 0) return 0.0;
            return (double) gradedSubmissions / totalSubmissions * 100;
        }

        public boolean isFullyGraded() {
            return totalSubmissions > 0 && pendingSubmissions == 0;
        }
    }
}