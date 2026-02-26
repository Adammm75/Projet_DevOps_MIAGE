package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {

    /**
     * Trouve tous les enseignants d'une classe
     */
    List<TeacherClass> findByClasseId(Long classeId);

    /**
     * Trouve toutes les classes d'un enseignant
     */
    List<TeacherClass> findByTeacherId(Long teacherId);

    /**
     * Vérifie si un enseignant est déjà affecté à une classe
     */
    boolean existsByTeacherIdAndClasseId(Long teacherId, Long classeId);

    /**
     * Supprime une affectation spécifique
     */
    void deleteByTeacherIdAndClasseId(Long teacherId, Long classeId);

    /**
     * Compte le nombre d'enseignants d'une classe
     */
    long countByClasseId(Long classeId);

    /**
     * Récupère les IDs des enseignants d'une classe
     */
    @Query("SELECT tc.teacher.id FROM TeacherClass tc WHERE tc.classe.id = :classeId")
    List<Long> findTeacherIdsByClasseId(@Param("classeId") Long classeId);
}