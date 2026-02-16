package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Qcm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QcmRepository extends JpaRepository<Qcm, Long> {

    /**
     * Récupère tous les QCM publiés d'un cours
     */
    List<Qcm> findByCoursIdAndPublieTrue(Long coursId);

    /**
     * Récupère tous les QCM d'un cours avec un statut de publication donné
     */
    List<Qcm> findByCoursIdAndPublie(Long coursId, Boolean publie);

    /**
     * Récupère tous les QCM d'un cours (peu importe le statut)
     */
    List<Qcm> findByCoursId(Long coursId);

}