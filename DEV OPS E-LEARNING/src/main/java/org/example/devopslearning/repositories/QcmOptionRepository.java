package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ✅ Repository pour les options de réponse des questions QCM
 */
public interface QcmOptionRepository extends JpaRepository<QcmOption, Long> {

    /**
     * Trouve toutes les options d'une question
     */
    List<QcmOption> findByQuestionId(Long questionId);

    /**
     * Trouve toutes les options d'une question, triées par ID
     */
    List<QcmOption> findByQuestionIdOrderById(Long questionId);

    /**
     * Trouve les bonnes réponses d'une question
     */
    List<QcmOption> findByQuestionIdAndEstCorrecteTrue(Long questionId);

    /**
     * Compte le nombre d'options d'une question
     */
    long countByQuestionId(Long questionId);

    /**
     * Compte le nombre de bonnes réponses d'une question
     */
    long countByQuestionIdAndEstCorrecteTrue(Long questionId);

    /**
     * Supprime toutes les options d'une question
     */
    void deleteByQuestionId(Long questionId);

    /**
     * Trouve toutes les options d'un QCM (via les questions)
     */
    @Query("SELECT o FROM QcmOption o WHERE o.question.qcm.id = :qcmId")
    List<QcmOption> findByQcmId(@Param("qcmId") Long qcmId);
}