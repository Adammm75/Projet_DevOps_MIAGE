package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.StudentPoints;
import org.example.devopslearning.entities.StudentPoints.AcademicLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPointsRepository extends JpaRepository<StudentPoints, Long> {

    Optional<StudentPoints> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<StudentPoints> findByStudentId(Long studentId);

    List<StudentPoints> findByCourseId(Long courseId);

    /**
     * Compte le nombre d'étudiants par niveau pour un cours donné.
     */
    @Query("SELECT sp.academicLevel, COUNT(sp) FROM StudentPoints sp WHERE sp.course.id = :courseId GROUP BY sp.academicLevel")
    List<Object[]> countByLevelForCourse(@Param("courseId") Long courseId);

    /**
     * Étudiants en difficulté : niveau DÉCOUVERTE pour un cours donné.
     */
    @Query("SELECT sp FROM StudentPoints sp WHERE sp.course.id = :courseId AND sp.academicLevel = :level ORDER BY sp.totalPoints ASC")
    List<StudentPoints> findByCourseIdAndAcademicLevel(@Param("courseId") Long courseId, @Param("level") AcademicLevel level);

    /**
     * Classement des étudiants d'un cours par points.
     */
    @Query("SELECT sp FROM StudentPoints sp WHERE sp.course.id = :courseId ORDER BY sp.totalPoints DESC")
    List<StudentPoints> findByCourseIdOrderByPointsDesc(@Param("courseId") Long courseId);
}