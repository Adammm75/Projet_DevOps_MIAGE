package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.entities.ResourceConsultation;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ResourceConsultationRepository extends JpaRepository<ResourceConsultation, Long> {

    /** Trouve la consultation d'un étudiant pour une ressource */
    Optional<ResourceConsultation> findByResourceAndStudent(CourseRessource resource, User student);

    /** IDs des ressources consultées par un étudiant dans un cours */
    @Query("SELECT rc.resource.id FROM ResourceConsultation rc " +
            "WHERE rc.student.id = :studentId AND rc.resource.course.id = :courseId")
    Set<Long> findConsultedResourceIdsByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    /** Nombre de consultations distinctes (étudiants uniques) par ressource */
    @Query("SELECT rc.resource.id, COUNT(rc) FROM ResourceConsultation rc " +
            "WHERE rc.resource.course.id = :courseId GROUP BY rc.resource.id")
    List<Object[]> countStudentsPerResourceInCourse(@Param("courseId") Long courseId);

    /** Nombre total de ressources consultées par un étudiant dans un cours */
    @Query("SELECT COUNT(rc) FROM ResourceConsultation rc " +
            "WHERE rc.student.id = :studentId AND rc.resource.course.id = :courseId")
    long countConsultedByStudentInCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    // ✅ AJOUTÉ : pour le suivi d'inactivité des rappels
    /** Vérifie si un étudiant a consulté une ressource d'un cours depuis une date donnée */
    @Query("SELECT COUNT(rc) FROM ResourceConsultation rc " +
            "WHERE rc.student.id = :studentId " +
            "AND rc.resource.course.id = :courseId " +
            "AND rc.consultedAt >= :since")
    long countRecentConsultationsByStudentInCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("since") Instant since);

    /** IDs des étudiants ayant consulté un cours depuis une date donnée */
    @Query("SELECT DISTINCT rc.student.id FROM ResourceConsultation rc " +
            "WHERE rc.resource.course.id = :courseId AND rc.consultedAt >= :since")
    Set<Long> findActiveStudentIdsSince(
            @Param("courseId") Long courseId,
            @Param("since") Instant since);
}