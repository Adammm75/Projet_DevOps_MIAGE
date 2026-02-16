package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseResourceRepository extends JpaRepository<CourseRessource, Long> {

    /**
     * Trouve toutes les ressources d'un cours
     */
    List<CourseRessource> findByCourse(Cours cours);

    /**
     * ✅ AJOUTÉ : Compte le nombre de ressources d'un cours
     */
    long countByCourse(Cours cours);

    /**
     * ✅ BONUS : Trouve les ressources d'un cours triées par date
     */
    List<CourseRessource> findByCourseOrderByCreatedAtDesc(Cours cours);

    /**
     * ✅ BONUS : Trouve les ressources par type
     */
    List<CourseRessource> findByCourseAndType(Cours cours, String type);
}