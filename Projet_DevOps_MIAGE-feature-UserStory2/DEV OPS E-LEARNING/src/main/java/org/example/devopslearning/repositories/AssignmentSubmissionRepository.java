package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.course.id = :courseId")
    List<AssignmentSubmission> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT s FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.assignment.course.id = :courseId")
    List<AssignmentSubmission> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NULL")
    List<AssignmentSubmission> findPendingGradesByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NOT NULL")
    List<AssignmentSubmission> findGradedSubmissionsByAssignmentId(@Param("assignmentId") Long assignmentId);

    long countByAssignmentId(Long assignmentId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NOT NULL")
    long countGradedByAssignmentId(@Param("assignmentId") Long assignmentId);
}