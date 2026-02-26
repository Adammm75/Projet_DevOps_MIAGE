package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ✅ REPOSITORY COMPLET ET UNIFIÉ pour CourseRessource
 * Gère tous les types de ressources (fichiers normaux + audios avec transcription)
 */
public interface CourseResourceRepository extends JpaRepository<CourseRessource, Long> {

    // ========================================
    // MÉTHODES DE BASE (Par objet Cours)
    // ========================================

    /**
     * Trouve toutes les ressources d'un cours
     */
    List<CourseRessource> findByCourse(Cours cours);

    /**
     * Compte le nombre de ressources d'un cours
     */
    long countByCourse(Cours cours);

    /**
     * Trouve les ressources d'un cours triées par date (récentes en premier)
     */
    List<CourseRessource> findByCourseOrderByCreatedAtDesc(Cours cours);

    /**
     * Trouve les ressources d'un cours par type
     */
    List<CourseRessource> findByCourseAndType(Cours cours, String type);

    // ========================================
    // MÉTHODES PAR ID DE COURS
    // ========================================

    /**
     * ✅ Trouve toutes les ressources d'un cours par ID
     */
    List<CourseRessource> findByCourse_Id(Long courseId);

    /**
     * ✅ Compte le nombre de ressources d'un cours par ID
     */
    long countByCourse_Id(Long courseId);

    /**
     * ✅ Ressources triées par date (récentes en premier)
     */
    List<CourseRessource> findByCourse_IdOrderByCreatedAtDesc(Long courseId);

    /**
     * ✅ Ressources par type
     */
    List<CourseRessource> findByCourse_IdAndType(Long courseId, String type);

    // ========================================
    // RECHERCHE ET FILTRES
    // ========================================

    /**
     * ✅ Recherche par titre
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId AND " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CourseRessource> searchByTitleInCourse(@Param("courseId") Long courseId,
                                                @Param("keyword") String keyword);

    /**
     * ✅ Trouve les ressources avec transcription
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId " +
            "AND r.transcript IS NOT NULL AND r.transcript != ''")
    List<CourseRessource> findResourcesWithTranscript(@Param("courseId") Long courseId);

    /**
     * ✅ Trouve les ressources avec résumé
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId " +
            "AND r.summary IS NOT NULL AND r.summary != ''")
    List<CourseRessource> findResourcesWithSummary(@Param("courseId") Long courseId);

    /**
     * ✅ Trouve la dernière ressource audio avec résumé d'un cours
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId " +
            "AND r.type = 'AUDIO' AND r.summary IS NOT NULL " +
            "ORDER BY r.createdAt DESC")
    Optional<CourseRessource> findLatestAudioWithSummary(@Param("courseId") Long courseId);

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * ✅ Compte les ressources par type pour un cours
     */
    @Query("SELECT r.type, COUNT(r) FROM CourseRessource r " +
            "WHERE r.course.id = :courseId GROUP BY r.type")
    List<Object[]> countResourcesByType(@Param("courseId") Long courseId);

    /**
     * ✅ Vérifie si un cours a au moins une ressource
     */
    boolean existsByCourse_Id(Long courseId);

    /**
     * ✅ Vérifie si une ressource avec ce titre existe déjà dans le cours
     */
    boolean existsByCourse_IdAndTitle(Long courseId, String title);

    // ========================================
    // GESTION DES URLS
    // ========================================

    /**
     * ✅ Trouve les ressources par URL
     */
    List<CourseRessource> findByUrl(String url);

    /**
     * ✅ Trouve les ressources par URL audio S3
     */
    List<CourseRessource> findByS3AudioUrl(String s3AudioUrl);

    // ========================================
    // SUPPRESSION
    // ========================================

    /**
     * ✅ Supprime toutes les ressources d'un cours
     */
    void deleteByCourse_Id(Long courseId);
}