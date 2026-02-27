package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

    List<CourseSession> findByCourseId(Long courseId);

    /** Séances passées d'un cours (pour rappels appel) */
    @Query("SELECT s FROM CourseSession s WHERE s.course.id = :courseId AND s.sessionDate < :now ORDER BY s.sessionDate DESC")
    List<CourseSession> findPastByCourseId(@Param("courseId") Long courseId, @Param("now") Instant now);

    /** Toutes les séances passées des cours d'un enseignant */
    @Query("SELECT s FROM CourseSession s WHERE s.course.createdBy.id = :teacherId AND s.sessionDate < :now ORDER BY s.sessionDate DESC")
    List<CourseSession> findPastSessionsByTeacherId(@Param("teacherId") Long teacherId, @Param("now") Instant now);

    /** Séances passées depuis N jours pour un enseignant */
    @Query("SELECT s FROM CourseSession s WHERE s.course.createdBy.id = :teacherId AND s.sessionDate BETWEEN :from AND :now ORDER BY s.sessionDate DESC")
    List<CourseSession> findRecentPastSessionsByTeacherId(@Param("teacherId") Long teacherId,
                                                          @Param("from") Instant from,
                                                          @Param("now") Instant now);
    List<CourseSession> findByCourseIdOrderBySessionDateDesc(Long courseId);

    @Query("SELECT s FROM CourseSession s LEFT JOIN FETCH s.course WHERE s.id = :id")
    Optional<CourseSession> findByIdWithCourse(@Param("id") Long id);
}