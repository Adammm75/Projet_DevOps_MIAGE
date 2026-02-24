package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CourseTeacher;
import org.example.devopslearning.entities.CourseTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseTeacherRepository extends JpaRepository<CourseTeacher, CourseTeacherId> {

    /**
     * Trouve tous les cours d'un enseignant
     */
    @Query("SELECT ct FROM CourseTeacher ct WHERE ct.id.teacherId = :teacherId")
    List<CourseTeacher> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Trouve tous les enseignants d'un cours
     */
    @Query("SELECT ct FROM CourseTeacher ct WHERE ct.id.courseId = :courseId")
    List<CourseTeacher> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Vérifie si un enseignant est affecté à un cours
     */
    default boolean existsByCourseIdAndTeacherId(Long courseId, Long teacherId) {
        CourseTeacherId id = new CourseTeacherId(courseId, teacherId);
        return existsById(id);
    }
}