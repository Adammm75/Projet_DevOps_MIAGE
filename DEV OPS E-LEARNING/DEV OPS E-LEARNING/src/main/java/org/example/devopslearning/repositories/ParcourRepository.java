package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Parcour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcourRepository extends JpaRepository<Parcour, Long> {

    /**
     * Compte le nombre de parcours pour une filière donnée
     */
    long countByFiliereId(Long filiereId);
}