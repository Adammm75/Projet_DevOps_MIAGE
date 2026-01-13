package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // ========================================
    // MÉTHODES DE BASE
    // ========================================

    /**
     * ✅ Récupère tous les assignments d'un cours (par objet Cours)
     */
    List<Assignment> findByCourse(Cours course);

    /**
     * ✅ Récupère tous les assignments d'un cours (par ID)
     */
    List<Assignment> findByCourseId(Long courseId);

    /**
     * ✅ NOUVEAU : Récupère tous les devoirs créés par un enseignant
     */
    List<Assignment> findByCreatedBy(User teacher);

    /**
     * ✅ NOUVEAU : Récupère les devoirs d'un cours triés par date limite (du plus récent au plus ancien)
     */
    List<Assignment> findByCourseIdOrderByDueDateDesc(Long courseId);

    /**
     * ✅ NOUVEAU : Récupère les devoirs d'un enseignant triés par date limite
     */
    List<Assignment> findByCreatedByOrderByDueDateDesc(User teacher);

    // ========================================
    // REQUÊTES PERSONNALISÉES (QUERY)
    // ========================================

    /**
     * ✅ NOUVEAU : Récupère les devoirs actifs (date limite future) d'un cours
     */
    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.dueDate > :now")
    List<Assignment> findActiveByCourse(@Param("courseId") Long courseId, @Param("now") Instant now);

    /**
     * ✅ NOUVEAU : Récupère les devoirs terminés (date limite passée) d'un cours
     */
    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.dueDate <= :now")
    List<Assignment> findCompletedByCourse(@Param("courseId") Long courseId, @Param("now") Instant now);

    /**
     * ✅ NOUVEAU : Récupère les devoirs actifs d'un enseignant
     */
    @Query("SELECT a FROM Assignment a WHERE a.createdBy = :teacher AND a.dueDate > :now")
    List<Assignment> findActiveByTeacher(@Param("teacher") User teacher, @Param("now") Instant now);

    /**
     * ✅ NOUVEAU : Compte le nombre de devoirs d'un cours
     */
    long countByCourseId(Long courseId);

    /**
     * ✅ NOUVEAU : Compte le nombre de devoirs créés par un enseignant
     */
    long countByCreatedBy(User teacher);

    /**
     * ✅ NOUVEAU : Supprime tous les devoirs d'un cours (utile lors de la suppression d'un cours)
     */
    void deleteByCourse(Cours course);

    /**
     * ✅ NOUVEAU : Supprime tous les devoirs d'un cours par ID
     */
    void deleteByCourseId(Long courseId);

    /**
     * ✅ NOUVEAU : Vérifie si un devoir existe pour un cours et un titre donnés
     */
    boolean existsByCourseIdAndTitle(Long courseId, String title);

    /**
     * ✅ NOUVEAU : Récupère les devoirs dont la date limite est dans X jours
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate BETWEEN :start AND :end ORDER BY a.dueDate ASC")
    List<Assignment> findAssignmentsDueBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * ✅ NOUVEAU : Récupère les X derniers devoirs créés par un enseignant
     */
    @Query("SELECT a FROM Assignment a WHERE a.createdBy = :teacher ORDER BY a.createdAt DESC")
    List<Assignment> findRecentByTeacher(@Param("teacher") User teacher);

    /**
     * ✅ NOUVEAU : Recherche de devoirs par titre (pour une barre de recherche)
     */
    @Query("SELECT a FROM Assignment a WHERE a.createdBy = :teacher AND LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Assignment> searchByTitleAndTeacher(@Param("keyword") String keyword, @Param("teacher") User teacher);

    /**
     * ✅ NOUVEAU : Compte le nombre de devoirs d'un cours (par objet Cours)
     */
    long countByCourse(Cours course);

    /**
     * ✅ NOUVEAU : Compte le nombre de devoirs corrigés d'un cours
     * (devoirs où au moins une soumission a été notée)
     */
    @Query("SELECT COUNT(DISTINCT a) FROM Assignment a " +
            "JOIN AssignmentSubmission s ON s.assignment = a " +
            "WHERE a.course.id = :courseId AND s.grade IS NOT NULL")
    long countGradedAssignmentsByCourse(@Param("courseId") Long courseId);

}
