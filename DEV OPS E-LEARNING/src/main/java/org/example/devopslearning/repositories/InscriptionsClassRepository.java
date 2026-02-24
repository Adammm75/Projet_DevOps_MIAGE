package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.InscriptionsClass;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionsClassRepository extends JpaRepository<InscriptionsClass, Long> {

    // ========================================
    // RÉCUPÉRATION PAR CLASSE
    // ========================================

    /**
     * Trouve toutes les inscriptions d'une classe
     */
    List<InscriptionsClass> findByClasseId(Long classeId);

    /**
     * Trouve les inscriptions d'une classe par statut
     */
    List<InscriptionsClass> findByClasseIdAndStatut(Long classeId, String statut);

    /**
     * Trouve les inscriptions d'une classe triées par date
     */
    List<InscriptionsClass> findByClasseIdOrderByDateInscription(Long classeId);

    // ========================================
    // RÉCUPÉRATION PAR ÉTUDIANT
    // ========================================

    /**
     * Trouve toutes les inscriptions d'un étudiant
     */
    List<InscriptionsClass> findByEtudiantId(Long etudiantId);

    /**
     * Trouve les inscriptions d'un étudiant par statut
     */
    List<InscriptionsClass> findByEtudiantIdAndStatut(Long etudiantId, String statut);

    /**
     * Trouve les inscriptions d'un étudiant triées par date
     */
    List<InscriptionsClass> findByEtudiantIdOrderByDateInscription(Long etudiantId);

    // ========================================
    // RÉCUPÉRATION SPÉCIFIQUE
    // ========================================

    /**
     * Trouve une inscription spécifique (étudiant + classe)
     */
    Optional<InscriptionsClass> findByClasseIdAndEtudiantId(Long classeId, Long etudiantId);

    // ========================================
    // VÉRIFICATIONS
    // ========================================

    /**
     * Vérifie si un étudiant est inscrit dans une classe
     */
    boolean existsByClasseIdAndEtudiantId(Long classeId, Long etudiantId);

    // ========================================
    // COMPTEURS
    // ========================================

    /**
     * Compte le nombre total d'inscriptions d'une classe
     */
    long countByClasseId(Long classeId);

    /**
     * Compte les étudiants actifs dans une classe
     */
    long countByClasseIdAndStatut(Long classeId, String statut);

    /**
     * Compte le nombre total d'inscriptions d'un étudiant
     */
    long countByEtudiantId(Long etudiantId);

    // ========================================
    // REQUÊTES PERSONNALISÉES
    // ========================================

    /**
     * Trouve les étudiants qui ne sont PAS inscrits dans une classe
     */
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT ic.etudiant.id FROM InscriptionsClass ic WHERE ic.classe.id = :classeId) " +
            "AND EXISTS (SELECT ur FROM u.userRoles ur WHERE ur.role.name = 'ROLE_STUDENT')")
    List<User> findStudentsNotInClass(@Param("classeId") Long classeId);

    /**
     * Trouve tous les étudiants d'une classe (directement en User)
     */
    @Query("SELECT ic.etudiant FROM InscriptionsClass ic WHERE ic.classe.id = :classeId AND ic.statut = :statut")
    List<User> findStudentsByClasseIdAndStatut(@Param("classeId") Long classeId, @Param("statut") String statut);

    /**
     * Récupère les IDs des étudiants d'une classe
     */
    @Query("SELECT ic.etudiant.id FROM InscriptionsClass ic WHERE ic.classe.id = :classeId")
    List<Long> findStudentIdsByClasseId(@Param("classeId") Long classeId);

    /**
     * Récupère les IDs des étudiants actifs d'une classe
     */
    @Query("SELECT ic.etudiant.id FROM InscriptionsClass ic WHERE ic.classe.id = :classeId AND ic.statut = 'ACTIVE'")
    List<Long> findActiveStudentIdsByClasseId(@Param("classeId") Long classeId);

    /**
     * Trouve les inscriptions par année universitaire
     */
    @Query("SELECT ic FROM InscriptionsClass ic WHERE ic.etudiant.id = :etudiantId AND ic.classe.anneeUniversitaire = :annee")
    List<InscriptionsClass> findByEtudiantAndYear(@Param("etudiantId") Long etudiantId, @Param("annee") String annee);

    /**
     * Trouve toutes les inscriptions actives
     */
    List<InscriptionsClass> findByStatut(String statut);

    // ========================================
    // SUPPRESSION
    // ========================================

    /**
     * Supprime une inscription spécifique
     */
    void deleteByClasseIdAndEtudiantId(Long classeId, Long etudiantId);
}