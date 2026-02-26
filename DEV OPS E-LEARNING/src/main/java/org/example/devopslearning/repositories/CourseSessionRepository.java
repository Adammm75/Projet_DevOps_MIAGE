package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

    List<CourseSession> findByCourseId(Long courseId);
    List<CourseSession> findByCourseIdOrderBySessionDateDesc(Long courseId);

    @Query("SELECT s FROM CourseSession s LEFT JOIN FETCH s.course WHERE s.id = :id")
    Optional<CourseSession> findByIdWithCourse(@Param("id") Long id);
}