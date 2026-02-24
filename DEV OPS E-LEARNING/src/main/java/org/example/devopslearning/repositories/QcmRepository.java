package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Qcm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ✅ Repository QCM complet avec toutes les méthodes de recherche
 */
public interface QcmRepository extends JpaRepository<Qcm, Long> {

    // ========================================
    // RECHERCHES PAR COURS
    // ========================================

    /**
     * Trouve tous les QCM d'un cours (publiés et brouillons)
     */
    List<Qcm> findByCoursIdOrderByDateCreationDesc(Long coursId);

    /**
     * Trouve uniquement les QCM publiés d'un cours
     */
    List<Qcm> findByCoursIdAndPublieTrue(Long coursId);

    /**
     * Trouve uniquement les brouillons d'un cours
     */
    List<Qcm> findByCoursIdAndPublieFalse(Long coursId);

    /**
     * Trouve tous les QCM d'un cours (peu importe le statut)
     */
    List<Qcm> findByCoursId(Long coursId);

    /**
     * Trouve tous les QCM d'un cours avec un statut donné
     */
    List<Qcm> findByCoursIdAndPublie(Long coursId, Boolean publie);

    // ========================================
    // RECHERCHES PAR ENSEIGNANT
    // ========================================

    /**
     * Trouve tous les QCM créés par un enseignant
     */
    List<Qcm> findByCreeeParIdOrderByDateCreationDesc(Long enseignantId);

    /**
     * Trouve les QCM publiés d'un enseignant
     */
    List<Qcm> findByCreeeParIdAndPublieTrue(Long enseignantId);

    /**
     * Trouve les brouillons d'un enseignant
     */
    List<Qcm> findByCreeeParIdAndPublieFalse(Long enseignantId);

    // ========================================
    // RECHERCHES COMBINÉES
    // ========================================

    /**
     * Trouve les QCM d'un cours créés par un enseignant
     */
    List<Qcm> findByCoursIdAndCreeeParIdOrderByDateCreationDesc(Long coursId, Long enseignantId);

    /**
     * Trouve les QCM publiés d'un cours créés par un enseignant
     */
    List<Qcm> findByCoursIdAndCreeeParIdAndPublieTrue(Long coursId, Long enseignantId);

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Compte le nombre total de QCM d'un enseignant
     */
    long countByCreeeParId(Long enseignantId);

    /**
     * Compte les QCM publiés d'un enseignant
     */
    long countByCreeeParIdAndPublieTrue(Long enseignantId);

    /**
     * Compte les brouillons d'un enseignant
     */
    long countByCreeeParIdAndPublieFalse(Long enseignantId);

    /**
     * Compte les QCM d'un cours
     */
    long countByCoursId(Long coursId);

    // ========================================
    // REQUÊTES PERSONNALISÉES
    // ========================================

    /**
     * Recherche de QCM par titre (partiel, insensible à la casse)
     */
    @Query("SELECT q FROM Qcm q WHERE q.creeePar.id = :enseignantId " +
            "AND LOWER(q.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY q.dateCreation DESC")
    List<Qcm> searchByTitre(@Param("enseignantId") Long enseignantId,
                            @Param("keyword") String keyword);

    /**
     * Trouve les QCM récents d'un enseignant (derniers 30 jours)
     */
    @Query("SELECT q FROM Qcm q WHERE q.creeePar.id = :enseignantId " +
            "AND q.dateCreation > :dateLimit " +
            "ORDER BY q.dateCreation DESC")
    List<Qcm> findRecentByEnseignant(@Param("enseignantId") Long enseignantId,
                                     @Param("dateLimit") java.time.Instant dateLimit);
}