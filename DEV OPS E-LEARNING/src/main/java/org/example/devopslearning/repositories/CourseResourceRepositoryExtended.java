package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ✅ Extension du Repository pour CourseRessource avec méthodes supplémentaires
 */
@Repository
public interface CourseResourceRepositoryExtended extends JpaRepository<CourseRessource, Long> {

    /**
     * ✅ Trouve toutes les ressources d'un cours
     */
    List<CourseRessource> findByCourse(Cours cours);

    /**
     * ✅ Trouve toutes les ressources d'un cours par ID
     */
    List<CourseRessource> findByCourse_Id(Long courseId);

    /**
     * ✅ Compte le nombre de ressources d'un cours
     */
    long countByCourse(Cours cours);

    /**
     * ✅ Compte le nombre de ressources d'un cours par ID
     */
    long countByCourse_Id(Long courseId);

    /**
     * ✅ Trouve les ressources d'un cours triées par date (plus récentes en premier)
     */
    List<CourseRessource> findByCourse_IdOrderByCreatedAtDesc(Long courseId);

    /**
     * ✅ Trouve les ressources d'un cours par type
     */
    List<CourseRessource> findByCourse_IdAndType(Long courseId, String type);

    /**
     * ✅ Trouve les ressources par type (tous cours confondus)
     */
    List<CourseRessource> findByType(String type);

    /**
     * ✅ Recherche de ressources par titre (contient)
     */
    @Query("SELECT r FROM CourseRessource r WHERE " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CourseRessource> searchByTitle(@Param("keyword") String keyword);

    /**
     * ✅ Recherche de ressources par titre dans un cours spécifique
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId AND " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CourseRessource> searchByTitleInCourse(@Param("courseId") Long courseId,
                                                @Param("keyword") String keyword);

    /**
     * ✅ Récupère les N dernières ressources d'un cours
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId " +
            "ORDER BY r.createdAt DESC")
    List<CourseRessource> findTopNByCourseId(@Param("courseId") Long courseId);

    /**
     * ✅ Compte les ressources par type pour un cours
     */
    @Query("SELECT r.type, COUNT(r) FROM CourseRessource r " +
            "WHERE r.course.id = :courseId " +
            "GROUP BY r.type")
    List<Object[]> countResourcesByType(@Param("courseId") Long courseId);

    /**
     * ✅ Trouve toutes les ressources avec leur cours (JOIN FETCH)
     */
    @Query("SELECT r FROM CourseRessource r " +
            "JOIN FETCH r.course c " +
            "WHERE c.id = :courseId")
    List<CourseRessource> findAllWithCourse(@Param("courseId") Long courseId);

    /**
     * ✅ Vérifie si une ressource avec ce titre existe déjà dans le cours
     */
    boolean existsByCourse_IdAndTitle(Long courseId, String title);

    /**
     * ✅ Supprime toutes les ressources d'un cours
     */
    void deleteByCourse_Id(Long courseId);

    /**
     * ✅ Compte le nombre total de ressources (tous cours)
     */
    @Query("SELECT COUNT(r) FROM CourseRessource r")
    long countAllResources();

    /**
     * ✅ Récupère les ressources par URL (utile pour vérifier les doublons)
     */
    List<CourseRessource> findByUrl(String url);

    /**
     * ✅ Trouve les ressources qui ont une description
     */
    @Query("SELECT r FROM CourseRessource r WHERE r.course.id = :courseId " +
            "AND r.description IS NOT NULL AND r.description != ''")
    List<CourseRessource> findResourcesWithDescription(@Param("courseId") Long courseId);

    /**
     * ✅ Statistiques globales des ressources
     */
    @Query("SELECT COUNT(r), COUNT(DISTINCT r.course), AVG(LENGTH(r.title)) " +
            "FROM CourseRessource r")
    Object[] getGlobalStats();
}