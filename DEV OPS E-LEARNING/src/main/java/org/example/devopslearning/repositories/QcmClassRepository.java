package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QcmClassRepository extends JpaRepository<QcmClass, Long> {

    /**
     * Trouve toutes les affectations d'un QCM
     */
    List<QcmClass> findByQcmId(Long qcmId);

    /**
     * Trouve tous les QCM d'une classe
     */
    List<QcmClass> findByClasseId(Long classeId);

    /**
     * Vérifie si un QCM est déjà affecté à une classe
     */
    boolean existsByQcmIdAndClasseId(Long qcmId, Long classeId);

    /**
     * Supprime l'affectation d'un QCM à une classe
     */
    void deleteByQcmIdAndClasseId(Long qcmId, Long classeId);

    /**
     * Compte le nombre de classes auxquelles un QCM est affecté
     */
    long countByQcmId(Long qcmId);

    /**
     * Récupère les IDs des classes d'un QCM
     */
    @Query("SELECT qc.classe.id FROM QcmClass qc WHERE qc.qcm.id = :qcmId")
    List<Long> findClasseIdsByQcmId(@Param("qcmId") Long qcmId);

    /**
     * Récupère les QCM publiés d'une classe
     */
    @Query("SELECT qc FROM QcmClass qc WHERE qc.classe.id = :classeId AND qc.qcm.publie = true ORDER BY qc.dateAffectation DESC")
    List<QcmClass> findPublishedQcmsByClasseId(@Param("classeId") Long classeId);
}