package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmRepons;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QcmReponsRepository extends JpaRepository<QcmRepons, Long> {
    List<QcmRepons> findByTentativeId(Long tentativeId);

    // ========================================
    // RÉCUPÉRATION PAR TENTATIVE
    // ========================================


    List<QcmRepons> findByTentativeIdOrderByQuestionId(Long tentativeId);

    // ========================================
    // RÉCUPÉRATION PAR QUESTION
    // ========================================

    List<QcmRepons> findByQuestionId(Long questionId);

    // ========================================
    // RÉCUPÉRATION SPÉCIFIQUE
    // ========================================

    Optional<QcmRepons> findByTentativeIdAndQuestionId(Long tentativeId, Long questionId);

    // ========================================
    // COMPTEURS
    // ========================================

    long countByTentativeId(Long tentativeId);

    long countByQuestionId(Long questionId);

    // ========================================
    // VÉRIFICATIONS
    // ========================================

    boolean existsByTentativeIdAndQuestionId(Long tentativeId, Long questionId);

    // ========================================
    // SUPPRESSION
    // ========================================

    void deleteByTentativeId(Long tentativeId);
}
