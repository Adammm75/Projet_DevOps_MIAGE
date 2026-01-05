package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.InscriptionsClass;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscriptionsClassRepository extends JpaRepository<InscriptionsClass, Long> {

    /**
     * Trouve toutes les inscriptions d'une classe
     */
    List<InscriptionsClass> findByClasseId(Long classeId);

    /**
     * ✅ AJOUTÉ - Trouve les inscriptions d'une classe par statut
     */
    List<InscriptionsClass> findByClasseIdAndStatut(Long classeId, String statut);

    /**
     * Trouve toutes les inscriptions d'un étudiant
     */
    List<InscriptionsClass> findByEtudiantId(Long etudiantId);

    /**
     * ✅ AJOUTÉ - Trouve les inscriptions d'un étudiant par statut
     */
    List<InscriptionsClass> findByEtudiantIdAndStatut(Long etudiantId, String statut);

    /**
     * ✅ AJOUTÉ - Trouve une inscription spécifique
     */
    Optional<InscriptionsClass> findByClasseIdAndEtudiantId(Long classeId, Long etudiantId);

    /**
     * Vérifie si un étudiant est inscrit dans une classe
     */
    boolean existsByClasseIdAndEtudiantId(Long classeId, Long etudiantId);

    /**
     * ✅ AJOUTÉ - Compte les étudiants actifs dans une classe
     */
    long countByClasseIdAndStatut(Long classeId, String statut);

    /**
     * ✅ AJOUTÉ - Trouve les étudiants qui ne sont PAS inscrits dans une classe
     */
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT ic.etudiant.id FROM InscriptionsClass ic WHERE ic.classe.id = :classeId) " +
            "AND EXISTS (SELECT ur FROM u.userRoles ur WHERE ur.role.name = 'ROLE_STUDENT')")
    List<User> findStudentsNotInClass(@Param("classeId") Long classeId);

    /**
     * Supprime une inscription
     */
    void deleteByClasseIdAndEtudiantId(Long classeId, Long etudiantId);

    /**
     * ✅ BONUS - Trouve tous les étudiants d'une classe (directement)
     */
    @Query("SELECT ic.etudiant FROM InscriptionsClass ic WHERE ic.classe.id = :classeId AND ic.statut = :statut")
    List<User> findStudentsByClasseIdAndStatut(@Param("classeId") Long classeId, @Param("statut") String statut);

    /**
     * ✅ BONUS - Compte le nombre total d'inscriptions d'une classe
     */
    long countByClasseId(Long classeId);

    /**
     * ✅ BONUS - Trouve toutes les inscriptions actives
     */
    List<InscriptionsClass> findByStatut(String statut);
}