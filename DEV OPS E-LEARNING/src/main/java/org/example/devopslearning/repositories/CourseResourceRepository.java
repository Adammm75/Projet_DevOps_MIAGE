package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.RessourceCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseResourceRepository extends JpaRepository<RessourceCours, Long> {

    /**
     * Trouve toutes les ressources d'un cours
     */
    List<RessourceCours> findByCourse(Cours cours);

    /**
     * ✅ AJOUTÉ : Compte le nombre de ressources d'un cours
     */
    long countByCourse(Cours cours);

    /**
     * ✅ BONUS : Trouve les ressources d'un cours triées par date
     */
    List<RessourceCours> findByCourseOrderByCreatedAtDesc(Cours cours);

    /**
     * ✅ BONUS : Trouve les ressources par type
     */
    List<RessourceCours> findByCourseAndType(Cours cours, String type);
}