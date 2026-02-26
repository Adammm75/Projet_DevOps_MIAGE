package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseEnrollment;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    /**
     * Trouve toutes les inscriptions d'un étudiant
     */
    List<CourseEnrollment> findByStudentId(Long studentId);

    /**
     * ✅ AJOUTÉ - Trouve toutes les inscriptions à un cours
     */
    List<CourseEnrollment> findByCourseId(Long courseId);

    /**
     * ✅ AJOUTÉ - Trouve une inscription spécifique
     */
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    /**
     * ✅ AJOUTÉ - Vérifie si un étudiant est inscrit à un cours
     */
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    /**
     * Compte le nombre d'étudiants inscrits à un cours
     */
    long countByCourseId(Long courseId);

    /**
     * Trouve les inscriptions actives d'un étudiant
     */
    List<CourseEnrollment> findByStudentIdAndStatus(Long studentId, String status);

    /**
     * ✅ BONUS - Trouve les inscriptions par statut
     */
    List<CourseEnrollment> findByStatus(String status);

    /**
     * ✅ BONUS - Trouve les étudiants d'un cours directement
     */
    @Query("SELECT ce.student FROM CourseEnrollment ce WHERE ce.course.id = :courseId AND ce.status = :status")
    List<User> findStudentsByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") String status);

    /**
     * ✅ BONUS - Compte les inscriptions actives d'un cours
     */
    long countByCourseIdAndStatus(Long courseId, String status);

    /**
     * ✅ BONUS - Trouve toutes les inscriptions d'un cours avec statut
     */
    List<CourseEnrollment> findByCourseIdAndStatus(Long courseId, String status);
}