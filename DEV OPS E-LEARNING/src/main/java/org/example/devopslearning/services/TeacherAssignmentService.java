package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AssignmentRepository;
import org.example.devopslearning.repositories.AssignmentSubmissionRepository;
import org.example.devopslearning.repositories.CoursRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherAssignmentService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CoursRepository coursRepository;

    // ========================================
    // GESTION DES DEVOIRS
    // ========================================

    /**
     * ✅ Liste tous les devoirs d'un enseignant
     */
    public List<Assignment> listByTeacher(User teacher) {
        return assignmentRepository.findByCreatedBy(teacher);
    }

    /**
     * ✅ Liste tous les devoirs d'un cours
     */
    public List<Assignment> listByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    /**
     * ✅ Récupère un devoir par son ID
     */
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable avec l'ID: " + assignmentId));
    }

    /**
     * ✅ Crée un nouveau devoir AVEC createdBy
     */
    public Assignment createAssignment(Long courseId, String title, String description,
                                       LocalDateTime dueDate, BigDecimal maxGrade, User teacher) {
        Cours course = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable avec l'ID: " + courseId));

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setCreatedBy(teacher);

        // Convertir LocalDateTime en Instant
        if (dueDate != null) {
            assignment.setDueDate(dueDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        assignment.setMaxGrade(maxGrade != null ? maxGrade : BigDecimal.valueOf(20.00));
        assignment.setCreatedAt(Instant.now());

        return assignmentRepository.save(assignment);
    }

    /**
     * ✅ Met à jour un devoir existant
     */
    /**
     * ✅ Met à jour un devoir existant
     */
    @Transactional
    public Assignment updateAssignment(Long assignmentId, String title, String description,
                                       LocalDateTime dueDate, BigDecimal maxGrade) {
        Assignment assignment = getAssignmentById(assignmentId);

        assignment.setTitle(title);
        assignment.setDescription(description);

        if (dueDate != null) {
            assignment.setDueDate(dueDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        assignment.setMaxGrade(maxGrade != null ? maxGrade : BigDecimal.valueOf(20.00));


        return assignmentRepository.save(assignment);
    }



    /**
     * ✅ Supprime un devoir (et ses soumissions en cascade)
     */
    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);
        assignmentRepository.delete(assignment);
    }

    // ========================================
    // GESTION DES SOUMISSIONS
    // ========================================

    /**
     * ✅ Liste toutes les soumissions d'un devoir
     */
    public List<AssignmentSubmission> listSubmissions(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    /**
     * ✅ Récupère les soumissions d'un devoir (alias)
     */
    public List<AssignmentSubmission> getSubmissions(Long assignmentId) {
        return listSubmissions(assignmentId);
    }

    /**
     * ✅ Récupère une soumission par son ID
     */
    public AssignmentSubmission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable avec l'ID: " + submissionId));
    }

    /**
     * ✅ Note une soumission AVEC l'enseignant qui corrige
     */
    public void gradeSubmission(Long submissionId, BigDecimal grade, String feedback, User teacher) {
        AssignmentSubmission sub = getSubmissionById(submissionId);

        sub.setGrade(grade);
        sub.setFeedback(feedback);
        sub.setGradedAt(Instant.now());
        sub.setGradedBy(teacher);

        submissionRepository.save(sub);
    }

    /**
     * ✅ Note une soumission avec BigDecimal SANS enseignant (pour compatibilité)
     */
    public void gradeSubmission(Long submissionId, BigDecimal grade, String feedback) {
        AssignmentSubmission sub = getSubmissionById(submissionId);

        sub.setGrade(grade);
        sub.setFeedback(feedback);
        sub.setGradedAt(Instant.now());

        submissionRepository.save(sub);
    }

    /**
     * ✅ Note une soumission avec Double (pour compatibilité)
     */
    public void gradeSubmission(Long submissionId, Double grade, String feedback) {
        gradeSubmission(submissionId, BigDecimal.valueOf(grade), feedback);
    }

    /**
     * ✅ Récupère l'ID du devoir associé à une soumission
     */
    public Long getAssignmentId(Long submissionId) {
        AssignmentSubmission submission = getSubmissionById(submissionId);
        return submission.getAssignment().getId();
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * ✅ Compte le nombre de soumissions non notées pour un enseignant
     */
    public long countPendingSubmissionsByTeacher(User teacher) {
        List<Assignment> assignments = listByTeacher(teacher);
        return assignments.stream()
                .flatMap(a -> listSubmissions(a.getId()).stream())
                .filter(s -> s.getGrade() == null && s.getSubmittedAt() != null)
                .count();
    }

    /**
     * ✅ Compte le nombre de soumissions d'un devoir
     */
    public int getSubmissionCount(Long assignmentId) {
        return listSubmissions(assignmentId).size();
    }

    /**
     * ✅ Compte le nombre de soumissions corrigées
     */
    public int getGradedCount(Long assignmentId) {
        return (int) listSubmissions(assignmentId).stream()
                .filter(s -> s.getGrade() != null)
                .count();
    }

    /**
     * ✅ Calcule la moyenne des notes d'un devoir
     */
    public double getAverageGrade(Long assignmentId) {
        List<AssignmentSubmission> graded = listSubmissions(assignmentId).stream()
                .filter(s -> s.getGrade() != null)
                .toList();

        if (graded.isEmpty()) return 0.0;

        return graded.stream()
                .mapToDouble(s -> s.getGrade().doubleValue())
                .average()
                .orElse(0.0);
    }
}
