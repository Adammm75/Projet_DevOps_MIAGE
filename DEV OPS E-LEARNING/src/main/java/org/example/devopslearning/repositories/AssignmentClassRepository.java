package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AssignmentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentClassRepository extends JpaRepository<AssignmentClass, Long> {

    /**
     * Trouve toutes les affectations d'un devoir
     */
    List<AssignmentClass> findByAssignmentId(Long assignmentId);

    /**
     * Trouve tous les devoirs d'une classe
     */
    List<AssignmentClass> findByClasseId(Long classeId);

    /**
     * Vérifie si un devoir est déjà affecté à une classe
     */
    boolean existsByAssignmentIdAndClasseId(Long assignmentId, Long classeId);

    /**
     * Supprime l'affectation d'un devoir à une classe
     */
    void deleteByAssignmentIdAndClasseId(Long assignmentId, Long classeId);

    /**
     * Compte le nombre de classes auxquelles un devoir est affecté
     */
    long countByAssignmentId(Long assignmentId);

    /**
     * Récupère les IDs des classes d'un devoir
     */
    @Query("SELECT ac.classe.id FROM AssignmentClass ac WHERE ac.assignment.id = :assignmentId")
    List<Long> findClasseIdsByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * Récupère les devoirs d'une classe triés par date limite
     */
    @Query("SELECT ac FROM AssignmentClass ac WHERE ac.classe.id = :classeId ORDER BY ac.assignment.dueDate DESC")
    List<AssignmentClass> findByClasseIdOrderByDueDateDesc(@Param("classeId") Long classeId);
}