package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmTentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QcmTentativeRepository extends JpaRepository<QcmTentative, Long> {

    // ========================================
    // RÉCUPÉRATION PAR ÉTUDIANT
    // ========================================

    /**
     * Trouve toutes les tentatives d'un étudiant
     */
    List<QcmTentative> findByEtudiantId(Long etudiantId);

    /**
     * Trouve les tentatives d'un étudiant triées par date
     */
    List<QcmTentative> findByEtudiantIdOrderByDateDebutDesc(Long etudiantId);

    // ========================================
    // RÉCUPÉRATION PAR QCM
    // ========================================

    /**
     * Trouve toutes les tentatives d'un QCM
     */
    List<QcmTentative> findByQcmId(Long qcmId);

    /**
     * Trouve les tentatives d'un QCM triées par date
     */
    List<QcmTentative> findByQcmIdOrderByDateDebutDesc(Long qcmId);

    // ========================================
    // RÉCUPÉRATION PAR ÉTUDIANT ET QCM
    // ========================================

    /**
     * Trouve les tentatives d'un étudiant pour un QCM
     */
    List<QcmTentative> findByEtudiantIdAndQcmId(Long etudiantId, Long qcmId);

    /**
     * Trouve les tentatives d'un étudiant pour un QCM triées par date
     */
    List<QcmTentative> findByQcmIdAndEtudiantIdOrderByDateDebutDesc(Long qcmId, Long etudiantId);

    // ========================================
    // RÉCUPÉRATION PAR STATUT
    // ========================================

    /**
     * Trouve les tentatives d'un étudiant par statut
     */
    List<QcmTentative> findByEtudiantIdAndStatut(Long etudiantId, String statut);

    /**
     * Trouve les tentatives d'un QCM par statut
     */
    List<QcmTentative> findByQcmIdAndStatut(Long qcmId, String statut);

    // ========================================
    // COMPTEURS
    // ========================================

    /**
     * Compte le nombre de tentatives d'un étudiant
     */
    long countByEtudiantId(Long etudiantId);

    /**
     * Compte le nombre de tentatives d'un QCM
     */
    long countByQcmId(Long qcmId);

    /**
     * Compte le nombre de tentatives d'un étudiant pour un QCM
     */
    long countByEtudiantIdAndQcmId(Long etudiantId, Long qcmId);

    /**
     * Compte les tentatives par statut pour un étudiant
     */
    long countByEtudiantIdAndStatut(Long etudiantId, String statut);

    // ========================================
    // VÉRIFICATIONS
    // ========================================

    /**
     * Vérifie si un étudiant a déjà passé un QCM
     */
    boolean existsByEtudiantIdAndQcmId(Long etudiantId, Long qcmId);
}
