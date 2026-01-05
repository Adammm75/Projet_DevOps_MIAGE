package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    // ✅ CORRIGÉ : utilise "assignment" et "student" (pas "cours" et "etudiant")
    List<AssignmentSubmission> findByAssignmentAndStudent(
            Assignment assignment,
            User student
    );

    // ✅ Méthodes supplémentaires utiles
    List<AssignmentSubmission> findByStudentId(Long studentId);
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    // ✅ Optionnel : pour récupérer UNE soumission spécifique
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(
            Long assignmentId,
            Long studentId
    );
}