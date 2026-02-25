package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {

    // ========================================
    // MÉTHODES DE BASE
    // ========================================

    /**
     * ✅ Récupère tous les cours d'un enseignant (par objet User)
     */
    List<Cours> findByCreatedBy(User teacher);

    /**
     * ✅ Récupère tous les cours d'un enseignant (par ID)
     */
    List<Cours> findByCreatedById(Long teacherId);

    /**
     * ✅ Récupère un cours par son code
     */
    Optional<Cours> findByCode(String code);

    /**
     * ✅ Vérifie si un code de cours existe déjà
     */
    boolean existsByCode(String code);

    /**
     * ✅ Vérifie si un cours avec ce titre existe pour un enseignant
     */
    boolean existsByTitleAndCreatedBy(String title, User teacher);

    // ========================================
    // RECHERCHE ET TRI
    // ========================================

    /**
     * ✅ Trouve les cours par ordre de création (plus récents en premier)
     */
    List<Cours> findAllByOrderByCreatedAtDesc();

    /**
     * ✅ Trouve les cours d'un enseignant triés par date de création
     */
    List<Cours> findByCreatedByOrderByCreatedAtDesc(User teacher);

    /**
     * ✅ Trouve les cours d'un enseignant triés par titre
     */
    List<Cours> findByCreatedByOrderByTitleAsc(User teacher);

    /**
     * ✅ Recherche de cours par titre ou code (INSENSIBLE À LA CASSE)
     */
    @Query("SELECT c FROM Cours c WHERE " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Cours> searchCourses(@Param("search") String search);

    /**
     * ✅ NOUVEAU : Recherche de cours par enseignant avec mot-clé
     */
    @Query("SELECT c FROM Cours c WHERE c.createdBy = :teacher AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Cours> searchCoursesByTeacher(@Param("teacher") User teacher,
                                       @Param("keyword") String keyword);

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * ✅ NOUVEAU : Compte le nombre de cours d'un enseignant
     */
    long countByCreatedBy(User teacher);

    /**
     * ✅ NOUVEAU : Compte le nombre total de cours actifs
     * (ayant au moins une ressource ou un assignment)
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Cours c " +
            "LEFT JOIN CourseRessource r ON r.course = c " +
            "LEFT JOIN Assignment a ON a.course = c " +
            "WHERE r.id IS NOT NULL OR a.id IS NOT NULL")
    long countActiveCourses();

    /**
     * ✅ NOUVEAU : Compte le nombre de cours créés par un enseignant dans une période
     */
    @Query("SELECT COUNT(c) FROM Cours c " +
            "WHERE c.createdBy = :teacher " +
            "AND c.createdAt BETWEEN :start AND :end")
    long countByCreatedByAndCreatedAtBetween(@Param("teacher") User teacher,
                                             @Param("start") Instant start,
                                             @Param("end") Instant end);

    // ========================================
    // REQUÊTES AVANCÉES
    // ========================================

    /**
     * ✅ NOUVEAU : Récupère les X derniers cours créés
     */
    List<Cours> findTop10ByOrderByCreatedAtDesc();

    /**
     * ✅ NOUVEAU : Récupère les cours créés dans une période
     */
    @Query("SELECT c FROM Cours c WHERE c.createdAt BETWEEN :start AND :end " +
            "ORDER BY c.createdAt DESC")
    List<Cours> findCoursesCreatedBetween(@Param("start") Instant start,
                                          @Param("end") Instant end);

    /**
     * ✅ NOUVEAU : Récupère les cours avec le nombre de ressources
     */
    @Query("SELECT c, COUNT(r) FROM Cours c " +
            "LEFT JOIN CourseRessource r ON r.course = c " +
            "WHERE c.createdBy = :teacher " +
            "GROUP BY c " +
            "ORDER BY c.createdAt DESC")
    List<Object[]> findCoursesWithResourceCount(@Param("teacher") User teacher);

    /**
     * ✅ NOUVEAU : Récupère les cours avec le nombre d'assignments
     */
    @Query("SELECT c, COUNT(a) FROM Cours c " +
            "LEFT JOIN Assignment a ON a.course = c " +
            "WHERE c.createdBy = :teacher " +
            "GROUP BY c " +
            "ORDER BY c.createdAt DESC")
    List<Object[]> findCoursesWithAssignmentCount(@Param("teacher") User teacher);

    /**
     * ✅ NOUVEAU : Récupère les cours sans ressources ni assignments (cours vides)
     */
    @Query("SELECT c FROM Cours c " +
            "WHERE c.createdBy = :teacher " +
            "AND NOT EXISTS (SELECT r FROM CourseRessource r WHERE r.course = c) " +
            "AND NOT EXISTS (SELECT a FROM Assignment a WHERE a.course = c)")
    List<Cours> findEmptyCoursesByTeacher(@Param("teacher") User teacher);



    // ========================================
    // GESTION ADMIN
    // ========================================

    /**
     * ✅ NOUVEAU : Récupère tous les cours avec leurs enseignants
     */
    @Query("SELECT c FROM Cours c " +
            "LEFT JOIN FETCH c.createdBy " +
            "ORDER BY c.createdAt DESC")
    List<Cours> findAllWithTeachers();

    /**
     * ✅ NOUVEAU : Recherche globale pour admin (tous les cours)
     */
    @Query("SELECT c FROM Cours c " +
            "LEFT JOIN c.createdBy u " +
            "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Cours> searchAllCourses(@Param("keyword") String keyword);

    /**
     * ✅ NOUVEAU : Récupère les cours par statut (actif/inactif)
     */
    @Query("SELECT c FROM Cours c WHERE " +
            "(:isActive = true AND EXISTS (SELECT r FROM CourseRessource r WHERE r.course = c)) " +
            "OR (:isActive = false AND NOT EXISTS (SELECT r FROM CourseRessource r WHERE r.course = c))")
    List<Cours> findByActiveStatus(@Param("isActive") boolean isActive);

    // ========================================
    // MÉTHODES DE SUPPRESSION
    // ========================================

    /**
     * ✅ NOUVEAU : Supprime tous les cours d'un enseignant
     * ATTENTION: À utiliser avec précaution (cascade)
     */
    void deleteByCreatedBy(User teacher);

    /**
     * ✅ NOUVEAU : Supprime les cours créés avant une certaine date
     * Utile pour archivage/nettoyage
     */
    @Query("DELETE FROM Cours c WHERE c.createdAt < :date")
    void deleteOldCourses(@Param("date") Instant date);

    // ------------------------------
    // 1️⃣ findCoursesByTeacherId
    // ------------------------------
    // Utilise la propriété 'createdBy.id'
    @Query("SELECT c FROM Cours c WHERE c.createdBy.id = :teacherId")
    List<Cours> findCoursesByTeacherId(@Param("teacherId") Long teacherId);

    // ------------------------------
    // 2️⃣ findTeachersForCourse
    // ------------------------------
    // Retourne l'enseignant d'un cours donné (cours.id)
    @Query("SELECT c.createdBy FROM Cours c WHERE c.id = :courseId")
    List<User> findTeachersForCourse(@Param("courseId") Long courseId);

    // ------------------------------
    // 3️⃣ findCoursesForStudent
    // ------------------------------
    // Si tu passes par InactivityAlert pour relier étudiants et cours :
    @Query("""
    SELECT ce.course FROM CourseEnrollment ce
    WHERE ce.student.id = :studentId
      AND ce.status = 'ACTIVE'""")
        List<Cours> findCoursesForStudent(@Param("studentId") Long studentId);

    

}
