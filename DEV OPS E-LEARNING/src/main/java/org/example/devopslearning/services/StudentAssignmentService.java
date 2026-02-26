package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🎓 SERVICE DEVOIRS POUR ÉTUDIANTS
 */
@Service
@RequiredArgsConstructor
public class StudentAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentClassRepository assignmentClassRepository;
    private final InscriptionsClassRepository inscriptionsClassRepository;
    private final S3StorageService s3StorageService;

    // ========================================
    // RÉCUPÉRATION DES DEVOIRS
    // ========================================

    /**
     * Récupère TOUS les devoirs accessibles par un étudiant
     * (basé sur ses inscriptions aux classes)
     */
    public List<Assignment> getAssignmentsForStudent(Long studentId) {
        // 1. Récupérer les classes de l'étudiant
        List<InscriptionsClass> inscriptions = inscriptionsClassRepository.findByEtudiantId(studentId);
        List<Long> classeIds = inscriptions.stream()
                .map(i -> i.getClasse().getId())
                .collect(Collectors.toList());

        if (classeIds.isEmpty()) {
            return List.of();
        }

        // 2. Récupérer les devoirs affectés à ces classes
        List<AssignmentClass> assignmentClasses = classeIds.stream()
                .flatMap(classId -> assignmentClassRepository.findByClasseId(classId).stream())
                .collect(Collectors.toList());

        // 3. Extraire les devoirs uniques
        return assignmentClasses.stream()
                .map(AssignmentClass::getAssignment)
                .distinct()
                .sorted((a1, a2) -> a2.getDueDate().compareTo(a1.getDueDate()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les devoirs d'un cours pour un étudiant
     */
    public List<Assignment> getAssignmentsForStudentByCourse(Long studentId, Long courseId) {
        List<Assignment> allAssignments = getAssignmentsForStudent(studentId);

        return allAssignments.stream()
                .filter(a -> a.getCourse().getId().equals(courseId))
                .collect(Collectors.toList());
    }

    /**
     * Récupère un devoir par ID
     */
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));
    }

    // ========================================
    // GESTION DES SOUMISSIONS
    // ========================================

    /**
     * Récupère la soumission d'un étudiant pour un devoir
     */
    public AssignmentSubmission getSubmission(Long studentId, Long assignmentId) {
        return submissionRepository.findByStudentIdAndAssignmentId(studentId, assignmentId)
                .orElse(null);
    }

    /**
     * Récupère une soumission par ID
     */
    public AssignmentSubmission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable"));
    }

    /**
     * Soumet un devoir
     */
    @Transactional
    public AssignmentSubmission submitAssignment(Long assignmentId, User student, MultipartFile file)
            throws IOException {

        Assignment assignment = getAssignmentById(assignmentId);

        // Vérifier que l'étudiant n'a pas déjà soumis
        AssignmentSubmission existing = getSubmission(student.getId(), assignmentId);
        if (existing != null) {
            throw new RuntimeException("Vous avez déjà soumis ce devoir. Utilisez 're-soumettre' si vous voulez modifier.");
        }

        // Upload du fichier
        String fileUrl = s3StorageService.uploadFileInFolder(file, "assignments");

        // Créer la soumission
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setSubmittedAt(Instant.now());

        return submissionRepository.save(submission);
    }

    /**
     * Re-soumet un devoir (remplace la soumission existante)
     */
    @Transactional
    public AssignmentSubmission resubmitAssignment(Long assignmentId, Long studentId, MultipartFile file)
            throws IOException {

        AssignmentSubmission existing = getSubmission(studentId, assignmentId);
        if (existing == null) {
            throw new RuntimeException("Aucune soumission existante trouvée");
        }

        // Vérifier que le devoir n'est pas encore corrigé
        if (existing.getGrade() != null) {
            throw new RuntimeException("Impossible de re-soumettre : le devoir est déjà corrigé");
        }

        // Upload du nouveau fichier
        String fileUrl = s3StorageService.uploadFileInFolder(file, "assignments");

        // Mettre à jour la soumission
        existing.setFileUrl(fileUrl);
        existing.setSubmittedAt(Instant.now());

        return submissionRepository.save(existing);
    }

    // ========================================
    // VÉRIFICATIONS D'ACCÈS
    // ========================================

    /**
     * Vérifie si un étudiant a accès à un devoir
     */
    public boolean canAccessAssignment(Long studentId, Long assignmentId) {
        List<Assignment> accessible = getAssignmentsForStudent(studentId);
        return accessible.stream().anyMatch(a -> a.getId().equals(assignmentId));
    }

    /**
     * Vérifie si un étudiant a déjà soumis un devoir
     */
    public boolean hasSubmitted(Long studentId, Long assignmentId) {
        return submissionRepository.findByStudentIdAndAssignmentId(studentId, assignmentId).isPresent();
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Compte les devoirs à rendre pour un étudiant
     */
    public long countPendingAssignments(Long studentId) {
        List<Assignment> assignments = getAssignmentsForStudent(studentId);
        List<AssignmentSubmission> submissions = submissionRepository.findByStudentId(studentId);

        Instant now = Instant.now();

        return assignments.stream()
                .filter(a -> a.getDueDate().isAfter(now))
                .filter(a -> submissions.stream().noneMatch(s -> s.getAssignment().getId().equals(a.getId())))
                .count();
    }

    /**
     * Compte les devoirs en retard pour un étudiant
     */
    public long countLateAssignments(Long studentId) {
        List<Assignment> assignments = getAssignmentsForStudent(studentId);
        List<AssignmentSubmission> submissions = submissionRepository.findByStudentId(studentId);

        Instant now = Instant.now();

        return assignments.stream()
                .filter(a -> a.getDueDate().isBefore(now))
                .filter(a -> submissions.stream().noneMatch(s -> s.getAssignment().getId().equals(a.getId())))
                .count();
    }

    /**
     * Compte les devoirs soumis mais non corrigés
     */
    public long countUngraded(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .filter(s -> s.getGrade() == null)
                .count();
    }

    /**
     * Compte les devoirs corrigés pour un étudiant
     */
    public long countGradedAssignments(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .filter(s -> s.getGrade() != null)
                .count();
    }
}