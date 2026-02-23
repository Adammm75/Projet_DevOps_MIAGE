package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    // ========================================
    // RÉCUPÉRATION PAR ÉTUDIANT
    // ========================================

    /**
     * Trouve toutes les soumissions d'un étudiant
     */
    List<AssignmentSubmission> findByStudentId(Long studentId);

    /**
     * Trouve les soumissions d'un étudiant triées par date
     */
    List<AssignmentSubmission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    // ========================================
    // RÉCUPÉRATION PAR DEVOIR
    // ========================================

    /**
     * Trouve toutes les soumissions pour un devoir
     */
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    /**
     * Trouve les soumissions d'un devoir triées par date
     */
    List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    // ========================================
    // RÉCUPÉRATION SPÉCIFIQUE
    // ========================================

    /**
     * Trouve la soumission d'un étudiant pour un devoir spécifique (par ID)
     */
    Optional<AssignmentSubmission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    /**
     * ⭐ AJOUTÉ - Trouve les soumissions par Assignment et Student (objets)
     */
    List<AssignmentSubmission> findByAssignmentAndStudent(Assignment assignment, User student);

    // ========================================
    // RÉCUPÉRATION PAR STATUT (CORRIGÉ OU NON)
    // ========================================

    /**
     * Trouve les soumissions en attente de correction pour un devoir
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NULL")
    List<AssignmentSubmission> findPendingGradesByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * Trouve les soumissions déjà corrigées pour un devoir
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NOT NULL")
    List<AssignmentSubmission> findGradedByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * Trouve les soumissions en attente de correction d'un étudiant
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.grade IS NULL")
    List<AssignmentSubmission> findPendingGradesByStudentId(@Param("studentId") Long studentId);

    /**
     * Trouve les soumissions corrigées d'un étudiant
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.grade IS NOT NULL")
    List<AssignmentSubmission> findGradedByStudentId(@Param("studentId") Long studentId);

    // ========================================
    // COMPTEURS
    // ========================================

    /**
     * Compte le nombre de soumissions d'un étudiant
     */
    long countByStudentId(Long studentId);

    /**
     * Compte le nombre de soumissions pour un devoir
     */
    long countByAssignmentId(Long assignmentId);

    /**
     * Compte les soumissions corrigées pour un devoir
     */
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NOT NULL")
    long countGradedByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * Compte les soumissions corrigées d'un étudiant
     */
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.grade IS NOT NULL")
    long countGradedByStudentId(@Param("studentId") Long studentId);

    /**
     * Compte les soumissions en attente pour un devoir
     */
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.grade IS NULL")
    long countPendingByAssignmentId(@Param("assignmentId") Long assignmentId);

    // ========================================
    // VÉRIFICATIONS
    // ========================================

    /**
     * Vérifie si un étudiant a soumis un devoir
     */
    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    // ========================================
    // SUPPRESSION
    // ========================================

    /**
     * Supprime toutes les soumissions d'un devoir
     */
    void deleteByAssignmentId(Long assignmentId);

    /**
     * Supprime la soumission d'un étudiant pour un devoir
     */
    void deleteByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
}