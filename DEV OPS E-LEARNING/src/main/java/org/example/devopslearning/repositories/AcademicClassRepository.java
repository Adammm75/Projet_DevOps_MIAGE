package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {

    /**
     * Compte le nombre de classes pour un parcours donné
     */
    long countByParcoursId(Long parcoursId);  // ✅ CORRIGÉ : parcours au lieu de parcour
}